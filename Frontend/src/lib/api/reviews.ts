/*
  Reviews and the Trusted Seller badge.

  A review can only be left by someone who completed a transaction with the
  person being rated, so `submit` takes the transaction rather than a reviewee:
  the backend works out who you are rating from who you traded with. There is
  nothing here to forge, which is what makes the badge derived from these ratings
  worth displaying.

  The two GETs are public (a seller's rating shows on every listing card, signed
  in or not); submitting requires a token.
*/

import { authedRequest, request } from './client'
import type { Review, Transaction, TrustedSellerBadge } from './types'

export const reviewsApi = {
  /** Every review written about a user, newest first. */
  about: (userId: number | string) => request<Review[]>(`/api/reviews/reviewee/${userId}`),

  /** Mean rating, or 0 when there are none yet. */
  averageFor: (userId: number | string) =>
    request<number>(`/api/reviews/reviewee/${userId}/average`),

  /** Completed transactions the signed-in student has not reviewed yet. */
  pending: () => authedRequest<Transaction[]>('/api/reviews/pending'),

  /** Rates the other party to a completed transaction. Rating is 1-5. */
  submit: (body: { transactionId: number; rating: number; comment?: string }) =>
    authedRequest<Review>('/api/reviews', { method: 'POST', body }),
}

export const badgeApi = {
  /**
   * The badge row for a user, or null when they have never earned one.
   *
   * Check `revokedAt` before showing anything: a revoked badge still returns a
   * row, and treating "row exists" as "trusted" would keep displaying the badge
   * after it was taken away.
   */
  forUser: async (userId: number | string): Promise<TrustedSellerBadge | null> => {
    try {
      return await request<TrustedSellerBadge>(`/api/trusted-seller-badges/user/${userId}`)
    } catch {
      // 404 is the normal answer for "no badge", not an error worth surfacing.
      return null
    }
  },
}

/** True only for a badge that exists and has not been revoked. */
export function isTrustedSeller(badge: TrustedSellerBadge | null): boolean {
  return badge !== null && badge.revokedAt === null
}
