/*
  Signup. Registration returns no token by design - the account is inert until
  the emailed code proves the student owns the mailbox - so this always hands
  off to /verify rather than logging anyone in.
*/

import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'

import { Alert } from '@/components/ui/Alert'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button } from '@/components/ui/Button'
import { TextField } from '@/components/ui/TextField'
import { authApi } from '@/lib/api/auth'
import { ApiError } from '@/lib/api/client'
import type { Campus } from '@/lib/api/types'
import { signUpSchema } from '@/lib/schemas'
import type { SignUpValues } from '@/lib/schemas'

export function SignUpPage() {
  const navigate = useNavigate()
  const [formError, setFormError] = useState<string | null>(null)
  const [campuses, setCampuses] = useState<Campus[]>([])

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<SignUpValues>({ resolver: zodResolver(signUpSchema) })

  // GET /api/campuses is permitAll, so this works before signing in.
  // A failure here is not worth blocking signup over - campus is optional.
  useEffect(() => {
    let cancelled = false
    authApi
      .campuses()
      .then((list) => {
        if (!cancelled) setCampuses(list)
      })
      .catch(() => undefined)
    return () => {
      cancelled = true
    }
  }, [])

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      await authApi.register({
        email: values.email,
        firstName: values.firstName,
        lastName: values.lastName,
        password: values.password,
        campusId: values.campusId ? Number(values.campusId) : null,
      })

      // rememberMe is false on purpose: a brand-new account is a brand-new
      // device, and there is no checkbox to answer it here. The student can tick
      // it on their next sign-in.
      navigate('/verify', {
        replace: true,
        state: { email: values.email, rememberMe: false },
      })
    } catch (error) {
      if (error instanceof ApiError) {
        // Map the backend's per-field messages onto the matching inputs.
        for (const [field, message] of Object.entries(error.fields)) {
          if (field in values) {
            setError(field as keyof SignUpValues, { message })
          }
        }
        setFormError(Object.keys(error.fields).length ? null : error.message)
        return
      }
      setFormError('Something went wrong. Please try again.')
    }
  })

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Sign up with your CPUT student email. We'll send a code to confirm it's you."
      footer={
        <>
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-brand-700 hover:underline">
            Sign in
          </Link>
        </>
      }
    >
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {formError && <Alert>{formError}</Alert>}

        <div className="grid gap-4 sm:grid-cols-2">
          <TextField
            label="First name"
            autoComplete="given-name"
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <TextField
            label="Last name"
            autoComplete="family-name"
            error={errors.lastName?.message}
            {...register('lastName')}
          />
        </div>

        <TextField
          label="Student email"
          type="email"
          inputMode="email"
          autoComplete="username"
          placeholder="240453182@mycput.ac.za"
          hint="Your student number followed by @mycput.ac.za"
          error={errors.email?.message}
          {...register('email')}
        />

        {campuses.length > 0 && (
          <div className="space-y-1.5">
            <label htmlFor="campusId" className="block text-sm font-medium text-ink-700">
              Campus <span className="font-normal text-ink-400">(optional)</span>
            </label>
            <select
              id="campusId"
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-ink-900 focus:outline-2 focus:outline-brand-600"
              defaultValue=""
              {...register('campusId')}
            >
              <option value="">Select your campus</option>
              {campuses.map((campus) => (
                <option key={campus.campusId} value={campus.campusId}>
                  {campus.name} — {campus.city}
                </option>
              ))}
            </select>
          </div>
        )}

        <TextField
          label="Password"
          type="password"
          autoComplete="new-password"
          hint="At least 8 characters"
          error={errors.password?.message}
          {...register('password')}
        />

        <TextField
          label="Confirm password"
          type="password"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />

        <Button type="submit" loading={isSubmitting}>
          Create account
        </Button>
      </form>
    </AuthLayout>
  )
}
