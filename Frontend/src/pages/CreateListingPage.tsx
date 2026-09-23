/*
  Post something for sale.

  OWNER: Mogamat Wazeer Gilbert (221374698)
  ROUTE: /listings/new

  TODO
   - listingsApi.categories()                  GET /api/categories
   - listingsApi.create({ sellerId, categoryId, campusId, title, description,
                          price, status: 'ACTIVE' })
                                               POST /api/listings
   - sellerId and campusId both come from useAuth().user - do not ask the user
   - listingsApi.addImage({ listingId, imageUrl, position, isPrimary })
     POST /api/listing-images. There is NO file upload on the backend: it stores
     a URL string, so take a URL for now
   - on success: navigate(`/listings/${created.listingId}`)
   - build the form with react-hook-form + zod, exactly like SignUpPage does.
     Put the schema in src/lib/schemas.ts next to signUpSchema
   - the backend factory rejects a blank title and a negative price with 400 and
     a `fields` map - surface those the way SignUpPage does
   - reuse: PageHeader, TextField, Textarea, Select, Button, Alert

  Your own components go in src/components/listings/.
*/

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function CreateListingPage() {
  return (
    <>
      <PageHeader title="Sell something" subtitle="List an item for your campus" />

      <EmptyState
        title="The create-listing form is not built yet"
        description="Open src/pages/CreateListingPage.tsx - the TODO lists the endpoints and which shared inputs to use."
      />
    </>
  )
}
