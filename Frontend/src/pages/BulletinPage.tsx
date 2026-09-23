/*
  Campus bulletin board.

  OWNER: unassigned
  ROUTE: /bulletin

  TODO
   - bulletinApi.list()                        GET /api/bulletin-posts   (public)
   - bulletinApi.announcements()               GET /api/bulletin-posts/announcements
   - bulletinApi.create({ authorId, title, content, status: 'PUBLISHED',
                          isFacultyAnnouncement: false })
                                               POST /api/bulletin-posts
   - authorId comes from useAuth().session?.userId
   - faculty announcements arrive as `facultyAnnouncement`, NOT
     `isFacultyAnnouncement` - see src/lib/api/types.ts. Pin those to the top
   - status is PUBLISHED | HIDDEN | REMOVED; only show PUBLISHED
   - reuse: PageHeader, Card, Badge, Textarea, TextField, Button, EmptyState

  Your own components go in src/components/bulletin/.
*/

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function BulletinPage() {
  return (
    <>
      <PageHeader title="Campus bulletin" subtitle="Announcements and notices" />

      <EmptyState
        title="The bulletin board is not built yet"
        description="This page is unassigned. Open src/pages/BulletinPage.tsx - the endpoints are listed at the top."
      />
    </>
  )
}
