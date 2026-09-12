/*
  Write a bulletin post.

  There's no per-field backend validation error for this endpoint (see the
  comment on bulletinPostSchema in src/lib/schemas.ts) - client-side zod
  validation is what actually stops a blank post from being submitted. Any
  backend-side failure surfaces as one general message via onError, not a
  field-level one.

  Always posts as status: 'PUBLISHED' and isFacultyAnnouncement: false - a
  regular student has no business creating a faculty announcement, and there
  is no moderation/HIDDEN workflow built anywhere for this page to hook into.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'

import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { TextField } from '@/components/ui/TextField'
import { Textarea } from '@/components/ui/Textarea'
import type { BulletinPostValues } from '@/lib/schemas'
import { bulletinPostSchema } from '@/lib/schemas'

type PostComposerProps = {
  onSubmit: (values: BulletinPostValues) => Promise<void>
}

export function PostComposer({ onSubmit }: PostComposerProps) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<BulletinPostValues>({ resolver: zodResolver(bulletinPostSchema) })

  const submit = handleSubmit(async (values) => {
    try {
      await onSubmit(values)
      reset()
    } catch {
      // The parent (BulletinPage) already knows the specific error message;
      // this form only needs to know something went wrong so it can show a
      // generic fallback without duplicating error-formatting logic.
      setError('root', { message: "Couldn't post that. Please try again." })
    }
  })

  return (
    <Card>
      <form onSubmit={submit} noValidate className="space-y-3">
        {errors.root && <Alert>{errors.root.message}</Alert>}

        <TextField
          label="Title"
          placeholder="What's this about?"
          error={errors.title?.message}
          {...register('title')}
        />

        <Textarea
          label="What's happening on campus?"
          placeholder="Share an announcement, event, or notice..."
          rows={3}
          error={errors.content?.message}
          {...register('content')}
        />

        <div className="flex justify-end">
          <Button type="submit" loading={isSubmitting}>
            Post
          </Button>
        </div>
      </form>
    </Card>
  )
}