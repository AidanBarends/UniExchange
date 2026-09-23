/*
  Dropdown matching TextField's look. Use this instead of hand-writing a
  <select> and copying the class string.

  Pass options as children, exactly like a native select:

    <Select label="Campus" {...register('campusId')}>
      <option value="">Choose a campus</option>
      {campuses.map((c) => <option key={c.campusId} value={c.campusId}>{c.name}</option>)}
    </Select>
*/

import { forwardRef } from 'react'

type SelectProps = React.ComponentProps<'select'> & {
  label: string
  error?: string
  hint?: string
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, error, hint, id, className = '', children, ...rest },
  ref,
) {
  const selectId = id ?? rest.name
  const describedBy = error ? `${selectId}-error` : hint ? `${selectId}-hint` : undefined

  return (
    <div className="space-y-1.5">
      <label htmlFor={selectId} className="block text-sm font-medium text-ink-700">
        {label}
      </label>

      <select
        {...rest}
        id={selectId}
        ref={ref}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        className={
          'block w-full rounded-lg border bg-white px-3 py-2.5 text-sm text-ink-900 ' +
          'focus:outline-2 focus:outline-offset-0 ' +
          (error
            ? 'border-red-300 focus:outline-red-500 '
            : 'border-gray-300 focus:outline-brand-600 ') +
          className
        }
      >
        {children}
      </select>

      {error ? (
        <p id={`${selectId}-error`} className="text-xs text-red-600">
          {error}
        </p>
      ) : hint ? (
        <p id={`${selectId}-hint`} className="text-xs text-ink-400">
          {hint}
        </p>
      ) : null}
    </div>
  )
})
