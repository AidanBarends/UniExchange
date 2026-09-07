/*
  Who is selling this.

  Rating comes from usersApi.averageRating(), which returns 0 for a seller
  with no reviews yet (ReviewServiceImpl.averageRatingForUser defaults an
  empty stream to 0.0) - that's shown as "No ratings yet" rather than "0.0
  stars", since a bare 0 reads as a bad rating rather than an absent one.

  Owner: Aidan Barends (230255639), for /listings/:listingId only.
*/

import { Link } from 'react-router-dom'

import { Avatar } from '@/components/ui/Avatar'
import { Spinner } from '@/components/ui/Spinner'
import type { User } from '@/lib/api/types'

type SellerCardProps = {
  seller: User | null
  loading: boolean
  /** Number of reviews backing `rating`, so 0 reviews reads differently from a genuine 0.0 average. */
  reviewCount: number | null
  rating: number | null
}

export function SellerCard({ seller, loading, reviewCount, rating }: SellerCardProps) {
  const fullName = seller ? `${seller.firstName} ${seller.lastName}` : null

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-4">
      <p className="mb-3 text-sm font-medium text-ink-700">Seller</p>

      {loading ? (
        <div className="flex items-center gap-2 py-2">
          <Spinner label="Loading seller" />
        </div>
      ) : (
        <div className="flex items-center gap-3">
          <Avatar name={fullName} className="size-12" />
          <div className="min-w-0 flex-1">
            {seller ? (
              <Link
                to={`/profile/${seller.userId}`}
                className="truncate text-sm font-semibold text-ink-900 hover:text-brand-700"
              >
                {fullName}
              </Link>
            ) : (
              <p className="text-sm font-semibold text-ink-500">Seller unavailable</p>
            )}
            <p className="text-xs text-ink-500">
              {reviewCount !== null && reviewCount > 0 && rating !== null
                ? `${rating.toFixed(1)} \u2605 (${reviewCount} review${reviewCount === 1 ? '' : 's'})`
                : 'No ratings yet'}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}