/*
  Users and profiles. OWNER: Raul Ja'aim Everts 230270565

  The signed-in user is already available from useAuth() - no request needed for
  your own profile header. Use these for OTHER people's profiles, and for the
  reviews and badge that make up someone's reputation.
*/

import { authedRequest } from './client'
import type { Campus, User } from './types'

export const usersApi = {
  byId: (userId: number | string) => authedRequest<User>(`/api/users/${userId}`),

  byEmail: (email: string) => authedRequest<User>(`/api/users/email/${email}`),

  campusById: (campusId: number) => authedRequest<Campus>(`/api/campuses/${campusId}`),

  /* Reputation. Typed loosely for now - tighten these when you build the page.
     GET /api/reviews/reviewee/:userId          -> reviews written about a user
     GET /api/reviews/reviewee/:userId/average  -> a plain number
     GET /api/trusted-seller-badges/user/:userId -> 404 when they have no badge */
  reviewsAbout: (userId: number | string) =>
    authedRequest<unknown[]>(`/api/reviews/reviewee/${userId}`),

  averageRating: (userId: number | string) =>
    authedRequest<number>(`/api/reviews/reviewee/${userId}/average`),
}
