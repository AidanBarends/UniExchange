/*
  Write (or edit) a bulletin post.

  There's no per-field backend validation error for this endpoint (see the
  comment on bulletinPostSchema in src/lib/schemas.ts) - client-side zod
  validation is what actually stops a blank post from being submitted. Any
  backend-side failure surfaces as one general message via onError, not a
  field-level one.

  New posts always go out as status: 'PUBLISHED' and isFacultyAnnouncement:
  false - a regular student has no business creating a faculty announcement,
  and there is no moderation/HIDDEN workflow built anywhere for this page to
  hook into. Editing preserves whatever status/isFacultyAnnouncement the post
  already had (see PostCard.tsx), since this form never changes either.

  The Photo/Event icon row (new-post mode only) is disabled UI matching the
  mockup - MOCK, not backend-connected. There's no image attachment or
  event-specific fields on BulletinPost to back them yet.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'

import { ALL_CATEGORIES, CATEGORY_LABELS } from '@/components/bulletin/categoryLabels'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Select } from '@/components/ui/Select'
import { TextField } from '@/components/ui/TextField'
import { Textarea } from '@/components/ui/Textarea'
import type { BulletinPostValues } from '@/lib/schemas'
import { bulletinPostSchema } from '@/lib/schemas'

type PostComposerProps = {
  onSubmit: (values: BulletinPostValues) => Promise<void>
  /** Pre-fills the form for editing an existing post. Omitted for a new post. */
  initialValues?: BulletinPostValues
  submitLabel?: string
  /** Shows a Cancel button next to submit - only relevant when editing. */
  onCancel?: () => void
}

export function PostComposer({
  onSubmit,
  initialValues,
  submitLabel = 'Post',
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
    // A new post needs a starting category too, or the picker would fall
    // back to whichever <option> happens to render first with no explicit
    // choice made.
    defaultValues: initialValues ?? { category: 'GENERAL' },
  })

  const submit = handleSubmit(async (values) => {
    try {
      await onSubmit(values)
      if (!initialValues) reset() // clear the composer after a new post; leave edited text as-is
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

        <Select label="Category" error={errors.category?.message} {...register('category')}>
          {ALL_CATEGORIES.map((category) => (
            <option key={category} value={category}>
              {CATEGORY_LABELS[category]}
            </option>
          ))}
        </Select>

        <Textarea
          label="What's happening on campus?"
          placeholder="Share an announcement, event, or notice..."
          rows={3}
          error={errors.content?.message}
          {...register('content')}
        />

        {!initialValues && (
          // Matches the mockup's Photo/Event icon row, but there is no image
          // attachment or event-specific fields on BulletinPost to back
          // these - disabled with a visible "Coming soon" title rather than
          // buttons that look real but silently do nothing when clicked.
          <div className="flex gap-3 text-ink-400">
            <button
              type="button"
              disabled
              title="Coming soon"
              className="flex cursor-not-allowed items-center gap-1.5 text-xs"
            >
              <PhotoIcon className="size-4" />
              Photo
            </button>
            <button
              type="button"
              disabled
              title="Coming soon"
              className="flex cursor-not-allowed items-center gap-1.5 text-xs"
            >
              <EventIcon className="size-4" />
              Event
            </button>
          </div>
        )}

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

function PhotoIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className={className}>
      <rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="8.5" cy="10" r="1.5" stroke="currentColor" strokeWidth="1.5" />
      <path
        d="M5 17l5-5 3 3 3-4 3 4"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function EventIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className={className}>
      <rect x="3" y="5" width="18" height="15" rx="2" stroke="currentColor" strokeWidth="1.5" />
      <path d="M3 9.5h18" stroke="currentColor" strokeWidth="1.5" />
      <path d="M8 3v4M16 3v4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  )
}