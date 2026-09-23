type ButtonProps = React.ComponentProps<'button'> & {
  variant?: 'primary' | 'ghost'
  loading?: boolean
}

export function Button({
  variant = 'primary',
  loading = false,
  disabled,
  className = '',
  children,
  ...rest
}: ButtonProps) {
  const base =
    'inline-flex w-full items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition ' +
    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 ' +
    'disabled:cursor-not-allowed disabled:opacity-60'

  const tone =
    variant === 'primary'
      ? 'bg-brand-600 text-white hover:bg-brand-700 active:bg-brand-800'
      : 'bg-transparent text-brand-700 hover:bg-brand-50'

  return (
    <button {...rest} disabled={disabled || loading} className={`${base} ${tone} ${className}`}>
      {loading && (
        <svg aria-hidden="true" viewBox="0 0 24 24" className="size-4 animate-spin">
          <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" opacity="0.25" />
          <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" strokeWidth="4" fill="none" strokeLinecap="round" />
        </svg>
      )}
      {children}
    </button>
  )
}
