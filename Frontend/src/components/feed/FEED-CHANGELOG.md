# Feed Change Log — Joshua Reid Adams (230317693)

Every change made to the UniExchange frontend from the moment we switched from
`main` to my branch (`JRA-230317693`) to start work on the homepage feed.

Branch: `JRA-230317693` · Commits: `1fd7f92` → `d87adfb` → `65c0f97` → `76a8245`

---

## Starting point

After pulling `main` (which contained the team lead's new routed frontend
scaffold and Yaseen's merged backend + auth work), my `/feed` route was a
placeholder: a `PageHeader` and an `EmptyState` saying "The feed is not built
yet". The branch was 18 commits behind its own origin copy, so it was first
fast-forwarded and merged with `main` before any work started.

## Ground rules I worked under (from the team lead)

1. **No custom headers/menus** — every page renders inside the shared
   `AppLayout`; I only built page content.
2. **Only my API file(s)** — I edited nothing in `src/lib/api/`; the feed uses
   the existing `listingsApi` and `authApi.campuses()` as-is.
3. **Shared components only** — everything visual comes from
   `src/components/ui/` (Card, Badge, Button, TextField, Select, Alert,
   Spinner, EmptyState) plus the shared `PageHeader`.
4. **My own components live in `src/components/feed/`** — nothing of mine
   leaks into anyone else's folder.

## Design references used

- **T2 document**, "2 Marketplace Feed" desktop + mobile mockups (page 28):
  sidebar with Sell button + categories + counts, card grid with badges,
  ZAR prices, location and relative timestamps; mobile chips and
  single-column cards.
- **Component Libraries shortlist (PDF)** — patterns borrowed and attributed:
  - **Preline** → skeleton loading cards
  - **Origin UI** → active-filter pill toolbar
  - **Pattern Craft** → dot-grid empty-state backdrop
  - **Motion-Primitives** → staggered card entrance (on the `motion` library)
  - **shadcn/ui** → the "copy patterns, not dependencies" principle; its
    accessible-primitives approach is the model for any future shared
    drawer/toast work (team-lead territory, not feed-local).

## Commit 1 — `1fd7f92` "feat: build homepage feed with search, campus and category filters"

The functional feed, wired to the real backend:

- **`pages/FeedPage.tsx`** (replaced the placeholder):
  - Loads listings via `GET /api/listings/search` (backend already filters to
    `ACTIVE`), categories via `GET /api/categories`, campuses via
    `GET /api/campuses`, all through the existing `lib/api` modules.
  - **Campus filter defaults to the signed-in student's campus**
    (`useAuth().user?.campusId`) — the product's "hyper-local" premise.
  - **Debounced search** (350 ms) so typing does not fire a request per
    keystroke.
  - Newest-first ordering (the T2 doc promises "the latest listings").
  - StrictMode-safe data loading (cancelled-flag pattern from the Frontend
    README); results kept stale-while-revalidate instead of flashing a
    spinner on every filter change (React 19 lint forced the cleaner
    pattern — a good trade).
  - Spinner on first load, error `Alert` with Retry, and an `EmptyState`
    with a "Sell something" call to action.
- **`components/feed/ListingCard.tsx`** — placeholder image tile, title,
  blue ZAR price (`Intl.NumberFormat('en-ZA', ZAR)`), 📍 campus + "2h ago"
  relative time; the whole card is a `Card to=` link to
  `/listings/:listingId` (Aidan's page).
- **`components/feed/ListingGrid.tsx`** — responsive 1 → 2 → 3 columns.
- **`components/feed/FeedSidebar.tsx`** — desktop-only left rail per the
  mockup: "+ Sell Item" button → `/listings/new`, and a Categories card with
  live per-category ACTIVE counts computed from one `GET /api/listings` call.
- **`components/feed/CategoryChips.tsx`** — mobile-only scrollable category
  chips ("All Items / Textbooks / …").

## Commit 2 — `d87adfb` "refactor: move feed components directly into components/feed/"

Structure correction: the four feed components had been nested in an extra
`components/feed/components/` subfolder; moved up to `src/components/feed/`
exactly as the folder's README intended (git recorded 100% renames; imports
updated; `FeedPage` comment fixed).

## Commit 3 — `65c0f97` "feat: feed UX polish - skeletons, sort, filter pills, badges and icons"

The design-polish pass (no new dependencies, palette untouched):

- **`ListingCardSkeleton.tsx`** (new) — pulsing skeleton grid on first load,
  same shape as real cards so the swap does not jump (Preline pattern).
- **`ActiveFilters.tsx`** (new) — result toolbar: "N results" + one removable
  pill per active filter (campus / category / search) + "Clear all"
  (Origin UI pattern).
- **`CategoryIcon.tsx`** (new) — keyword-mapped stroke icons per category
  (book / chip / shirt / wrench / armchair / tag fallback), shown in the
  sidebar rows next to the counts.
- **`ListingCard.tsx`** — hover lift (`-translate-y-0.5` + shadow) and image
  tile zoom (`group-hover:scale-105`); **"Just listed"** badge (green, < 24 h)
  and **"SOLD"** badge (neutral) overlays.
- **`FeedPage.tsx`** — **Sort control** (Newest / Price ↑ / Price ↓,
  client-side over the already-fetched rows), inline **clear (×) button**
  in the search field, and the empty state wrapped in a subtle
  **dot-grid backdrop** with the Sell CTA (Pattern Craft style).

## Commit 4 — `76a8245` "feat: staggered entrance animation for feed cards"

- Added the **`motion`** library (^13.2.0) — the single new dependency.
- **`ListingGrid.tsx`** — cards fade in and rise 8 px with a 40 ms stagger
  (Motion-Primitives pattern); **`prefers-reduced-motion` users get cards
  instantly with no movement** via `useReducedMotion`.

## What was deliberately NOT built (v2 candidates)

- **Price-range filter** — the search endpoint has no min/max params; needs
  backend work first (Aidan/lead domain).
- **Real card images** — `GET /api/listing-images/listing/:id` is one request
  per card; do it behind a batch endpoint or on hover/lazy.
- **Seller name + trusted-seller badge on cards** — N+1 requests per feed
  load; wants a denormalized field or batch endpoint.
- **Load-more / pagination** — search currently returns everything.
- **Save/favourite ❤** — no wishlist table in the schema yet.

## Verification done at every step

`npx tsc -b` · `npm run lint` (zero problems) · `npm run build` — all green
before every commit. No shared file was modified at any point.

## Files touched (all within my ownership)

```
Frontend/src/pages/FeedPage.tsx
Frontend/src/components/feed/ListingCard.tsx
Frontend/src/components/feed/ListingGrid.tsx
Frontend/src/components/feed/FeedSidebar.tsx
Frontend/src/components/feed/CategoryChips.tsx
Frontend/src/components/feed/CategoryIcon.tsx
Frontend/src/components/feed/ActiveFilters.tsx
Frontend/src/components/feed/ListingCardSkeleton.tsx
Frontend/package.json + package-lock.json   (motion dependency only)
```
