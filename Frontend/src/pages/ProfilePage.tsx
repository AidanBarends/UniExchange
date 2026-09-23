/*
  A student's profile and reputation.

  OWNER: Raul Ja'aim Everts (230270565)
  ROUTES: /profile            -> the signed-in student (no param)
          /profile/:userId    -> somebody else

  One component serves both: when useParams().userId is undefined you are
  looking at yourself, and useAuth().user already holds that data with no
  request needed.

  TODO
   - usersApi.byId(userId)                     GET /api/users/:id   (other people)
   - listingsApi.bySeller(userId)              GET /api/listings/seller/:id
   - usersApi.averageRating(userId)            GET /api/reviews/reviewee/:id/average
   - usersApi.reviewsAbout(userId)             GET /api/reviews/reviewee/:id
   - trusted seller badge: GET /api/trusted-seller-badges/user/:id - this 404s
     when they have no badge, which is normal, so catch it rather than showing
     an error
   - reuse: PageHeader, Card, Avatar, Badge, EmptyState, Spinner

  The account panel below came from the old placeholder dashboard. Keep it,
  move it, or bin it - it is a starting point, not a requirement.

  Your own components go in src/components/profile/.
*/

import { useParams } from 'react-router-dom'

import { useAuth } from '@/auth/useAuth'
import { PageHeader } from '@/components/layout/PageHeader'
import { Avatar } from '@/components/ui/Avatar'
import { Badge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'

export function ProfilePage() {
  const { userId } = useParams<{ userId?: string }>()
  const { user, session, loadingUser } = useAuth()

  const isOwnProfile = userId === undefined
  const fullName = user ? `${user.firstName} ${user.lastName}` : null

  if (!isOwnProfile) {
    return (
      <>
        <PageHeader title="Profile" subtitle={`Student #${userId}`} />
        <EmptyState
          title="Other people's profiles are not built yet"
          description="The route and its :userId param already work. Open src/pages/ProfilePage.tsx for the endpoints."
        />
      </>
    )
  }

  return (
    <>
      <PageHeader title="Your profile" subtitle="How other students see you" />

      <div className="mb-6 flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-4">
        <Avatar name={fullName} className="size-14" />
        <div className="min-w-0">
          <p className="truncate text-base font-semibold text-ink-900">
            {loadingUser ? 'Loading…' : (fullName ?? 'Student')}
          </p>
          <p className="truncate text-sm text-ink-500">{user?.email ?? session?.email ?? '—'}</p>
          <div className="mt-1.5 flex flex-wrap gap-1.5">
            <Badge tone={user?.accountStatus === 'ACTIVE' ? 'success' : 'warning'}>
              {user?.accountStatus ?? '—'}
            </Badge>
            {session?.roles.map((role) => (
              <Badge key={role} tone="brand">
                {role.replace('ROLE_', '')}
              </Badge>
            ))}
          </div>
        </div>
      </div>

      <EmptyState
        title="Your listings and reviews go here"
        description="Open src/pages/ProfilePage.tsx - the TODO lists the endpoints for listings, ratings, reviews and the trusted-seller badge."
      />
    </>
  )
}
