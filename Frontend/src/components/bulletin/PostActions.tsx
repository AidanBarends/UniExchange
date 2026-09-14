/*
  Edit/delete actions for a post you wrote.

  IMPORTANT: same gap as ListingController - BulletinPostController's PUT and
  DELETE don't check that the caller is actually the post's author, they
  accept the request from any authenticated user. Hiding these buttons for
  non-owners (see isOwner in BulletinPage) is a UI decision, not enforcement.
  The real fix needs a server-side ownership check in BulletinPostController/
  BulletinPostServiceImpl - shared backend code, not something to patch from
  this page.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { useState } from 'react'

import { PostComposer } from '@/components/bulletin/PostComposer'
import { Button } from '@/components/ui/Button'
import type { BulletinPostValues } from '@/lib/schemas'

type PostActionsProps = {
  initialValues: BulletinPostValues
  onSave: (values: BulletinPostValues) => Promise<void>
  onDelete: () => Promise<void>
}

export function PostActions({ initialValues, onSave, onDelete }: PostActionsProps) {
  const [editing, setEditing] = useState(false)
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

  if (editing) {
    return (
      <div className="mt-3">
        <PostComposer
          initialValues={initialValues}
          submitLabel="Save"
          onCancel={() => setEditing(false)}
          onSubmit={async (values) => {
            await onSave(values)
            setEditing(false)
          }}
        />
      </div>
    )
  }

  return (
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
  )
}
