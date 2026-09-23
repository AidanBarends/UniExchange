/*
  Homepage feed - what is for sale on campus.

  OWNER: Joshua Reid Adams (230317693)
  ROUTE: /feed   (this is where students land after signing in)

  TODO
   - listingsApi.list()                        GET /api/listings      (public)
   - listingsApi.categories()                  GET /api/categories    (public)
   - listingsApi.search({ campusId, categoryId, title })
                                               GET /api/listings/search
   - the signed-in student's campus is useAuth().user?.campusId - default the
     feed to their campus, that is the whole "hyper-local" point of the product
   - each card links to /listings/${listing.listingId}
   - reuse: PageHeader, Card, Badge, EmptyState, Spinner, Select
   - listing.price is a number; format with
     new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR' })

  Your own components go in src/components/feed/ so nobody collides with you.
*/

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function FeedPage() {
  return (
    <>
      <PageHeader title="Feed" subtitle="What's for sale on your campus" />

      <EmptyState
        title="The feed is not built yet"
        description="This route is wired and ready. Open src/pages/FeedPage.tsx - the TODO at the top lists the exact endpoints to call."
      />
    </>
  )
}
