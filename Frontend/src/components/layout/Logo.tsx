/*
  Wordmark. The project has no brand asset yet, so this is type plus a simple
  exchange glyph rather than a placeholder image.
*/

export function Logo({ className = '' }: { className?: string }) {
  return (
    <div className={`flex items-center gap-2.5 ${className}`}>
      <span
        aria-hidden="true"
        className="grid size-9 place-items-center rounded-xl bg-brand-600 text-white shadow-sm"
      >
        <svg viewBox="0 0 24 24" fill="none" className="size-5">
          <path
            d="M4 8h13l-3-3M20 16H7l3 3"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </span>
      <span className="text-lg font-semibold tracking-tight text-ink-900">
        Uni<span className="text-brand-600">Exchange</span>
      </span>
    </div>
  )
}
