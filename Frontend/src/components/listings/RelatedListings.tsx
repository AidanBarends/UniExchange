/*
  "More like this" - a row of other listings in the same category on the same
  campus, shown at the bottom of the Product Details page.

  How "related" is decided: same categoryId AND same campusId, current listing
  excluded, only ACTIVE ones kept. That's the honest limit of what the backend
  offers - there's no recommendation endpoint, no tag/keyword similarity, no
  "people also viewed". GET /api/listings/search takes campusId + categoryId
  and nothing finer, so this is a category shelf, not a real recommender, and
  it's labelled as plain "More like this" rather than implying anything smarter.

  Thumbnails need a second call each (GET /api/listing-images/listing/:id) -
  there's no image on the Listing payload itself. Capped at MAX_RELATED so
  that's at most a handful of small requests.

  Renders nothing at all when there are no matches, so the page doesn't grow an
  empty "More like this" heading.

  Owner: Aidan Barends (230255639), for /listings/:listingId only.
*/

import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { Spinner } from '@/components/ui/Spinner'
import { listingsApi } from '@/lib/api/listings'
import type { Listing } from '@/lib/api/types'

const MAX_RELATED = 6

const currencyFormatter = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR' })

type RelatedListingsProps = {
  /** The listing being viewed - excluded from its own "more like this" row. */
  currentListingId: number
  categoryId: number
  campusId: number
}

type RelatedItem = Listing & { imageUrl: string | null }

type LoadState =
  | { status: 'loading' }
  | { status: 'done'; items: RelatedItem[] }

export function RelatedListings({ currentListingId, categoryId, campusId }: RelatedListingsProps) {
  const [state, setState] = useState<LoadState>({ status: 'loading' })

  useEffect(() => {
    let cancelled = false

    async function load() {
      setState({ status: 'loading' })

      let matches: Listing[]
      try {
        matches = await listingsApi.search({ campusId, categoryId })
      } catch {
        // A failed shelf is not worth an error banner on an otherwise-fine
        // page - just render nothing.
        if (!cancelled) setState({ status: 'done', items: [] })
        return
      }

      const shortlist = matches
        .filter((listing) => listing.listingId !== currentListingId && listing.status === 'ACTIVE')
        .slice(0, MAX_RELATED)

      const withImages = await Promise.all(
        shortlist.map(async (listing) => {
          try {
            const images = await listingsApi.imagesFor(listing.listingId)
            const chosen = images.find((image) => image.primary) ?? images[0]
            return { ...listing, imageUrl: chosen?.imageUrl ?? null }
          } catch {
            return { ...listing, imageUrl: null }
          }
        }),
      )

      if (!cancelled) setState({ status: 'done', items: withImages })
    }

    void Promise.resolve().then(load)
    return () => {
      cancelled = true
    }
  }, [currentListingId, categoryId, campusId])

  if (state.status === 'loading') {
    return (
      <section className="mt-8">
        <h2 className="mb-3 text-sm font-medium text-ink-700">More like this</h2>
        <div className="grid place-items-center py-8">
          <Spinner label="Loading related listings" />
        </div>
      </section>
    )
  }

  if (state.items.length === 0) return null

  return (
    <section className="mt-8">
      <h2 className="mb-3 text-sm font-medium text-ink-700">More like this</h2>

      <div className="-mx-4 flex gap-3 overflow-x-auto px-4 pb-2">
        {state.items.map((item) => (
          <Link
            key={item.listingId}
            to={`/listings/${item.listingId}`}
            className="block w-40 shrink-0 overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition hover:border-brand-300 hover:shadow focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
          >
            <div className="aspect-square w-full bg-gray-100">
              {item.imageUrl ? (
                <img
                  src={item.imageUrl}
                  alt={item.title}
                  className="size-full object-cover"
                  onError={(event) => {
                    event.currentTarget.style.display = 'none'
                  }}
                />
              ) : (
                <div className="grid size-full place-items-center text-ink-300">
                  <PhotoIcon className="size-8" />
                </div>
              )}
            </div>

            <div className="p-3">
              <p className="line-clamp-2 text-sm font-medium text-ink-900">{item.title}</p>
              <p className="mt-1 text-sm font-semibold text-brand-700">
                {currencyFormatter.format(item.price)}
              </p>
            </div>
          </Link>
        ))}
      </div>
    </section>
  )
}

function PhotoIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className={className}>
      <rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="8.5" cy="10" r="1.5" stroke="currentColor" strokeWidth="1.5" />
      <path
        d="M5 17l5-5 3 3 3-4 3 4"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
