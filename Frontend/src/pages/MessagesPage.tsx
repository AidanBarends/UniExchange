/*
  Conversation list.

  OWNER: unassigned
  ROUTE: /messages     (the thread itself is ChatPage at /messages/:conversationId)

  TODO
   - messagesApi.conversationsForUser(userId)
                     GET /api/conversation-participants/user/:id
     Each row gives you a conversationId; the domain has no JPA relationships, so
     there is no single "give me my conversations with previews" call. Fetch the
     participants, then resolve the other person and the latest message per
     conversation. Agree the approach with whoever builds ChatPage.
   - each row links to /messages/${conversationId}
   - reuse: PageHeader, Card, Avatar, Badge, EmptyState, Spinner

  Your own components go in src/components/messages/.
*/

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function MessagesPage() {
  return (
    <>
      <PageHeader title="Messages" subtitle="Your conversations with other students" />

      <EmptyState
        title="Messaging is not built yet"
        description="This page is unassigned. Open src/pages/MessagesPage.tsx - the endpoints are listed at the top."
      />
    </>
  )
}
