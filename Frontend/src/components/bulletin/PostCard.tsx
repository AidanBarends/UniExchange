import { useState } from 'react'

import { CATEGORY_LABELS } from '@/components/bulletin/categoryLabels'
import { PostComposer } from '@/components/bulletin/PostComposer'
import { Avatar } from '@/components/ui/Avatar'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import type { BulletinPost } from '@/lib/api/types'
import type { BulletinPostValues } from '@/lib/schemas'

type PostCardProps = {
  post: BulletinPost
  authorName: string | null
  imageUrl: string | null
  isOwner: boolean
  formatRelativeTime: (iso: string) => string
  onSave: (values: BulletinPostValues) => Promise<void>
  onDelete: () => Promise<void>
}

export function PostCard({
  post,
  authorName,
  imageUrl,
  isOwner,
  formatRelativeTime,
  onSave,
  onDelete,
}: PostCardProps) {
  const [editing, setEditing] = useState(false)
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const header = (
    <div className="flex items-center gap-3">
      <Avatar name={authorName} className="size-9" />
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-ink-900">{authorName ?? 'Someone'}</p>
        <p className="text-xs text-ink-500">{formatRelativeTime(post.createdAt)}</p>
      </div>
      <div className="flex gap-1.5">
        {post.category !== 'GENERAL' && <Badge tone="neutral">{CATEGORY_LABELS[post.category]}</Badge>}
        {post.facultyAnnouncement && <Badge tone="brand">Announcement</Badge>}
      </div>
    </div>
  )

  if (editing) {
    return (
      <Card>
        {header}
        <div className="mt-3">
          <PostComposer
            initialValues={{
              title: post.title,
              content: post.content,
              category: post.category,
              imageUrl: imageUrl ?? '',
            }}
            submitLabel="Save"
            onCancel={() => setEditing(false)}
            onSubmit={async (values) => {
              await onSave(values)
              setEditing(false)
            }}
          />
        </div>
      </Card>
    )
  }

  return (
    <Card>
      {header}

      <h2 className="mt-3 text-sm font-semibold text-ink-900">{post.title}</h2>
      <p className="mt-1 whitespace-pre-wrap text-sm text-ink-700">{post.content}</p>

      {imageUrl && (
        <img
          src={imageUrl}
          alt=""
          className="mt-3 max-h-96 w-full rounded-lg border border-gray-200 object-cover"
          onError={(event) => {
            event.currentTarget.style.display = 'none'
          }}
        />
      )}

      {isOwner && (
        <div className="mt-3 flex gap-2">
          {!confirmingDelete ? (
            <>
              <Button variant="ghost" onClick={() => setEditing(true)}>
                Edit
              </Button>
              <Button
                variant="ghost"
                className="!text-red-700 hover:!bg-red-50"
                onClick={() => setConfirmingDelete(true)}
              >
                Delete
              </Button>
            </>
          ) : (
            <>
              <Button
                variant="primary"
                loading={deleting}
                className="!bg-red-600 hover:!bg-red-700 active:!bg-red-800"
                onClick={async () => {
                  setDeleting(true)
                  try {
                    await onDelete()
                  } finally {
                    setDeleting(false)
                  }
                }}
              >
                Confirm delete
              </Button>
              <Button variant="ghost" disabled={deleting} onClick={() => setConfirmingDelete(false)}>
                Cancel
              </Button>
            </>
          )}
        </div>
      )}
    </Card>
  )
}