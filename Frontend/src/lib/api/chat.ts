/*
  Chat: conversations, messages and attachments.

  This replaces the old raw-CRUD messages module. The backend's
  /api/conversations, /api/conversation-participants and /api/messages endpoints
  are now ADMIN-only, because they took ids straight from the request body and
  would have let any signed-in student read anyone's private thread. Everything
  here goes through /api/chat, which works out who you are from the token.

  Delivery is by polling. There is no WebSocket on the backend - see the plan for
  why, but the short version is that Azure App Service's cheaper tiers unload an
  idle app and drop persistent connections. Poll an open thread with
  `messagesSince`, which returns an empty array when nothing is new.
*/

import { authedRequest, authedUploadWithProgress } from './client'
import type { ChatMediaUploaded, ChatMessageView, ChatThreadView } from './types'

export const chatApi = {
  /** The inbox: every thread with its last message, unread count and listing. */
  threads: () => authedRequest<ChatThreadView[]>('/api/chat/threads'),

  /**
   * Opens the conversation with another student, reusing the existing one when
   * there already is one about the same listing. This is what "Message Seller"
   * calls - it is safe to call repeatedly.
   */
  startThread: (body: { otherUserId: number; listingId?: number | null }) =>
    authedRequest<ChatThreadView>('/api/chat/threads', { method: 'POST', body }),

  /**
   * Messages newer than `afterMessageId`. Pass 0 for the whole thread, then the
   * highest messageId you have seen for each subsequent poll.
   */
  messagesSince: (conversationId: number | string, afterMessageId = 0) =>
    authedRequest<ChatMessageView[]>(`/api/chat/threads/${conversationId}/messages`, {
      query: { afterMessageId },
    }),

  /** `mediaId` comes from `uploadMedia`. At least one of content/mediaId is required. */
  send: (
    conversationId: number | string,
    body: { content?: string; mediaId?: number | null },
  ) =>
    authedRequest<ChatMessageView>(`/api/chat/threads/${conversationId}/messages`, {
      method: 'POST',
      body,
    }),

  /**
   * Uploads an attachment, returning the id to pass to `send`.
   *
   * Two steps rather than one, so the UI can show progress, preview the file
   * before committing, and retry a failed upload without the student re-picking
   * it or losing a typed caption.
   *
   * `durationMs` is required for voice notes: a MediaRecorder blob carries no
   * duration, so time the recording yourself and pass it here.
   */
  uploadMedia: (
    conversationId: number | string,
    file: File,
    options: { durationMs?: number; onProgress?: (percent: number) => void; signal?: AbortSignal } = {},
  ) =>
    authedUploadWithProgress<ChatMediaUploaded>(
      `/api/chat/threads/${conversationId}/media`,
      file,
      {
        query: { durationMs: options.durationMs },
        onProgress: options.onProgress,
        signal: options.signal,
      },
    ),

  markRead: (conversationId: number | string) =>
    authedRequest<void>(`/api/chat/threads/${conversationId}/read`, { method: 'POST' }),

  /** Drives the dot on the Messages tab. */
  unreadCount: () => authedRequest<{ count: number }>('/api/chat/unread-count'),
}
