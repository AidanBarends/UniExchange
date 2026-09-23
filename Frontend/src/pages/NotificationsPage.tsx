/*
  Notification inbox.

  OWNER: unassigned
  ROUTE: /notifications

  TODO
   - notificationsApi.forUser(userId)          GET /api/notifications/user/:id
                                               (already newest-first)
   - notificationsApi.unreadForUser(userId)    GET /api/notifications/user/:id/unread
   - notificationsApi.markRead(notificationId) PATCH /api/notifications/:id/read
   - userId comes from useAuth().session?.userId
   - the unread flag arrives as `read`, NOT `isRead` - see src/lib/api/types.ts
   - notification.type is MESSAGE | LISTING | TRANSACTION | BULLETIN | SYSTEM, and
     entityType/entityId tell you what to link to
   - the bell in TopBar has a commented-out unread dot waiting for you:
     src/components/layout/TopBar.tsx
   - reuse: PageHeader, Card, Badge, EmptyState, Spinner

  Your own components go in src/components/notifications/.
*/

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function NotificationsPage() {
  return (
    <>
      <PageHeader title="Notifications" subtitle="Messages, sales and campus news" />

      <EmptyState
        title="Notifications are not built yet"
        description="This page is unassigned. Open src/pages/NotificationsPage.tsx - the endpoints are listed at the top."
      />
    </>
  )
}
