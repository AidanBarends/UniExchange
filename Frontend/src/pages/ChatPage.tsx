/*
  A single conversation thread.

  ROUTE: /messages/:conversationId

  Polling, not websockets - the backend has none, deliberately (Azure App
  Service's cheap tiers unload an idle app and drop persistent connections).
  Each poll passes the highest messageId already held, so the common case is an
  empty array rather than the whole thread.

  The thread body is a separate component with key={threadId}. Navigating from
  one conversation to another then remounts it, so the messages, the poll cursor
  and the scroll position all reset by construction - rather than needing an
  effect that clears them, which is both easy to get wrong and the kind of
  synchronous setState that React now warns about.
*/

import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'

import { MessageBubble } from '@/components/messages/MessageBubble'
import { MessageComposer } from '@/components/messages/MessageComposer'
import { PageHeader } from '@/components/layout/PageHeader'
import { Alert } from '@/components/ui/Alert'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { useAuth } from '@/auth/useAuth'
import { chatApi } from '@/lib/api/chat'
import { ApiError } from '@/lib/api/client'
import type { ChatMessageView, ChatThreadView } from '@/lib/api/types'

const POLL_MS = 3_000

export function ChatPage() {
  const { conversationId } = useParams<{ conversationId: string }>()
  const threadId = Number(conversationId)

  if (!threadId) {
    return (
      <>
        <PageHeader title="Chat" />
        <EmptyState title="Conversation not found" description="That link does not look right." />
      </>
    )
  }

  return <ChatThread key={threadId} threadId={threadId} />
}

function ChatThread({ threadId }: { threadId: number }) {
  const { session } = useAuth()
  const myUserId = session?.userId ?? 0

  const [messages, setMessages] = useState<ChatMessageView[] | null>(null)
  const [thread, setThread] = useState<ChatThreadView | null>(null)
  const [error, setError] = useState<string | null>(null)

  // The poll cursor. A ref rather than state so advancing it never schedules a
  // render, and the interval closure always sees the current value.
  const cursorRef = useRef(0)
  const bottomRef = useRef<HTMLDivElement | null>(null)

  const poll = useCallback(
    async (signal: { cancelled: boolean }) => {
      try {
        const fresh = await chatApi.messagesSince(threadId, cursorRef.current)
        if (signal.cancelled) return

        if (fresh.length > 0) {
          cursorRef.current = fresh[fresh.length - 1].messageId
          setMessages((previous) => [...(previous ?? []), ...fresh])
          // Anything that arrives while the thread is open has been seen.
          void chatApi.markRead(threadId).catch(() => {})
        } else {
          // Distinguishes "loaded, empty" from "still loading".
          setMessages((previous) => previous ?? [])
        }
        setError(null)
      } catch (err: unknown) {
        if (!signal.cancelled) {
          setError(err instanceof ApiError ? err.message : 'Something went wrong.')
        }
      }
    },
    [threadId],
  )

  useEffect(() => {
    const signal = { cancelled: false }

    // Inline for the first load, so no setState happens in the effect body.
    chatApi
      .messagesSince(threadId, 0)
      .then((fresh) => {
        if (signal.cancelled) return
        if (fresh.length > 0) cursorRef.current = fresh[fresh.length - 1].messageId
        setMessages(fresh)
        void chatApi.markRead(threadId).catch(() => {})
      })
      .catch((err: unknown) => {
        if (!signal.cancelled) {
          setError(err instanceof ApiError ? err.message : 'Something went wrong.')
        }
      })

    // The thread list is the only place the other participant's name and the
    // listing title live, since a message carries neither.
    chatApi
      .threads()
      .then((all) => {
        if (!signal.cancelled) {
          setThread(all.find((candidate) => candidate.conversationId === threadId) ?? null)
        }
      })
      .catch(() => {})

    const interval = window.setInterval(() => {
      // Polling a hidden tab is waste, and mobile browsers throttle it anyway.
      if (!document.hidden) void poll(signal)
    }, POLL_MS)

    const onFocus = () => void poll(signal)
    window.addEventListener('focus', onFocus)

    return () => {
      signal.cancelled = true
      window.clearInterval(interval)
      window.removeEventListener('focus', onFocus)
    }
  }, [poll, threadId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const otherName = thread?.otherParticipant
    ? `${thread.otherParticipant.firstName} ${thread.otherParticipant.lastName}`
    : 'Conversation'

  return (
    <>
      <PageHeader
        title={otherName}
        subtitle={thread?.listingTitle ? `About: ${thread.listingTitle}` : undefined}
      />

      {error && <Alert tone="error">{error}</Alert>}

      <div className="flex min-h-[60vh] flex-col overflow-hidden rounded-2xl bg-gray-50 ring-1 ring-gray-200">
        <div className="flex-1 space-y-2 overflow-y-auto p-4">
          {messages === null && (
            <div className="grid place-items-center py-12">
              <Spinner />
            </div>
          )}

          {messages?.length === 0 && (
            <p className="py-12 text-center text-sm text-ink-500">No messages yet. Say hello.</p>
          )}

          {messages?.map((message) => (
            <MessageBubble
              key={message.messageId}
              message={message}
              mine={message.senderId === myUserId}
            />
          ))}

          <div ref={bottomRef} />
        </div>

        <MessageComposer
          conversationId={threadId}
          // Poll immediately after sending, so a message you sent and one you
          // received arrive through exactly the same path.
          onSent={() => void poll({ cancelled: false })}
        />
      </div>
    </>
  )
}
