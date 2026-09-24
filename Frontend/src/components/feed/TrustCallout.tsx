/*
  TrustCallout - the small highlighted card at the bottom of the left rail
  in the desktop mockup ("Verified Student Network"). Static content for now;
  swap the copy for whatever verification claim is actually true.

  Owner: Joshua Reid Adams (230317693)
*/

export function TrustCallout() {
  return (
    <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
      <p className="flex items-center gap-1.5 text-sm font-semibold text-emerald-800">
        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.75"
          className="size-4 shrink-0"
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
        Verified Student Network
      </p>
      <p className="mt-1.5 text-xs text-emerald-700">
        All active sellers are verified with university credentials for campus
        safety.
      </p>
    </div>
  );
}
