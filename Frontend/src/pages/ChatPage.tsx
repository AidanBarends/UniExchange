/*
  A single conversation thread.

  OWNER: unassigned  (same person as MessagesPage)
  ROUTE: /messages/:conversationId

  TODO
   - messagesApi.messagesIn(conversationId)    GET /api/messages/conversation/:id
                                               (already oldest-first)
   - messagesApi.participantsIn(conversationId) to work out who you are talking to
   - messagesApi.send({ conversationId, senderId, content })  POST /api/messages
   - senderId is useAuth().session?.userId; compare it to message.senderId to
     decide which side of the thread a bubble sits on
   - there are no websockets on the backend, so new messages need polling or a
     manual refresh - keep it simple, poll on an interval and clear it on unmount
   - reuse: PageHeader, Avatar, Textarea, Button, EmptyState, Spinner

  Your own components go in src/components/messages/.
*/

import { useParams } from 'react-router-dom'

import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'

export function ChatPage() {
  const { conversationId } = useParams<{ conversationId: string }>()

  return (
    <>
      <PageHeader title="Chat" subtitle={`Conversation #${conversationId}`} />

      <EmptyState
        title="The chat thread is not built yet"
        description="The route and its :conversationId param already work. Open src/pages/ChatPage.tsx for the endpoints."
      />
    </>
  )
}
