/*
  A single listing.

  OWNER: Aidan Barends (230255639)
  ROUTE: /listings/:listingId

  Restyled to match the team's Product Details mockup. A few things in that
  mockup don't have backend support and are deliberately left out rather than
  faked - see the comments in SellerCard.tsx and ListingGallery.tsx for the
  specifics (condition badge, discounted price, favorites, seller
  online-status, a real map). The "Location" section here is a text card
  (campus name + city), not a map with a pin, since Campus has no
  coordinates.

  NOTE: images come back with `primary`, not `isPrimary` - see the comment at the
  top of src/lib/api/types.ts for why.

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
import { Card } from '@/components/ui/Card'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { ApiError, authedRequest } from '@/lib/api/client'
import { listingsApi } from '@/lib/api/listings'
import type { Campus, Category, Listing, ListingImage, ListingStatus, User } from '@/lib/api/types'
import { usersApi } from '@/lib/api/users'

const currencyFormatter = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR' })
const absoluteDateFormatter = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'medium' })

const STATUS_TONE: Record<ListingStatus, 'success' | 'neutral' | 'warning' | 'danger'> = {
  ACTIVE: 'success',
  SOLD: 'neutral',
  REMOVED: 'warning',
  DELETED: 'danger',
}

/** "Listed 2 hours ago" - matches the mockup's relative-time style. Falls back
 * to an absolute date once something is more than a week old, since "47 days
 * ago" is less useful than just reading the date at that point. */
function formatRelativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime()
  const diffMin = Math.floor(diffMs / 60_000)

  if (diffMin < 1) return 'Just now'
  if (diffMin < 60) return `${diffMin} minute${diffMin === 1 ? '' : 's'} ago`

  const diffHr = Math.floor(diffMin / 60)
  if (diffHr < 24) return `${diffHr} hour${diffHr === 1 ? '' : 's'} ago`

  const diffDay = Math.floor(diffHr / 24)
  if (diffDay < 7) return `${diffDay} day${diffDay === 1 ? '' : 's'} ago`

  return absoluteDateFormatter.format(new Date(iso))
}

// Not exposed by usersApi.ts (users.ts is Raul's file, not touched here) -
// just the one field this page actually needs from a real endpoint:
// GET /api/trusted-seller-badges/user/:id, 404 when the seller has none.
// findByUserId on the backend does NOT filter out revoked badges, so
// revokedAt has to be checked here rather than trusting a 200 alone.
type TrustedSellerBadgeResponse = { revokedAt: string | null }

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
  const [trusted, setTrusted] = useState<boolean | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const load = useCallback(async (id: number) => {
    setState({ status: 'loading' })
    setActionError(null)

    // Reset supporting detail from any previously-loaded listing so a
    // direct navigation between two listings never flashes stale data.
    setCategory(null)
    setCampus(null)
    setImages([])
    setSeller(null)
    setSellerLoading(true)
    setRating(null)
    setReviewCount(null)
    setTrusted(null)

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

    // Everything below is supporting detail - if one of these fails, the
    // page still shows the listing itself rather than falling back to an
    // error, it just shows that one field blank.
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

    void authedRequest<TrustedSellerBadgeResponse>(`/api/trusted-seller-badges/user/${listing.sellerId}`)
      .then((badge) => setTrusted(badge.revokedAt === null))
      .catch(() => setTrusted(false))
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
  const isOwner = user?.userId === listing.sellerId

  // Belt-and-suspenders: the buttons that call these are only rendered for
  // isOwner already, but that's a UI decision, not enforcement - the backend
  // currently accepts PATCH .../sold and DELETE from ANY authenticated user,
  // not just the seller (ListingController/ListingServiceImpl do not check
  // sellerId against the caller). Refusing here client-side closes nothing on
  // its own - anyone can still call the API directly - but it stops this
  // page from being the thing that fires an unauthorized request. The real
  // fix has to be a server-side ownership check.
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

  const handleShare = async (): Promise<'shared' | 'copied' | 'cancelled'> => {
    const url = window.location.href
    if (navigator.share) {
      try {
        await navigator.share({ title: listing.title, url })
        return 'shared'
      } catch {
        // AbortError when the user just closes the native share sheet - not
        // an error worth surfacing.
        return 'cancelled'
      }
    }
    await navigator.clipboard.writeText(url)
    return 'copied'
  }

  return (
    <>
      <PageHeader
        title={listing.title}
        subtitle={category ? category.name : `Listing #${listing.listingId}`}
        action={
          <Button variant="ghost" onClick={() => navigate('/feed')}>
            Back to feed
          </Button>
        }
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
          <Card>
            <div className="flex flex-wrap items-center gap-2">
              {category && <Badge tone="brand">{category.name}</Badge>}
              <Badge tone={STATUS_TONE[listing.status]}>{listing.status}</Badge>
            </div>

            <p className="mt-3 text-2xl font-semibold text-brand-700">
              {currencyFormatter.format(listing.price)}
            </p>
            <p className="mt-1 text-xs text-ink-500">
              Listed {formatRelativeTime(listing.createdAt)}
            </p>
          </Card>

          <SellerCard
            seller={seller}
            loading={sellerLoading}
            rating={rating}
            reviewCount={reviewCount}
            trusted={trusted}
            showMessageAction={!isOwner}
            onMessage={() => navigate('/messages')}
            onShare={handleShare}
          />

          {isOwner && (
            <ListingOwnerActions listing={listing} onMarkSold={handleMarkSold} onDelete={handleDelete} />
          )}
        </div>
      </div>

      <Card className="mt-6">
        <p className="mb-2 text-sm font-medium text-ink-700">Description</p>
        {listing.description ? (
          <p className="whitespace-pre-wrap text-sm text-ink-700">{listing.description}</p>
        ) : (
          <p className="text-sm text-ink-400 italic">No description provided.</p>
        )}
      </Card>

      {campus && (
        <Card className="mt-6">
          <p className="mb-2 text-sm font-medium text-ink-700">Location</p>
          <div className="flex items-start gap-2 text-sm text-ink-700">
            <LocationIcon className="mt-0.5 size-4 shrink-0 text-ink-400" />
            <div>
              <p>{campus.name}</p>
              <p className="text-ink-500">{campus.city}</p>
            </div>
          </div>
        </Card>
      )}
    </>
  )
}

function LocationIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className={className}>
      <path
        d="M12 21s-7-6.1-7-11.5A7 7 0 0 1 19 9.5C19 14.9 12 21 12 21z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinejoin="round"
      />
      <circle cx="12" cy="9.5" r="2.25" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  )
}