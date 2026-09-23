/* Multi-line input matching TextField. Needed by create-listing and bulletin. */

import { forwardRef } from 'react'

type TextareaProps = React.ComponentProps<'textarea'> & {
  label: string
  error?: string
  hint?: string
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { label, error, hint, id, className = '', rows = 4, ...rest },
  ref,
) {
  const fieldId = id ?? rest.name
  const describedBy = error ? `${fieldId}-error` : hint ? `${fieldId}-hint` : undefined

  return (
    <div className="space-y-1.5">
      <label htmlFor={fieldId} className="block text-sm font-medium text-ink-700">
        {label}
      </label>

      <textarea
        {...rest}
        id={fieldId}
        ref={ref}
        rows={rows}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        className={
          'block w-full rounded-lg border bg-white px-3 py-2.5 text-sm text-ink-900 ' +
          'placeholder:text-ink-400 focus:outline-2 focus:outline-offset-0 ' +
          (error
            ? 'border-red-300 focus:outline-red-500 '
            : 'border-gray-300 focus:outline-brand-600 ') +
          className
        }
      />

      {error ? (
        <p id={`${fieldId}-error`} className="text-xs text-red-600">
          {error}
        </p>
      ) : hint ? (
        <p id={`${fieldId}-hint`} className="text-xs text-ink-400">
          {hint}
        </p>
      ) : null}
    </div>
  )
})
