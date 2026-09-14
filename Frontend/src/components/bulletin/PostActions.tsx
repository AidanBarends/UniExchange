/*
  Edit / delete controls for a bulletin post - only rendered by BulletinPage
  when the signed-in user is the post's author (post.authorId === user.userId).

  IMPORTANT: that isOwner check is a UI decision, not enforcement. GET, PUT
  and DELETE on /api/bulletin-posts/** are all permitAll in SecurityConfig, so
  the backend accepts an update or delete from anyone with the post id, not
  just its author - same gap ListingOwnerActions documents for listings. The
  real fix needs a server-side ownership check; hiding the buttons here just
  stops this page from being the thing that fires the request.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { useState } from 'react'

import { PostComposer } from './PostComposer'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import type { BulletinPost } from '@/lib/api/types'
import type { BulletinPostValues } from '@/lib/schemas'

type PostActionsProps = {
  post: BulletinPost
  onUpdate: (values: BulletinPostValues) => Promise<void>
  onDelete: () => Promise<void>
}

export function PostActions({ post, onUpdate, onDelete }: PostActionsProps) {
  const [editing, setEditing] = useState(false)
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleDelete = async () => {
    setDeleting(true)
    setDeleteError(null)
    try {
      await onDelete()
    } catch {
      // BulletinPage doesn't catch this - see the comment on bulletinApi.remove -
      // so any failure (network, 404 from a stale row, etc.) lands here.
      setDeleteError("Couldn't delete that. Please try again.")
    } finally {
      setDeleting(false)
    }
  }

  if (editing) {
    return (
      <div className="mt-3">
        <PostComposer
          initialValues={{ title: post.title, content: post.content }}
          submitLabel="Save changes"
          errorMessage="Couldn't save that. Please try again."
          onSubmit={async (values) => {
            await onUpdate(values)
            setEditing(false)
          }}
          onCancel={() => setEditing(false)}
        />
      </div>
    )
  }

  return (
    <div className="mt-3 space-y-2">
      {deleteError && <Alert>{deleteError}</Alert>}

      <div className="flex justify-end gap-2">
        {!confirmingDelete ? (
          <>
            <Button variant="ghost" disabled={deleting} onClick={() => setEditing(true)}>
              Edit
            </Button>
            <Button
              variant="ghost"
              disabled={deleting}
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
              onClick={handleDelete}
            >
              Confirm delete
            </Button>
            <Button variant="ghost" disabled={deleting} onClick={() => setConfirmingDelete(false)}>
              Cancel
            </Button>
          </>
        )}
      </div>
    </div>
  )
}
