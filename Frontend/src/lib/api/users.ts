/*
  Users and profiles. OWNER: Raul Ja'aim Everts 230270565

  The signed-in user is already available from useAuth() - no request needed for
  your own profile header. Use these for OTHER people's profiles, and for the
  reviews and badge that make up someone's reputation.
*/

import { authedRequest } from './client'
import type { Campus, Review, TrustedSellerBadge, User } from './types'

export const usersApi = {
  byId: (userId: number | string) => authedRequest<User>(`/api/users/${userId}`),

  byEmail: (email: string) => authedRequest<User>(`/api/users/email/${email}`),

  campusById: (campusId: number) => authedRequest<Campus>(`/api/campuses/${campusId}`),

  reviewsAbout: (userId: number | string) =>
    authedRequest<Review[]>(`/api/reviews/reviewee/${userId}`),

  averageRating: (userId: number | string) =>
    authedRequest<number>(`/api/reviews/reviewee/${userId}/average`),

  /*
    404 is the normal response when the user has no badge — catch it in the
    component rather than treating it as an error.
  */
  trustedSellerBadge: (userId: number | string) =>
    authedRequest<TrustedSellerBadge>(`/api/trusted-seller-badges/user/${userId}`),
}