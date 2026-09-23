type AlertProps = {
  tone?: 'error' | 'success' | 'info'
  children: React.ReactNode
}

const TONES = {
  error: 'border-red-200 bg-red-50 text-red-800',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  info: 'border-brand-200 bg-brand-50 text-brand-800',
} as const

export function Alert({ tone = 'error', children }: AlertProps) {
  return (
    <div
      role={tone === 'error' ? 'alert' : 'status'}
      className={`rounded-lg border px-3.5 py-3 text-sm ${TONES[tone]}`}
    >
      {children}
    </div>
  )
}
