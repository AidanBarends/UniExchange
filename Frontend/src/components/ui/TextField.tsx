import { forwardRef } from 'react'

type TextFieldProps = React.ComponentProps<'input'> & {
  label: string
  error?: string
  hint?: string
}

export const TextField = forwardRef<HTMLInputElement, TextFieldProps>(function TextField(
  { label, error, hint, id, className = '', ...rest },
  ref,
) {
  const inputId = id ?? rest.name
  const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined

  return (
    <div className="space-y-1.5">
      <label htmlFor={inputId} className="block text-sm font-medium text-ink-700">
        {label}
      </label>

      <input
        {...rest}
        id={inputId}
        ref={ref}
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
        <p id={`${inputId}-error`} className="text-xs text-red-600">
          {error}
        </p>
      ) : hint ? (
        <p id={`${inputId}-hint`} className="text-xs text-ink-400">
          {hint}
        </p>
      ) : null}
    </div>
  )
})
