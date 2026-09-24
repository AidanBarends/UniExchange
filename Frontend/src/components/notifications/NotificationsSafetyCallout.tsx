/*
  NotificationsSafetyCallout - the footer card from the wireframe ("Campus
  Safety Commitment"), rebuilt with the same card language already
  established in components/feed/TrustCallout.tsx and SafeExchangeCard.tsx
  (icon + heading + one line + a small action) rather than the mock's own
  styling, so it reads as part of the same app.

  Static copy for now, same as its feed counterparts - there's no backend
  concept of "safe zones" to source real numbers from yet.

  OWNER: Joshua Reid Adams (230317693)
*/

import { useNavigate } from "react-router-dom";

import { Card } from "@/components/ui/Card";

export function NotificationsSafetyCallout() {
  const navigate = useNavigate();

  return (
    <Card className="flex flex-wrap items-center justify-between gap-4 p-4">
      <div className="flex items-start gap-3">
        <span className="grid size-9 shrink-0 place-items-center rounded-xl bg-emerald-50 text-emerald-700">
          <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            className="size-5"
          >
            <path
              d="M12 3 4 6v5c0 5 3.5 8.5 8 10 4.5-1.5 8-5 8-10V6l-8-3Z"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="m9 12 2 2 4-4"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </span>
        <div>
          <p className="text-sm font-semibold text-ink-900">
            Campus Safety Commitment
          </p>
          <p className="mt-0.5 text-xs text-ink-500">
            All buyers and sellers in your notification log are verified with
            university registration emails.
          </p>
        </div>
      </div>

      <button
        type="button"
        onClick={() => navigate("/bulletin")}
        className="shrink-0 text-sm font-medium text-brand-700 hover:text-brand-900"
      >
        View Safe Zones →
      </button>
    </Card>
  );
}
