/*
  Hero image + thumbnail strip for a listing.

  Images come from listingsApi.imagesFor(), already ordered by position on the
  backend (findByListingIdOrderByPositionAsc), so this trusts that order and
  only re-picks the one flagged `primary` to open on.

  Matches the team's Product Details mockup: up to VISIBLE_THUMBNAILS shown
  normally, the rest collapsed behind a "+N" tile that expands the strip when
  tapped (mockup shows a static "+3" - this makes it functional instead of
  decorative, since collapsing photos with no way to reach them would be
  worse than not collapsing at all).

  Owner: Aidan Barends (230255639), for /listings/:listingId only.
*/

import { useState } from 'react'

import type { ListingImage } from '@/lib/api/types'

const VISIBLE_THUMBNAILS = 2

type ListingGalleryProps = {
  images: ListingImage[]
  /** Used for alt text; there is no per-image caption in the domain. */
  title: string
}

export function ListingGallery({ images, title }: ListingGalleryProps) {
  const primaryIndex = Math.max(
    0,
    images.findIndex((image) => image.primary),
  )
  const [activeIndex, setActiveIndex] = useState(primaryIndex)
  const [expanded, setExpanded] = useState(false)

  if (images.length === 0) {
    return (
      <div className="grid aspect-[4/3] w-full place-items-center rounded-2xl border border-dashed border-gray-300 bg-gray-50 text-ink-400">
        <div className="text-center">
          <PhotoIcon className="mx-auto size-10" />
          <p className="mt-2 text-sm">No photos yet</p>
        </div>
      </div>
    )
  }

  const active = images[Math.min(activeIndex, images.length - 1)]
  const hiddenCount = images.length - VISIBLE_THUMBNAILS
  const showOverflowTile = !expanded && images.length > VISIBLE_THUMBNAILS + 1
  const thumbnails = showOverflowTile ? images.slice(0, VISIBLE_THUMBNAILS) : images

  return (
    <div>
      <div className="aspect-[4/3] w-full overflow-hidden rounded-2xl border border-gray-200 bg-gray-100">
        <img
          src={active.imageUrl}
          alt={title}
          className="size-full object-cover"
          onError={(event) => {
            event.currentTarget.style.display = 'none'
          }}
        />
      </div>

      {images.length > 1 && (
        <div className="mt-3 flex gap-2 overflow-x-auto pb-1">
          {thumbnails.map((image, index) => (
            <button
              key={image.imageId}
              type="button"
              onClick={() => setActiveIndex(index)}
              aria-label={`Show photo ${index + 1} of ${images.length}`}
              aria-pressed={index === activeIndex}
              className={`size-16 shrink-0 overflow-hidden rounded-lg border-2 transition ${
                index === activeIndex
                  ? 'border-brand-600'
                  : 'border-transparent opacity-80 hover:opacity-100'
              }`}
            >
              <img src={image.imageUrl} alt="" className="size-full object-cover" />
            </button>
          ))}

          {showOverflowTile && (
            <button
              type="button"
              onClick={() => setExpanded(true)}
              aria-label={`Show ${hiddenCount} more photos`}
              className="grid size-16 shrink-0 place-items-center rounded-lg bg-ink-900/80 text-sm font-semibold text-white transition hover:bg-ink-900"
            >
              +{hiddenCount}
            </button>
          )}
        </div>
      )}
    </div>
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