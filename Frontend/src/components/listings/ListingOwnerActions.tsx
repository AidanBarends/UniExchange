import { useState } from 'react'

import { Button } from '@/components/ui/Button'
import type { Listing } from '@/lib/api/types'

type ListingOwnerActionsProps = {
  listing: Listing
  onMarkSold: () => Promise<void>
  onDelete: () => Promise<void>
}

export function ListingOwnerActions({ listing, onMarkSold, onDelete }: ListingOwnerActionsProps) {
  const [pending, setPending] = useState<'sold' | 'delete' | null>(null)
  const [confirmingDelete, setConfirmingDelete] = useState(false)

  const handleMarkSold = async () => {
    setPending('sold')
    try {
      await onMarkSold()
    } finally {
      setPending(null)
    }
  }

  const handleDelete = async () => {
    setPending('delete')
    try {
      await onDelete()
    } finally {
      setPending(null)
    }
  }

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-4">
      <p className="mb-3 text-sm font-medium text-ink-700">Manage this listing</p>

      <div className="flex flex-col gap-2 sm:flex-row">
        {listing.status === 'ACTIVE' && (
          <Button
            variant="ghost"
            loading={pending === 'sold'}
            disabled={pending !== null}
            onClick={handleMarkSold}
          >
            Mark as sold
          </Button>
        )}

        {!confirmingDelete ? (
          <Button
            variant="ghost"
            disabled={pending !== null}
            className="!text-red-700 hover:!bg-red-50"
            onClick={() => setConfirmingDelete(true)}
          >
            Delete listing
          </Button>
        ) : (
          <div className="flex flex-1 gap-2">
            <Button
              variant="primary"
              loading={pending === 'delete'}
              disabled={pending !== null}
              className="!bg-red-600 hover:!bg-red-700 active:!bg-red-800"
              onClick={handleDelete}
            >
              Confirm delete
            </Button>
            <Button variant="ghost" disabled={pending !== null} onClick={() => setConfirmingDelete(false)}>
              Cancel
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}