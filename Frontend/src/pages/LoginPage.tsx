/*
  Sign in.

  The password is never the whole story. Unless this browser has been trusted
  before, signing in only gets you as far as an emailed code:

    trusted device -> 200 with a token, straight to the app.
    otherwise      -> 202, no token, a code is on its way -> /verify.

  "Remember me" is what earns that trust. Ticked, the session and the device
  token go to localStorage and survive a restart. Unticked, both go to
  sessionStorage, so closing the browser loses them and the next sign-in needs a
  code again - which is exactly the behaviour the checkbox promises.

  The one non-obvious case: an account that never used its FIRST code gets a 403
  with code EMAIL_NOT_VERIFIED before any of this. Showing that as an error would
  be a dead end, so it redirects to the code screen and triggers a fresh code.
*/

import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'

import { useAuth } from '@/auth/useAuth'
import { Alert } from '@/components/ui/Alert'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button } from '@/components/ui/Button'
import { Checkbox } from '@/components/ui/Checkbox'
import { TextField } from '@/components/ui/TextField'
import { authApi } from '@/lib/api/auth'
import { ApiError } from '@/lib/api/client'
import { loginSchema } from '@/lib/schemas'
import type { LoginValues } from '@/lib/schemas'
import { readDeviceToken } from '@/lib/session'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { signIn } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { rememberMe: false },
  })

  const from = (location.state as { from?: string } | null)?.from ?? '/feed'

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      const result = await authApi.login({
        email: values.email,
        password: values.password,
        deviceToken: readDeviceToken(),
        rememberMe: values.rememberMe,
      })

      // A token means the backend recognised this browser and skipped the code.
      if ('token' in result) {
        signIn(result, values.rememberMe)
        navigate(from, { replace: true })
        return
      }

      /*
        No token: a code has just been sent. autoResend must stay false here -
        login already sent one, and asking for another within the backend's 60s
        cooldown would greet the student with an error on arrival.
      */
      navigate('/verify', {
        replace: true,
        state: {
          email: values.email,
          rememberMe: values.rememberMe,
          from,
          autoResend: false,
        },
      })
    } catch (error) {
      if (error instanceof ApiError && error.isUnverified) {
        navigate('/verify', {
          replace: true,
          state: { email: values.email, rememberMe: values.rememberMe, from, autoResend: true },
        })
        return
      }
      setFormError(
        error instanceof ApiError ? error.message : 'Something went wrong. Please try again.',
      )
    }
  })

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to your UniExchange account."
      footer={
        <>
          New here?{' '}
          <Link to="/signup" className="font-medium text-brand-700 hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {formError && <Alert>{formError}</Alert>}

        <TextField
          label="Student email"
          type="email"
          inputMode="email"
          autoComplete="username"
          placeholder="240453182@mycput.ac.za"
          error={errors.email?.message}
          {...register('email')}
        />

        <TextField
          label="Password"
          type="password"
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
        />

        <Checkbox
          label="Remember me on this device"
          hint="Stay signed in and skip the emailed code next time. Leave this off on a shared or campus computer."
          {...register('rememberMe')}
        />

        <Button type="submit" loading={isSubmitting}>
          Sign in
        </Button>
      </form>
    </AuthLayout>
  )
}
