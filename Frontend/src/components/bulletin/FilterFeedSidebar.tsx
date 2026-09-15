/*
  "Filter Feed" panel from the mockup.

  IMPORTANT: this is UI-only. BulletinPost has no category field, so there is
  no real data to filter by yet - checking "Events" would have to either do
  nothing (misleading - looks broken) or filter against a category invented
  for a REAL post someone actually wrote (worse - attaches a false label to
  someone's real content that anyone using the app would see, not just
  something visible in a code comment).

  So instead: "All Posts" is checked and locked on, since that's genuinely
  the only real state right now. Everything else is disabled with a
  "Coming soon" hint rather than pretending to work. Once BulletinPost has a
  real category field, this becomes a real filter - the disabled items are a
  placeholder for that, not a permanent design choice.

  Owner: Aidan Barends (230255639), for /bulletin only. MOCK UI - not
  backend-connected. Do not treat as done; see roadmap for backend plan.
*/

import { Card } from '@/components/ui/Card'
import { Checkbox } from '@/components/ui/Checkbox'

const COMING_SOON_CATEGORIES = ['Events', 'Study Groups', 'Lost & Found']
const COMING_SOON_TAGS = ['#campus', '#midterms', '#textbookexchange', '#roomkeys']

export function FilterFeedSidebar() {
  return (
    <div className="space-y-4">
      <Card>
        <p className="mb-3 text-sm font-medium text-ink-700">Filter Feed</p>

        <div className="space-y-2.5">
          <Checkbox label="All Posts" checked disabled />
          {COMING_SOON_CATEGORIES.map((category) => (
            <Checkbox key={category} label={category} disabled hint="Coming soon" />
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