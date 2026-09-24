/*
  SafeExchangeCard - the "Campus Safe Exchange Zone" card below the live feed
  in the desktop mockup. Static copy for now; there's no "recommended
  meetup spot" concept in the backend yet.

  Owner: Joshua Reid Adams (230317693)
*/

import { Card } from "@/components/ui/Card";

export function SafeExchangeCard() {
  return (
    <Card className="p-4">
      <p className="flex items-center gap-1.5 text-sm font-semibold text-ink-900">
        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          className="size-4 shrink-0 text-brand-600"
        >
          <path d="M12 21s-7-5.5-7-11a7 7 0 1 1 14 0c0 5.5-7 11-7 11Z" />
          <circle cx="12" cy="10" r="2.5" />
        </svg>
        Campus Safe Exchange Zone
      </p>
      <p className="mt-1.5 text-xs text-ink-500">
        Trade in well-lit, campus-monitored locations. Recommended spots are
        shown here once set.
      </p>

      <div className="mt-3 rounded-xl border border-gray-200 bg-gray-50 p-3">
        <p className="text-sm font-medium text-ink-900">Recommended spot</p>
        <p className="mt-0.5 text-xs text-ink-500">
          Set per-campus once that data exists.
        </p>
      </div>

      <p className="mt-3 text-xs text-ink-400">
        Always test electronics before payment.
      </p>
    </Card>
  );
}
