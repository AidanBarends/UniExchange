/*
  Listings, categories and listing images.
  OWNERS: Aidan Barends 230255639 (product details) + Mogamat Wazeer Gilbert 221374698 (create listing)

  GET on listings, listing-images and categories is public; POST/PUT/DELETE need
  a token, which authedRequest attaches for you.

  Add whatever you need here - this file is yours, so you will not collide with
  anyone else's work.
*/

import { authedRequest, request } from './client'
import type { Category, Listing, ListingImage, ListingStatus } from './types'

/** The body POST /api/listings and PUT /api/listings/:id both expect. */
export type ListingInput = {
  sellerId: number
  categoryId: number
  campusId: number
  title: string
  description?: string | null
  price: number
  status: ListingStatus
}

export const listingsApi = {
  /** All listings. Public. */
  list: () => request<Listing[]>('/api/listings'),

  /**
   * Server-side search. Every parameter is optional and empty ones are dropped.
   * Backed by GET /api/listings/search on ListingController.
   */
  search: (filters: { campusId?: number; categoryId?: number; title?: string } = {}) =>
    request<Listing[]>('/api/listings/search', { query: filters }),

  byId: (listingId: number | string) => request<Listing>(`/api/listings/${listingId}`),

  bySeller: (sellerId: number) => request<Listing[]>(`/api/listings/seller/${sellerId}`),

  create: (body: ListingInput) => authedRequest<Listing>('/api/listings', { method: 'POST', body }),

  update: (listingId: number, body: ListingInput) =>
    authedRequest<Listing>(`/api/listings/${listingId}`, { method: 'PUT', body }),

  remove: (listingId: number) =>
    authedRequest<void>(`/api/listings/${listingId}`, { method: 'DELETE' }),

  /** PATCH /api/listings/:id/sold */
  markSold: (listingId: number) =>
    authedRequest<Listing>(`/api/listings/${listingId}/sold`, { method: 'PATCH' }),

  categories: () => request<Category[]>('/api/categories'),

  imagesFor: (listingId: number | string) =>
    request<ListingImage[]>(`/api/listing-images/listing/${listingId}`),

  /** NOTE: you send `isPrimary`, but the response comes back as `primary`. */
  addImage: (body: {
    listingId: number
    imageUrl: string
    position: number
    isPrimary: boolean
  }) => authedRequest<ListingImage>('/api/listing-images', { method: 'POST', body }),
}
