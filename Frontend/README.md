# UniExchange — Frontend

React + TypeScript + Vite client for the UniExchange campus marketplace.

Auth is finished. Every other page is **scaffolded and routed** — the file exists,
the route works, and the top of each file lists the exact endpoints to call. Pick
up your page and write the UI.

## Who owns what

| Page | Owner | Route | File | API module |
|---|---|---|---|---|
| Login / Signup / Verify | Yaseen Kannemeyer (240453182) | `/login` `/signup` `/verify` | `pages/LoginPage.tsx` etc. | `lib/api/auth.ts` |
| Homepage feed | **Joshua Reid Adams** (230317693) | `/feed` | `pages/FeedPage.tsx` | `lib/api/listings.ts` |
| Product details | **Aidan Barends** (230255639) | `/listings/:listingId` | `pages/ListingDetailsPage.tsx` | `lib/api/listings.ts` |
| Create listing | **Mogamat Wazeer Gilbert** (221374698) | `/listings/new` | `pages/CreateListingPage.tsx` | `lib/api/listings.ts` |
| User profile | **Raul Ja'aim Everts** (230270565) | `/profile`, `/profile/:userId` | `pages/ProfilePage.tsx` | `lib/api/users.ts` |
| Notifications | *unassigned* | `/notifications` | `pages/NotificationsPage.tsx` | `lib/api/notifications.ts` |
| Message chat | *unassigned* | `/messages`, `/messages/:conversationId` | `pages/MessagesPage.tsx`, `pages/ChatPage.tsx` | `lib/api/messages.ts` |
| Campus bulletin | *unassigned* | `/bulletin` | `pages/BulletinPage.tsx` | `lib/api/bulletin.ts` |

Each owner also has `src/components/<feature>/` for components only their page uses.

## Getting started

```bash
npm install
cp .env.example .env.local     # only needed if the backend is not on :8080
npm run dev                    # http://localhost:5173
```

The Spring Boot backend must be running (see the root README). Or in VS Code, press
`F5` with **Full Stack: Backend + Frontend** selected and both start together.

```bash
npm run build      # production bundle
npm run lint
npx tsc -b         # type-check
```

## How to build your page

Everything below already exists. You should not need to touch anyone else's files.

**1. Your page renders content only.** The header, navigation and sign-out button
come from `AppLayout`, which wraps every signed-in route. Start with `PageHeader`:

```tsx
import { PageHeader } from '@/components/layout/PageHeader'

export function FeedPage() {
  return (
    <>
      <PageHeader title="Feed" subtitle="What's for sale on your campus" />
      {/* your content */}
    </>
  )
}
```

**2. Call the backend through your API module**, never `fetch` directly. Auth is
attached for you:

```tsx
import { listingsApi } from '@/lib/api/listings'
import { ApiError } from '@/lib/api/client'

const listings = await listingsApi.list()
```

`authedRequest` reads the token from the stored session, so you never pass it in.
Add new endpoints to *your* module in `src/lib/api/` — that is the whole point of
the split, so five people are not editing one file.

**3. Fetch in an effect, StrictMode-safe.** Copy this shape; effects run twice in
development and this is what stops the double-set:

```tsx
useEffect(() => {
  let cancelled = false
  listingsApi
    .list()
    .then((data) => { if (!cancelled) setListings(data) })
    .catch((error: unknown) => {
      if (!cancelled) setError(error instanceof ApiError ? error.message : 'Something went wrong.')
    })
  return () => { cancelled = true }
}, [])
```

**4. Who is signed in:** `useAuth()` gives you `user` (full profile), `session`
(`userId`, `email`, `roles`) and `signOut`. No request needed.

**5. Forms:** react-hook-form + zod. Copy `SignUpPage.tsx`, and put your schema in
`src/lib/schemas.ts` next to the existing ones.

## Shared components

Use these rather than writing your own — that is what keeps the seven pages looking
like one app.

| From `@/components/ui/` | |
|---|---|
| `Button` | full width by default; `className="w-auto px-3"` for inline |
| `TextField` `Textarea` `Select` | form inputs, all `forwardRef` so `{...register()}` works |
| `Card` | white panel; pass `to="/path"` to make the whole card a link |
| `Badge` | status pill — `neutral` `brand` `success` `warning` `danger` |
| `Avatar` | initials in a circle |
| `Spinner` `EmptyState` | loading and empty states |
| `Alert` | `error` `success` `info` |
| `Checkbox` | label sits beside the box; `forwardRef` like the other inputs |
| `OtpInput` | auth only |

| From `@/components/layout/` | |
|---|---|
| `PageHeader` | title + subtitle + optional action, top of every page |
| `AppLayout` `TopBar` `BottomNav` | the shell — mounted once in `App.tsx`, don't import these |
| `AuthLayout` `Logo` | auth screens and the wordmark |

Add a destination to the nav by editing `components/layout/navigation.ts` — the
mobile tab bar and desktop nav both read from it, so they can never drift apart.

## Two backend gotchas

**Booleans are renamed on the way out.** The entities declare `isPrimary`, `isRead`
and `isFacultyAnnouncement`, but their Java getters are `isPrimary()` etc. and
Jackson strips the `is`. So you **send** `{ "isPrimary": true }` and **read back**
`{ "primary": true }`. The types in `lib/api/types.ts` use the response names. If a
boolean comes back `undefined`, this is why.

**Foreign keys are plain numbers.** There are no JPA relationships in the domain, so
a `Listing` gives you `sellerId`, not a nested `seller` object. Fetch related
records separately.

## Layout

```
src/
├── main.tsx              BrowserRouter + AuthProvider
├── App.tsx               every route, grouped by owner
├── index.css             Tailwind v4 import + design tokens (@theme)
├── lib/
│   ├── session.ts        session + trusted-device token; picks local/sessionStorage
│   ├── schemas.ts        zod schemas
│   └── api/
│       ├── client.ts     request() / authedRequest() / ApiError  — shared
│       ├── types.ts      response shapes                          — shared
│       └── auth.ts listings.ts users.ts messages.ts notifications.ts bulletin.ts
├── auth/                 authContext, AuthProvider, useAuth, ProtectedRoute
├── components/
│   ├── ui/               shared primitives
│   ├── layout/           app shell + navigation
│   └── feed/ listings/ profile/ messages/ notifications/ bulletin/   per owner
└── pages/                one file per route
```

Routes: `/` redirects by auth state · `/login` `/signup` `/verify` (public) ·
`/feed` `/listings/new` `/listings/:listingId` `/profile` `/profile/:userId`
`/notifications` `/messages` `/messages/:conversationId` `/bulletin` (protected) ·
anything else shows the 404 page.

## Design tokens

Defined in `src/index.css`, available as normal Tailwind utilities.

- **Brand** `brand-50` … `brand-900` (teal/blue). `brand-600` is the primary.
- **Ink** — **only** `ink-400` `ink-500` `ink-700` `ink-900` exist. `ink-600` and
  friends silently produce no style.
- Everything else is stock Tailwind. Stick to `gray-200` borders, `gray-300` inputs,
  `red-*` errors, `emerald-*` success, `amber-*` warning.
- Shapes: `rounded-lg` inputs/buttons, `rounded-xl` small cards, `rounded-2xl` panels.

Icons are hand-written inline SVG (24×24, `fill="none"`, `stroke="currentColor"`,
`strokeWidth="2"`) — see `components/layout/NavIcons.tsx`. No icon library.

## Session and "Remember me"

Signing in takes **two steps unless this browser has been trusted before**:

| `POST /api/auth/login` | Status | Body | What to do |
|---|---|---|---|
| trusted device | `200` | `AuthResponse` (has `token`) | signed in |
| everything else | `202` | `RegistrationResponse` (no token) | a code was emailed → `/verify` |

So `authApi.login()` returns `AuthResponse | RegistrationResponse`. Narrow it with
`'token' in result`. Only `/verify-otp` ever issues a token, and it is also the only
place a device earns the right to skip the code next time.

The **Remember me** checkbox picks which browser store everything goes into, and that
choice is the whole mechanism:

| Ticked | Session + device token in | Survives reload | Survives closing the browser |
|---|---|---|---|
| yes | `localStorage`, token lasts 30 days | yes | **yes** |
| no | `sessionStorage`, token lasts 1 hour | yes | **no → code required again** |

`sessionStorage` being wiped when the browser closes *is* the "session was lost"
trigger — nothing detects it. `lib/session.ts` writes to one store and clears the
other, so the two can never disagree.

An expired token is treated as signed out on load (there is **no refresh endpoint**),
and any `401` clears the session. Signing out deliberately keeps the device token:
remembering a device means "don't ask me for a code here again", not "stay signed in
forever".

You should not need to touch any of this — `useAuth()` and `authedRequest()` work
exactly as before.
