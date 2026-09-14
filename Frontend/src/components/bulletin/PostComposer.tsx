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

  Also doubles as the edit form (see PostActions): pass initialValues to
  pre-fill title/content, submitLabel/onCancel to swap the button row. Editing
  never touches status/isFacultyAnnouncement - those aren't fields on this
  form - the caller (BulletinPage.handleUpdatePost) re-sends the post's
  existing values for them since PUT replaces the whole record.

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
  /** Pre-fills the form and switches reset() off on success - set for edit mode. */
  initialValues?: BulletinPostValues
  submitLabel?: string
  errorMessage?: string
  /** Renders a Cancel button next to submit - set for edit mode. */
  onCancel?: () => void
}

export function PostComposer({
  onSubmit,
  initialValues,
  submitLabel = 'Post',
  errorMessage = "Couldn't post that. Please try again.",
  onCancel,
}: PostComposerProps) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<BulletinPostValues>({
    resolver: zodResolver(bulletinPostSchema),
    defaultValues: initialValues,
  })

  const submit = handleSubmit(async (values) => {
    try {
      await onSubmit(values)
      // Editing unmounts this form on success (PostActions closes the inline
      // editor) rather than reusing it for another submission, so clearing
      // it back to blank would only be visible if the save failed midway.
      if (!initialValues) reset()
    } catch {
      // The parent (BulletinPage) already knows the specific error message;
      // this form only needs to know something went wrong so it can show a
      // generic fallback without duplicating error-formatting logic.
      setError('root', { message: errorMessage })
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

        <div className="flex justify-end gap-2">
          {onCancel && (
            <Button type="button" variant="ghost" disabled={isSubmitting} onClick={onCancel}>
              Cancel
            </Button>
          )}
          <Button type="submit" loading={isSubmitting}>
            {submitLabel}
          </Button>
        </div>
      </form>
    </Card>
  )
}