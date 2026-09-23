/*
  ListingCard - one item in the feed grid (T2 mockup: "2 Marketplace Feed").

  Anatomy matches the desktop mockup:
    image tile -> [category badge top-left] [favorite heart top-right]
    category label + time-ago line
    title
    description snippet (2 lines)
    price
    seller row: avatar + name + subtext

  ASSUMPTION FLAGGED: `listing.description` and `listing.sellerName` are used
  below for the snippet and seller row. If those aren't real fields on
  `Listing` yet, this renders the fallbacks shown - swap in the real field
  names (or wire a sellers/{id} lookup) once you confirm what's on the DTO.

  Image area is still a placeholder for v1; per-listing images cost one
  request per card, added in v1.5.

  Owner: Joshua Reid Adams (230317693)
*/

import { useState } from "react";

import { Avatar } from "@/components/ui/Avatar";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import type { Listing } from "@/lib/api/types";

type ListingCardProps = {
  listing: Listing;
  /** Resolved campus name for the location line; falls back to a generic label. */
  campusName?: string;
};

const zar = new Intl.NumberFormat("en-ZA", {
  style: "currency",
  currency: "ZAR",
});
const DAY_MS = 24 * 60 * 60 * 1000;
/* Evaluated once per page load - render stays pure (react-hooks/purity). */
const NOW_MS = Date.now();

function timeAgo(iso: string): string {
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  const weeks = Math.floor(days / 7);
  if (weeks < 5) return `${weeks}w ago`;
  return new Date(iso).toLocaleDateString("en-ZA", {
    day: "numeric",
    month: "short",
  });
}

export function ListingCard({ listing, campusName }: ListingCardProps) {
  const isNew = NOW_MS - new Date(listing.createdAt).getTime() < DAY_MS;
  const isSold = listing.status === "SOLD";
  const [favorited, setFavorited] = useState(false);

  // See ASSUMPTION FLAGGED note above - these two fields may not exist yet.
  const description = (listing as { description?: string }).description;
  const sellerName = (listing as { sellerName?: string }).sellerName;

  return (
    <Card
      to={`/listings/${listing.listingId}`}
      className="group flex h-full flex-col gap-2 p-3 transition-all hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="relative overflow-hidden rounded-xl bg-gray-100">
        <div className="flex aspect-[4/3] items-center justify-center text-gray-400 transition-transform duration-300 group-hover:scale-105">
          <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            className="size-10"
          >
            <rect x="3" y="4" width="18" height="16" rx="2" />
            <circle cx="9" cy="10" r="1.5" />
            <path
              d="m5 18 4.5-5 3 3.5L15 13l4 5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>

        {/* Top-left status/category badge, matching the mock's tinted tag. */}
        <span className="absolute left-2 top-2">
          {isSold ? (
            <Badge tone="neutral">SOLD</Badge>
          ) : isNew ? (
            <Badge tone="success">Just listed</Badge>
          ) : null}
        </span>

        {/* Top-right favorite toggle, visual only for now. */}
        <button
          type="button"
          onClick={(event) => {
            event.preventDefault();
            setFavorited((value) => !value);
          }}
          aria-pressed={favorited}
          aria-label={favorited ? "Remove from favorites" : "Add to favorites"}
          className="absolute right-2 top-2 grid size-7 place-items-center rounded-full bg-white/90 text-ink-500 shadow-sm transition hover:text-brand-600"
        >
          <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
            fill={favorited ? "currentColor" : "none"}
            stroke="currentColor"
            strokeWidth="1.75"
            className={`size-4 ${favorited ? "text-brand-600" : ""}`}
          >
            <path
              d="M12 20s-6.5-4.1-9-8.2C1.2 8.7 2.4 5 6 5c2 0 3.4 1 4 2.3C10.6 6 12 5 14 5c3.6 0 4.8 3.7 3 6.8-2.5 4.1-9 8.2-9 8.2Z"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      </div>

      <p className="text-xs text-ink-400">
        {campusName ?? "On campus"} <span aria-hidden="true">•</span>{" "}
        {timeAgo(listing.createdAt)}
      </p>

      <h3 className="line-clamp-2 text-sm font-semibold text-ink-900">
        {listing.title}
      </h3>

      {description && (
        <p className="line-clamp-2 text-xs text-ink-500">{description}</p>
      )}

      <p className="text-base font-bold text-brand-700">
        {zar.format(listing.price)}
      </p>

      <div className="mt-auto flex items-center gap-2 pt-1">
        <Avatar name={sellerName} className="size-6 shrink-0" />
        <span className="truncate text-xs font-medium text-ink-700">
          {sellerName ?? "Seller"}
        </span>
      </div>
    </Card>
  );
}
