/*
  A student's profile and reputation.

  OWNER: Raul Ja'aim Everts (230270565)
  ROUTES: /profile            -> the signed-in student (no param needed)
          /profile/:userId    -> somebody else's profile

  One component serves both routes. When useParams().userId is undefined you are
  looking at yourself, and useAuth().user already holds that data — no extra
  request needed for the header. The reputation data (listings, reviews, badge)
  always has to be fetched regardless of whose profile it is.
*/

import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'

import { useAuth } from '@/auth/useAuth'
import { PageHeader } from '@/components/layout/PageHeader'
import { Avatar } from '@/components/ui/Avatar'
import { Badge } from '@/components/ui/Badge'
import { Card } from '@/components/ui/Card'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { ApiError } from '@/lib/api/client'
import { listingsApi } from '@/lib/api/listings'
import type { Listing, Review, TrustedSellerBadge, User } from '@/lib/api/types'
import { usersApi } from '@/lib/api/users'

const ZAR = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR' })

/* ── star display ─────────────────────────────────────────────────────────── */

function StarRating({ value }: { value: number }) {
  return (
    <span className="flex items-center gap-0.5" aria-label={`${value.toFixed(1)} out of 5 stars`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <svg
          key={n}
          viewBox="0 0 20 20"
          className={`size-4 ${n <= Math.round(value) ? 'text-amber-400' : 'text-gray-200'}`}
          fill="currentColor"
          aria-hidden="true"
        >
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
      ))}
    </span>
  )
}

/* ── profile header card ──────────────────────────────────────────────────── */

type ProfileHeaderProps = {
  user: User
  roles: string[]
  badge: TrustedSellerBadge | null
  avgRating: number | null
  reviewCount: number
  activeListings: number
  soldListings: number
}

function ProfileHeader({
  user,
  roles,
  badge,
  avgRating,
  reviewCount,
  activeListings,
  soldListings,
}: ProfileHeaderProps) {
  const fullName = `${user.firstName} ${user.lastName}`
  const isBadgeActive = badge !== null && badge.revokedAt === null

  return (
    <Card className="mb-4">
      {/* top row: avatar + identity */}
      <div className="flex items-start gap-4">
        <Avatar name={fullName} className="size-16 shrink-0 text-lg" />

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-base font-semibold text-ink-900 truncate">{fullName}</p>
            {isBadgeActive && (
              <Badge tone="success">✓ Trusted Seller</Badge>
            )}
          </div>

          <p className="mt-0.5 truncate text-sm text-ink-500">{user.email}</p>

          <div className="mt-2 flex flex-wrap gap-1.5">
            <Badge tone={user.accountStatus === 'ACTIVE' ? 'success' : 'warning'}>
              {user.accountStatus}
            </Badge>
            {roles.map((role) => (
              <Badge key={role} tone="brand">
                {role.replace('ROLE_', '')}
              </Badge>
            ))}
          </div>
        </div>
      </div>

      {/* stats row */}
      <div className="mt-4 grid grid-cols-3 divide-x divide-gray-100 rounded-xl border border-gray-100 bg-gray-50">
        <div className="flex flex-col items-center py-3 px-2">
          {avgRating !== null ? (
            <>
              <StarRating value={avgRating} />
              <p className="mt-1 text-xs text-ink-500">
                {avgRating.toFixed(1)} ({reviewCount})
              </p>
            </>
          ) : (
            <p className="text-xs text-ink-400">No reviews yet</p>
          )}
        </div>

        <div className="flex flex-col items-center py-3">
          <p className="text-lg font-bold text-ink-900">{soldListings}</p>
          <p className="text-xs text-ink-500">Sold</p>
        </div>

        <div className="flex flex-col items-center py-3">
          <p className="text-lg font-bold text-ink-900">{activeListings}</p>
          <p className="text-xs text-ink-500">Active</p>
        </div>
      </div>
    </Card>
  )
}

/* ── listing card ─────────────────────────────────────────────────────────── */

function ListingCard({ listing }: { listing: Listing }) {
  return (
    <Card to={`/listings/${listing.listingId}`} className="flex flex-col gap-1">
      <p className="text-sm font-medium text-ink-900 line-clamp-2">{listing.title}</p>
      <p className="mt-auto text-sm font-semibold text-brand-700">{ZAR.format(listing.price)}</p>
      <Badge
        tone={
          listing.status === 'ACTIVE'
            ? 'success'
            : listing.status === 'SOLD'
            ? 'neutral'
            : 'warning'
        }
      >
        {listing.status}
      </Badge>
    </Card>
  )
}

/* ── review row ───────────────────────────────────────────────────────────── */

function ReviewRow({ review }: { review: Review }) {
  const date = new Date(review.createdAt).toLocaleDateString('en-ZA', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })

  return (
    <div className="flex flex-col gap-1 border-b border-gray-100 py-3 last:border-0">
      <div className="flex items-center gap-2">
        <StarRating value={review.rating} />
        <span className="ml-auto text-xs text-ink-400">{date}</span>
      </div>
      {review.comment && (
        <p className="text-sm text-ink-700">{review.comment}</p>
      )}
    </div>
  )
}

/* ── page ─────────────────────────────────────────────────────────────────── */

export function ProfilePage() {
  const { userId: userIdParam } = useParams<{ userId?: string }>()
  const { user: authUser, session, loadingUser } = useAuth()

  const isOwnProfile = userIdParam === undefined

  /*
    The resolved user. For own profile this comes straight from auth context.
    For other profiles we fetch it. `null` means we are still loading.
  */
  const [profileUser, setProfileUser] = useState<User | null>(
    isOwnProfile ? authUser : null,
  )
  const [profileLoading, setProfileLoading] = useState(!isOwnProfile)
  const [profileError, setProfileError] = useState<string | null>(null)

  /* reputation */
  const [listings, setListings] = useState<Listing[]>([])
  const [reviews, setReviews] = useState<Review[]>([])
  const [avgRating, setAvgRating] = useState<number | null>(null)
  const [badge, setBadge] = useState<TrustedSellerBadge | null>(null)
  const [repLoading, setRepLoading] = useState(true)

  /* keep profileUser in sync when auth finishes loading on own profile */
  useEffect(() => {
    if (isOwnProfile && authUser) {
      setProfileUser(authUser)
    }
  }, [isOwnProfile, authUser])

  /* fetch other user */
  useEffect(() => {
    if (isOwnProfile || !userIdParam) return

    setProfileLoading(true)
    setProfileError(null)

    usersApi
      .byId(userIdParam)
      .then((u) => setProfileUser(u))
      .catch(() => setProfileError('Could not load this profile.'))
      .finally(() => setProfileLoading(false))
  }, [isOwnProfile, userIdParam])

  /* fetch reputation data once we know the userId */
  useEffect(() => {
    const resolvedId = isOwnProfile
      ? session?.userId
      : userIdParam ? Number(userIdParam) : undefined

    if (resolvedId === undefined) return

    setRepLoading(true)

    Promise.allSettled([
      listingsApi.bySeller(resolvedId as number),
      usersApi.reviewsAbout(resolvedId),
      usersApi.averageRating(resolvedId),
      usersApi.trustedSellerBadge(resolvedId),
    ]).then(([listingsRes, reviewsRes, ratingRes, badgeRes]) => {
      if (listingsRes.status === 'fulfilled') setListings(listingsRes.value)
      if (reviewsRes.status === 'fulfilled') setReviews(reviewsRes.value)
      if (ratingRes.status === 'fulfilled') setAvgRating(ratingRes.value)

      if (badgeRes.status === 'fulfilled') {
        setBadge(badgeRes.value)
      } else if (
        badgeRes.reason instanceof ApiError &&
        badgeRes.reason.status === 404
      ) {
        setBadge(null) // no badge — normal, not an error
      }

      setRepLoading(false)
    })
  }, [isOwnProfile, userIdParam, session?.userId])

  /* ── loading / error states ── */

  if (profileLoading || (isOwnProfile && loadingUser)) {
    return (
      <>
        <PageHeader title="Profile" />
        <div className="flex justify-center py-16">
          <Spinner label="Loading profile" className="size-8" />
        </div>
      </>
    )
  }

  if (profileError || !profileUser) {
    return (
      <>
        <PageHeader title="Profile" />
        <EmptyState
          title="Profile not found"
          description={profileError ?? 'This user does not exist or could not be loaded.'}
        />
      </>
    )
  }

  /* ── derived values ── */

  const roles = isOwnProfile ? (session?.roles ?? []) : []
  const activeListings = listings.filter((l) => l.status === 'ACTIVE')
  const soldListings = listings.filter((l) => l.status === 'SOLD')
  const fullName = `${profileUser.firstName} ${profileUser.lastName}`

  /* ── render ── */

  return (
    <>
      <PageHeader
        title={isOwnProfile ? 'Your profile' : fullName}
        subtitle={isOwnProfile ? 'How other students see you' : `Student #${profileUser.userId}`}
      />

      <ProfileHeader
        user={profileUser}
        roles={roles}
        badge={badge}
        avgRating={avgRating}
        reviewCount={reviews.length}
        activeListings={activeListings.length}
        soldListings={soldListings.length}
      />

      {/* listings */}
      <section className="mb-6">
        <h2 className="mb-3 text-sm font-semibold text-ink-700">
          {isOwnProfile ? 'Your listings' : 'Listings'}
        </h2>

        {repLoading ? (
          <div className="flex justify-center py-8">
            <Spinner label="Loading listings" />
          </div>
        ) : listings.length === 0 ? (
          <EmptyState
            title="No listings yet"
            description={
              isOwnProfile
                ? 'Post something to start selling on campus.'
                : 'This student has not listed anything yet.'
            }
          />
        ) : (
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            {listings.map((listing) => (
              <ListingCard key={listing.listingId} listing={listing} />
            ))}
          </div>
        )}
      </section>

      {/* reviews */}
      <section>
        <h2 className="mb-3 text-sm font-semibold text-ink-700">
          Reviews ({reviews.length})
        </h2>

        {repLoading ? (
          <div className="flex justify-center py-8">
            <Spinner label="Loading reviews" />
          </div>
        ) : reviews.length === 0 ? (
          <EmptyState
            title="No reviews yet"
            description={
              isOwnProfile
                ? 'Complete a transaction and buyers can leave you a review.'
                : 'This student has not received any reviews yet.'
            }
          />
        ) : (
          <Card>
            {reviews.map((review) => (
              <ReviewRow key={review.reviewId} review={review} />
            ))}
          </Card>
        )}
      </section>
    </>
  )
}