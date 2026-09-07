/*
  A single listing.

  OWNER: Aidan Barends (230255639)
  ROUTE: /listings/:listingId

  NOTE: images come back with `primary`, not `isPrimary` - see the comment at the
  top of src/lib/api/types.ts for why.

  "Message seller" hands off to /messages rather than creating a conversation -
  messages.ts is unowned, so wiring a real conversation-creation flow here
  would risk duplicating whoever picks up messaging. Revisit once that's
  assigned and the flow is agreed.

  Your own components go in src/components/listings/.
*/

import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { useAuth } from '@/auth/useAuth'
import { PageHeader } from '@/components/layout/PageHeader'
import { ListingGallery } from '@/components/listings/ListingGallery'
import { ListingOwnerActions } from '@/components/listings/ListingOwnerActions'
import { SellerCard } from '@/components/listings/SellerCard'
import { Alert } from '@/components/ui/Alert'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { ApiError } from '@/lib/api/client'
import { listingsApi } from '@/lib/api/listings'
import type { Campus, Category, Listing, ListingImage, ListingStatus, User } from '@/lib/api/types'
import { usersApi } from '@/lib/api/users'

// PIECE 5: owner-only actions (mark as sold / delete). Third and last new
// component file for this page - ListingOwnerActions.tsx.

const currencyFormatter = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR' })
const dateFormatter = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'medium' })

const STATUS_TONE: Record<ListingStatus, 'success' | 'neutral' | 'warning' | 'danger'> = {
  ACTIVE: 'success',
  SOLD: 'neutral',
  REMOVED: 'warning',
  DELETED: 'danger',
}

type LoadState =
  | { status: 'loading' }
  | { status: 'not-found' }
  | { status: 'error'; message: string }
  | { status: 'ready'; listing: Listing }

export function ListingDetailsPage() {
  const { listingId } = useParams<{ listingId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()

  const numericId = listingId !== undefined && /^\d+$/.test(listingId) ? Number(listingId) : null

  const [state, setState] = useState<LoadState>({ status: 'loading' })
  const [category, setCategory] = useState<Category | null>(null)
  const [campus, setCampus] = useState<Campus | null>(null)
  const [images, setImages] = useState<ListingImage[]>([])
  const [seller, setSeller] = useState<User | null>(null)
  const [sellerLoading, setSellerLoading] = useState(true)
  const [rating, setRating] = useState<number | null>(null)
  const [reviewCount, setReviewCount] = useState<number | null>(null)

  // Piece 5: surfaces errors from mark-sold / delete, which happen after the
  // page is already 'ready' so they can't just set the LoadState to 'error'.
  const [actionError, setActionError] = useState<string | null>(null)

  const load = useCallback(async (id: number) => {
    setState({ status: 'loading' })
    setActionError(null)

    let listing: Listing
    try {
      listing = await listingsApi.byId(id)
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        setState({ status: 'not-found' })
      } else {
        setState({
          status: 'error',
          message: error instanceof ApiError ? error.message : 'Something went wrong.',
        })
      }
      return
    }

    setState({ status: 'ready', listing })

    void listingsApi
      .categoryById(listing.categoryId)
      .then(setCategory)
      .catch(() => setCategory(null))

    void usersApi
      .campusById(listing.campusId)
      .then(setCampus)
      .catch(() => setCampus(null))

    void listingsApi
      .imagesFor(id)
      .then(setImages)
      .catch(() => setImages([]))

    setSellerLoading(true)

    void usersApi
      .byId(listing.sellerId)
      .then(setSeller)
      .catch(() => setSeller(null))
      .finally(() => setSellerLoading(false))

    void usersApi
      .averageRating(listing.sellerId)
      .then(setRating)
      .catch(() => setRating(null))

    void usersApi
      .reviewsAbout(listing.sellerId)
      .then((reviews) => setReviewCount(reviews.length))
      .catch(() => setReviewCount(null))
  }, [])

  useEffect(() => {
    if (numericId === null) return
    Promise.resolve().then(() => load(numericId))
  }, [numericId, load])

  if (numericId === null) {
    return (
      <>
        <PageHeader title="Listing" />
        <EmptyState
          title="That doesn't look like a listing"
          description={`"${listingId}" isn't a valid listing ID.`}
        />
      </>
    )
  }

  if (state.status === 'loading') {
    return (
      <>
        <PageHeader title="Listing" />
        <div className="grid place-items-center py-16">
          <Spinner label="Loading listing" className="size-8" />
        </div>
      </>
    )
  }

  if (state.status === 'not-found') {
    return (
      <>
        <PageHeader title="Listing" subtitle={`Listing #${numericId}`} />
        <EmptyState
          title="Listing not found"
          description="This listing may have been removed or the link is out of date."
        />
      </>
    )
  }

  if (state.status === 'error') {
    return (
      <>
        <PageHeader title="Listing" subtitle={`Listing #${numericId}`} />
        <EmptyState
          title="Couldn't load this listing"
          description={state.message}
          action={
            <Button variant="ghost" onClick={() => void load(numericId)}>
              Try again
            </Button>
          }
        />
      </>
    )
  }

  const { listing } = state

  // Belt-and-suspenders: the buttons that call these are only rendered for
  // isOwner already, but that's a UI decision, not enforcement - the backend
  // currently accepts PATCH .../sold and DELETE from ANY authenticated user,
  // not just the seller (ListingController/ListingServiceImpl do not check
  // sellerId against the caller). Refusing here client-side closes nothing on
  // its own - anyone can still call the API directly - but it stops this
  // page from being the thing that fires an unauthorized request. The real
  // fix has to be a server-side ownership check.
  const isOwner = user?.userId === listing.sellerId

  const handleMarkSold = async () => {
    if (!isOwner) {
      setActionError('Only the seller can mark this listing as sold.')
      return
    }
    try {
      const updated = await listingsApi.markSold(listing.listingId)
      setState({ status: 'ready', listing: updated })
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : 'Could not mark this as sold.')
    }
  }

  const handleDelete = async () => {
    if (!isOwner) {
      setActionError('Only the seller can delete this listing.')
      return
    }
    try {
      await listingsApi.remove(listing.listingId)
      navigate('/feed')
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : 'Could not delete this listing.')
    }
  }

  return (
    <>
      <PageHeader
        title={listing.title}
        subtitle={category ? category.name : `Listing #${listing.listingId}`}
      />

      {actionError && (
        <div className="mb-4">
          <Alert>{actionError}</Alert>
        </div>
      )}

      <div className="grid gap-6 md:grid-cols-5">
        <div className="md:col-span-3">
          <ListingGallery images={images} title={listing.title} />
        </div>

        <div className="flex flex-col gap-4 md:col-span-2">
          <div className="rounded-2xl border border-gray-200 bg-white p-4">
            <div className="flex items-start justify-between gap-3">
              <p className="text-2xl font-semibold text-ink-900">
                {currencyFormatter.format(listing.price)}
              </p>
              <Badge tone={STATUS_TONE[listing.status]}>{listing.status}</Badge>
            </div>

            <dl className="mt-4 space-y-2 text-sm">
              {campus && (
                <div className="flex justify-between gap-4">
                  <dt className="text-ink-500">Campus</dt>
                  <dd className="text-right text-ink-700">{campus.name}</dd>
                </div>
              )}
              <div className="flex justify-between gap-4">
                <dt className="text-ink-500">Posted</dt>
                <dd className="text-ink-700">{dateFormatter.format(new Date(listing.createdAt))}</dd>
              </div>
            </dl>
          </div>

          <SellerCard
            seller={seller}
            loading={sellerLoading}
            rating={rating}
            reviewCount={reviewCount}
            showMessageAction={!isOwner}
            onMessage={() => navigate('/messages')}
          />

          {isOwner && (
            <ListingOwnerActions listing={listing} onMarkSold={handleMarkSold} onDelete={handleDelete} />
          )}
        </div>
      </div>

      <div className="mt-6 rounded-2xl border border-gray-200 bg-white p-4">
        <p className="mb-2 text-sm font-medium text-ink-700">Description</p>
        {listing.description ? (
          <p className="whitespace-pre-wrap text-sm text-ink-700">{listing.description}</p>
        ) : (
          <p className="text-sm text-ink-400 italic">No description provided.</p>
        )}
      </div>
    </>
  )
}