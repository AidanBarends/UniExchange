/*
  A single listing.

  OWNER: Aidan Barends (230255639)
  ROUTE: /listings/:listingId

  TODO
   - listingsApi.byId(listingId)               GET /api/listings/:id
   - listingsApi.imagesFor(listingId)          GET /api/listing-images/listing/:id
   - usersApi.byId(listing.sellerId)           the seller's name and reputation
   - usersApi.averageRating(listing.sellerId)  GET /api/reviews/reviewee/:id/average
   - "Message seller" should create/find a conversation - talk to whoever takes
     messaging so you agree on that flow rather than both building half of it
   - 404: listingsApi.byId throws ApiError with status 404; show a not-found
     state rather than a blank page
   - reuse: PageHeader, Card, Badge, Avatar, Spinner, Button

  NOTE: images come back with `primary`, not `isPrimary` - see the comment at the
  top of src/lib/api/types.ts for why.

  Your own components go in src/components/listings/.
*/

import { useParams } from 'react-router-dom'

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function ListingDetailsPage() {
  const { listingId } = useParams<{ listingId: string }>()

  return (
    <>
      <PageHeader title="Listing" subtitle={`Listing #${listingId}`} />

      <EmptyState
        title="Listing details are not built yet"
        description="The route and its :listingId param already work. Open src/pages/ListingDetailsPage.tsx for the endpoints."
      />
    </>
  )
}
