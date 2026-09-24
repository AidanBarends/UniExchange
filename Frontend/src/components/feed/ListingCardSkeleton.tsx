/*
  ListingCardSkeleton - pulsing placeholder shown while the feed's first load
  is in flight (the standard skeleton-loading pattern from Preline's catalog).

  Mirrors ListingCard's exact shape so the swap from skeleton to content does
  not jump. Purely decorative, hence aria-hidden.

  Owner: Joshua Reid Adams (230317693)
*/

export function ListingCardSkeleton() {
  return (
    <div
      aria-hidden="true"
      className="rounded-2xl border border-gray-200 bg-white p-3 shadow-sm"
    >
      <div className="aspect-[4/3] animate-pulse rounded-xl bg-gray-100" />
      <div className="mt-3 space-y-2.5">
        <div className="h-4 w-3/4 animate-pulse rounded bg-gray-100" />
        <div className="h-5 w-1/3 animate-pulse rounded bg-gray-100" />
        <div className="h-3 w-1/2 animate-pulse rounded bg-gray-100" />
      </div>
    </div>
  )
}
