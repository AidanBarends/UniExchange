/*
  ActiveFilters - the result toolbar: "N results" plus one removable pill per
  active filter (campus, category, search) and a Clear-all action.

  Makes the hyper-local filtering visible and always reversible - a click on a
  pill removes just that filter.

  Owner: Joshua Reid Adams (230317693)
*/

import { Badge } from '@/components/ui/Badge'

type ActiveFiltersProps = {
  resultCount: number
  campusName?: string
  categoryName?: string
  search?: string
  onClearCampus: () => void
  onClearCategory: () => void
  onClearSearch: () => void
  onClearAll: () => void
}

function FilterPill({
  label,
  value,
  onClear,
}: {
  label: string
  value: string
  onClear: () => void
}) {
  return (
    <Badge tone="brand">
      <span className="sr-only">{label} filter:</span>
      {value}
      <button
        type="button"
        onClick={onClear}
        aria-label={`Clear ${label} filter`}
        className="ml-1 text-brand-700 hover:text-brand-900"
      >
        ×
      </button>
    </Badge>
  )
}

export function ActiveFilters({
  resultCount,
  campusName,
  categoryName,
  search,
  onClearCampus,
  onClearCategory,
  onClearSearch,
  onClearAll,
}: ActiveFiltersProps) {
  const hasAny = Boolean(campusName || categoryName || search)

  return (
    <div className="flex flex-wrap items-center gap-2">
      <p className="text-sm text-ink-500" aria-live="polite">
        {resultCount} {resultCount === 1 ? 'result' : 'results'}
      </p>

      {campusName && <FilterPill label="campus" value={campusName} onClear={onClearCampus} />}
      {categoryName && (
        <FilterPill label="category" value={categoryName} onClear={onClearCategory} />
      )}
      {search && <FilterPill label="search" value={`“${search}”`} onClear={onClearSearch} />}

      {hasAny && (
        <button
          type="button"
          onClick={onClearAll}
          className="text-sm font-medium text-brand-700 underline hover:text-brand-900"
        >
          Clear all
        </button>
      )}
    </div>
  )
}
