/*
  CategoryChips - the mobile category filter row from the T2 mobile mockup
  ("All Items | Textbooks | Electronics"). Horizontally scrollable, thumb-sized.

  Desktop uses FeedSidebar instead - this renders only below `lg`.

  Owner: Joshua Reid Adams (230317693)
*/

import type { Category } from '@/lib/api/types'

type CategoryChipsProps = {
  categories: Category[]
  /** null means "All Items". */
  activeCategoryId: number | null
  onSelectCategory: (categoryId: number | null) => void
}

const CHIP_BASE =
  'whitespace-nowrap rounded-full border px-3.5 py-1.5 text-sm font-medium transition ' +
  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600'
const CHIP_ACTIVE = 'border-brand-600 bg-brand-600 text-white'
const CHIP_IDLE = 'border-gray-300 bg-white text-ink-700 hover:border-brand-300'

export function CategoryChips({ categories, activeCategoryId, onSelectCategory }: CategoryChipsProps) {
  return (
    <div className="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1 lg:hidden" role="tablist" aria-label="Categories">
      <button
        type="button"
        role="tab"
        aria-selected={activeCategoryId === null}
        onClick={() => onSelectCategory(null)}
        className={`${CHIP_BASE} ${activeCategoryId === null ? CHIP_ACTIVE : CHIP_IDLE}`}
      >
        All Items
      </button>

      {categories.map((category) => (
        <button
          key={category.categoryId}
          type="button"
          role="tab"
          aria-selected={activeCategoryId === category.categoryId}
          onClick={() => onSelectCategory(category.categoryId)}
          className={`${CHIP_BASE} ${activeCategoryId === category.categoryId ? CHIP_ACTIVE : CHIP_IDLE}`}
        >
          {category.name}
        </button>
      ))}
    </div>
  )
}
