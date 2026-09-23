/*
  ConditionFilter - the "Condition Filter" card from the desktop mockup:
  a pill toggle group (Any / Brand New / Like New / Fair / Good) plus a
  min/max price range.

  STRUCTURE ONLY FOR NOW: state here is local and does not touch the search
  request in FeedPage. Listing/ListingRequest has no `condition` field on the
  backend yet, so there's nothing to filter by. Once that field exists, lift
  this state up to FeedPage the same way categoryId/campusId already work
  and pass it into listingsApi.search(...).

  Owner: Joshua Reid Adams (230317693)
*/

import { useState } from "react";

import { Card } from "@/components/ui/Card";

const CONDITIONS = ["Any", "Brand New", "Like New", "Fair / Good"] as const;

const PILL_BASE =
  "rounded-full border px-3 py-1.5 text-sm font-medium transition " +
  "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600";
const PILL_ACTIVE = "border-brand-600 bg-brand-600 text-white";
const PILL_IDLE =
  "border-gray-300 bg-white text-ink-700 hover:border-brand-300";

const MAX_PRICE = 10_000;

export function ConditionFilter() {
  const [condition, setCondition] =
    useState<(typeof CONDITIONS)[number]>("Any");
  const [maxPrice, setMaxPrice] = useState(MAX_PRICE);

  return (
    <Card className="p-4">
      <p className="text-xs font-semibold uppercase tracking-wide text-ink-400">
        Condition Filter
      </p>

      <div className="mt-3 flex flex-wrap gap-2">
        {CONDITIONS.map((label) => (
          <button
            key={label}
            type="button"
            onClick={() => setCondition(label)}
            aria-pressed={condition === label}
            className={`${PILL_BASE} ${condition === label ? PILL_ACTIVE : PILL_IDLE}`}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="mt-4">
        <div className="flex items-center justify-between text-sm">
          <span className="text-ink-700">Max Price:</span>
          <span className="font-semibold text-ink-900">
            {maxPrice >= MAX_PRICE
              ? `R ${MAX_PRICE.toLocaleString("en-ZA")}+`
              : `R ${maxPrice.toLocaleString("en-ZA")}`}
          </span>
        </div>
        <input
          type="range"
          min={0}
          max={MAX_PRICE}
          step={50}
          value={maxPrice}
          onChange={(event) => setMaxPrice(Number(event.target.value))}
          className="mt-2 w-full accent-brand-600"
          aria-label="Maximum price"
        />
        <div className="mt-1 flex justify-between text-xs text-ink-400">
          <span>R 0</span>
          <span>R {MAX_PRICE.toLocaleString("en-ZA")}+</span>
        </div>
      </div>
    </Card>
  );
}
