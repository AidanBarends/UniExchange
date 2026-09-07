/*
  Actions available only to the listing's own seller.

  No "Edit" button here on purpose: the backend supports PUT /api/listings/:id
  (listingsApi.update), but there is no edit route in the frontend yet -
  /listings/new (Wazeer's page) is create-only. Wiring an edit form is out of
  scope for the details page and would mean inventing UI he owns.

  IMPORTANT: the backend does NOT check that the caller is actually the
  seller on PATCH .../sold or DELETE - ListingController/ListingServiceImpl
  accept the request from any authenticated user, not just the owner. Hiding
  these buttons for non-owners (see isOwner in ListingDetailsPage) is a UI
  decision, not enforcement - anyone could still call the API directly. The
  real fix needs a server-side ownership check; that's shared backend code,
  not something to change unilaterally from this page.

  Owner: Aidan Barends (230255639), for /listings/:listingId only.
*/

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