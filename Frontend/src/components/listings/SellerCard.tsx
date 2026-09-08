/*
  Who is selling this, and a way to reach them.

  Rating comes from usersApi.averageRating(), which returns 0 for a seller
  with no reviews yet (ReviewServiceImpl.averageRatingForUser defaults an
  empty stream to 0.0) - that's shown as "No ratings yet" rather than "0.0
  stars", since a bare 0 reads as a bad rating rather than an absent one.

  The mockup shows a "UCT Campus Resident" tagline and a green online-status
  dot next to the seller's name - neither exists in the backend (User has no
  online-status field, and there's no residency concept anywhere), so
  they're left out. The "Trusted Seller" checkmark IS real: it comes from
  GET /api/trusted-seller-badges/user/:id, a genuine badge system that just
  hadn't been wired into any page yet.

  "Message seller" does not create a conversation - messages.ts is unowned,
  and building that flow solo risks duplicating whoever picks up messaging.
  The button just hands off to /messages for now.

  "Share" is client-side only (Web Share API, falling back to clipboard) -
  no backend involved, so no honesty concerns there. The mockup's separate
  "Save/Favorite" button is skipped entirely: there's no favorites/wishlist
  entity anywhere in the backend to back it.

  Owner: Aidan Barends (230255639), for /listings/:listingId only.
*/

import { useState } from 'react'
import { Link } from 'react-router-dom'

import { Avatar } from '@/components/ui/Avatar'
import { Button } from '@/components/ui/Button'
import { Spinner } from '@/components/ui/Spinner'
import type { User } from '@/lib/api/types'

type SellerCardProps = {
  seller: User | null
  loading: boolean
  /** Number of reviews backing `rating`, so 0 reviews reads differently from a genuine 0.0 average. */
  reviewCount: number | null
  rating: number | null
  /** Null while loading/unknown, so the checkmark doesn't flash in incorrectly before we know. */
  trusted: boolean | null
  /** Hidden entirely when the viewer is the seller - you don't message yourself. */
  showMessageAction: boolean
  onMessage: () => void
  onShare: () => Promise<'shared' | 'copied' | 'cancelled'>
}

export function SellerCard({
  seller,
  loading,
  reviewCount,
  rating,
  trusted,
  showMessageAction,
  onMessage,
  onShare,
}: SellerCardProps) {
  const fullName = seller ? `${seller.firstName} ${seller.lastName}` : null
  const [shareLabel, setShareLabel] = useState('Share')

  const handleShare = async () => {
    const result = await onShare()
    if (result === 'copied') {
      setShareLabel('Link copied!')
      setTimeout(() => setShareLabel('Share'), 2000)
    }
  }

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
              <div className="flex items-center gap-1.5">
                <Link
                  to={`/profile/${seller.userId}`}
                  className="truncate text-sm font-semibold text-ink-900 hover:text-brand-700"
                >
                  {fullName}
                </Link>
                {trusted && (
                  <span title="Trusted Seller" aria-label="Trusted Seller">
                    <TrustedIcon className="size-4 shrink-0 text-brand-600" />
                  </span>
                )}
              </div>
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

      <div className="mt-4 flex gap-2">
        {showMessageAction && (
          <Button variant="primary" className="flex-1" onClick={onMessage}>
            Message Seller
          </Button>
        )}
        <Button variant="ghost" className={showMessageAction ? '' : 'flex-1'} onClick={() => void handleShare()}>
          {shareLabel}
        </Button>
      </div>
    </div>
  )
}

function TrustedIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className={className}>
      <path
        d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3z"
        fill="currentColor"
        opacity="0.15"
      />
      <path
        d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinejoin="round"
      />
      <path
        d="M9 12l2 2 4-4"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}