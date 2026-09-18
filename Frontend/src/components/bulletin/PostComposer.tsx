import { zodResolver } from '@hookform/resolvers/zod'
import { useRef, useState } from 'react'
import { useForm } from 'react-hook-form'

import { ALL_CATEGORIES, CATEGORY_LABELS } from '@/components/bulletin/categoryLabels'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Select } from '@/components/ui/Select'
import { TextField } from '@/components/ui/TextField'
import { Textarea } from '@/components/ui/Textarea'
import { ApiError } from '@/lib/api/client'
import { uploadsApi } from '@/lib/api/uploads'
import type { BulletinPostValues } from '@/lib/schemas'
import { bulletinPostSchema } from '@/lib/schemas'

const ACCEPTED_IMAGE_TYPES = 'image/png,image/jpeg,image/gif,image/webp'

type PostComposerProps = {
  onSubmit: (values: BulletinPostValues) => Promise<void>
  initialValues?: BulletinPostValues
  submitLabel?: string
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
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<BulletinPostValues>({
    resolver: zodResolver(bulletinPostSchema),
    defaultValues: initialValues ?? { category: 'GENERAL' },
  })
  const [showPhotoInput, setShowPhotoInput] = useState(Boolean(initialValues?.imageUrl))
  const [photoMode, setPhotoMode] = useState<'upload' | 'url'>(initialValues?.imageUrl ? 'url' : 'upload')
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const imageUrl = watch('imageUrl')

  const submit = handleSubmit(async (values) => {
    try {
      await onSubmit(values)
      if (!initialValues) {
        reset()
        setShowPhotoInput(false)
        setPhotoMode('upload')
      }
    } catch {
      setError('root', { message: "Couldn't post that. Please try again." })
    }
  })

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return

    setUploadError(null)
    setUploading(true)
    try {
      const { url } = await uploadsApi.image(file)
      setValue('imageUrl', url, { shouldValidate: true, shouldDirty: true })
    } catch (error) {
      setUploadError(error instanceof ApiError ? error.message : 'Could not upload that image.')
    } finally {
      setUploading(false)
    }
  }

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

        {showPhotoInput && (
          <div className="space-y-2 rounded-lg border border-gray-200 p-3">
            <div className="flex gap-1.5">
              <button
                type="button"
                onClick={() => setPhotoMode('upload')}
                aria-pressed={photoMode === 'upload'}
                className={`rounded-md px-2.5 py-1 text-xs font-medium ${
                  photoMode === 'upload' ? 'bg-brand-50 text-brand-700' : 'text-ink-500 hover:bg-gray-100'
                }`}
              >
                Upload a photo
              </button>
              <button
                type="button"
                onClick={() => setPhotoMode('url')}
                aria-pressed={photoMode === 'url'}
                className={`rounded-md px-2.5 py-1 text-xs font-medium ${
                  photoMode === 'url' ? 'bg-brand-50 text-brand-700' : 'text-ink-500 hover:bg-gray-100'
                }`}
              >
                Paste a link
              </button>
            </div>

            {photoMode === 'upload' ? (
              <div>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept={ACCEPTED_IMAGE_TYPES}
                  className="hidden"
                  onChange={handleFileChange}
                />
                <Button
                  type="button"
                  variant="ghost"
                  className="!w-auto"
                  loading={uploading}
                  onClick={() => fileInputRef.current?.click()}
                >
                  {imageUrl ? 'Choose a different photo' : 'Choose a photo'}
                </Button>
                {uploadError && <p className="text-xs text-red-600">{uploadError}</p>}
              </div>
            ) : (
              <TextField
                label="Image URL"
                placeholder="https://..."
                error={errors.imageUrl?.message}
                {...register('imageUrl')}
              />
            )}

            {imageUrl && (
              <img src={imageUrl} alt="" className="h-24 rounded-md border border-gray-200 object-cover" />
            )}
          </div>
        )}

        <div className="flex gap-3 text-ink-500">
          <button
            type="button"
            onClick={() =>
              setShowPhotoInput((shown) => {
                if (shown) {
                  setValue('imageUrl', '')
                  setUploadError(null)
                  setPhotoMode('upload')
                }
                return !shown
              })
            }
            aria-pressed={showPhotoInput}
            className="flex items-center gap-1.5 text-xs hover:text-ink-700"
          >
            <PhotoIcon className="size-4" />
            {showPhotoInput ? 'Remove photo' : 'Photo'}
          </button>
          <button
            type="button"
            disabled
            title="Coming soon"
            className="flex cursor-not-allowed items-center gap-1.5 text-xs text-ink-400"
          >
            <EventIcon className="size-4" />
            Event
          </button>
        </div>

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