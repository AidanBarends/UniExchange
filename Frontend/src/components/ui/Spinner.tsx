/* Loading indicator. `label` is announced to screen readers, not drawn. */

type SpinnerProps = {
  label?: string
  className?: string
}

export function Spinner({ label = 'Loading', className = 'size-6' }: SpinnerProps) {
  return (
    <span role="status" aria-live="polite" className="inline-flex items-center gap-2 text-ink-500">
      <svg aria-hidden="true" viewBox="0 0 24 24" className={`${className} animate-spin`}>
        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" opacity="0.25" />
        <path
          d="M12 2a10 10 0 0 1 10 10"
          stroke="currentColor"
          strokeWidth="4"
          fill="none"
          strokeLinecap="round"
        />
      </svg>
      <span className="sr-only">{label}</span>
    </span>
  )
}
