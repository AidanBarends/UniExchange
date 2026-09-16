/*
  "Filter Feed" panel from the mockup - now backed by the real
  BulletinPostCategory field and GET /api/bulletin-posts/category/:category.

  Behaves like a single-select even though it's drawn as checkboxes, matching
  the mockup's visual style: choosing a specific category deselects "All
  Posts", and choosing "All Posts" clears back to everything. There's no
  backend support for selecting MULTIPLE categories at once, so this doesn't
  pretend to offer that.

  Trending Tags stays the disabled "Coming soon" mock from before - there is
  still no tags entity anywhere in the backend, unrelated to this category
  work.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { CATEGORY_LABELS, FILTERABLE_CATEGORIES } from '@/components/bulletin/categoryLabels'
import { Card } from '@/components/ui/Card'
import { Checkbox } from '@/components/ui/Checkbox'
import type { BulletinPostCategory } from '@/lib/api/types'

const COMING_SOON_TAGS = ['#campus', '#midterms', '#textbookexchange', '#roomkeys']

type FilterFeedSidebarProps = {
  /** null means "All Posts" - no filter applied. */
  selected: BulletinPostCategory | null
  onSelect: (category: BulletinPostCategory | null) => void
}

export function FilterFeedSidebar({ selected, onSelect }: FilterFeedSidebarProps) {
  return (
    <div className="space-y-4">
      <Card>
        <p className="mb-3 text-sm font-medium text-ink-700">Filter Feed</p>

        <div className="space-y-2.5">
          <Checkbox label="All Posts" checked={selected === null} onChange={() => onSelect(null)} />
          {FILTERABLE_CATEGORIES.map((category) => (
            <Checkbox
              key={category}
              label={CATEGORY_LABELS[category]}
              checked={selected === category}
              onChange={() => onSelect(selected === category ? null : category)}
            />
          ))}
        </div>
      </Card>

      <Card>
        <p className="mb-1 text-sm font-medium text-ink-700">Trending Tags</p>
        <p className="mb-3 text-xs text-ink-400">Coming soon</p>
        <div className="flex flex-wrap gap-1.5">
          {COMING_SOON_TAGS.map((tag) => (
            <span
              key={tag}
              className="cursor-not-allowed rounded-full bg-gray-100 px-2.5 py-1 text-xs text-ink-400"
            >
              {tag}
            </span>
          ))}
        </div>
      </Card>
    </div>
  )
}
