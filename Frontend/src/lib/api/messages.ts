/*
  Conversations and messages. OWNER: unassigned

  The domain has no JPA relationships, so a conversation does not carry its
  participants or messages - fetch them separately:
    conversationsForUser -> participants rows for the signed-in user
    messagesIn(conversationId) -> the thread, already ordered oldest-first
*/

import { authedRequest } from './client'
import type { Conversation, ConversationParticipant, Message } from './types'

export const messagesApi = {
  /** Participant rows for a user; each one carries a conversationId. */
  conversationsForUser: (userId: number) =>
    authedRequest<ConversationParticipant[]>(`/api/conversation-participants/user/${userId}`),

  conversationById: (conversationId: number | string) =>
    authedRequest<Conversation>(`/api/conversations/${conversationId}`),

  participantsIn: (conversationId: number | string) =>
    authedRequest<ConversationParticipant[]>(
      `/api/conversation-participants/conversation/${conversationId}`,
    ),

  /** Ordered by sentAt ascending on the backend. */
  messagesIn: (conversationId: number | string) =>
    authedRequest<Message[]>(`/api/messages/conversation/${conversationId}`),

  send: (body: { conversationId: number; senderId: number; content: string }) =>
    authedRequest<Message>('/api/messages', { method: 'POST', body }),

  startConversation: () => authedRequest<Conversation>('/api/conversations', { method: 'POST' }),

  addParticipant: (body: { conversationId: number; userId: number }) =>
    authedRequest<ConversationParticipant>('/api/conversation-participants', {
      method: 'POST',
      body,
    }),
}
