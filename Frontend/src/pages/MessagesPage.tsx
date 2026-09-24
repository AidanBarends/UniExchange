/*
  Conversation list.

  ROUTE: /messages     (the thread itself is ChatPage at /messages/:conversationId)

  One request, not N+1. The backend's /api/chat/threads assembles the other
  participant, the listing, the last message and the unread count server-side,
  because the domain has no JPA relationships and doing it here would mean three
  extra round trips per row.
*/

import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { PageHeader } from '@/components/layout/PageHeader'
import { Avatar } from '@/components/ui/Avatar'
import { Alert } from '@/components/ui/Alert'
import { Badge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { chatApi } from '@/lib/api/chat'
import { ApiError } from '@/lib/api/client'
import type { ChatThreadView } from '@/lib/api/types'

/** Slow: this is the inbox, not an open thread, so it does not need to feel live. */
const POLL_MS = 15_000

function relativeTime(iso: string | null): string {
  if (!iso) return ''
  const then = new Date(iso).getTime()
  const minutes = Math.round((Date.now() - then) / 60_000)

  if (minutes < 1) return 'now'
  if (minutes < 60) return `${minutes}m`
  if (minutes < 60 * 24) return `${Math.round(minutes / 60)}h`
  return new Date(iso).toLocaleDateString()
}

export function MessagesPage() {
  const [threads, setThreads] = useState<ChatThreadView[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async (signal?: { cancelled: boolean }) => {
    try {
      const results = await chatApi.threads()
      if (!signal?.cancelled) {
        setThreads(results)
        setError(null)
      }
    } catch (err: unknown) {
      if (!signal?.cancelled) {
        setError(err instanceof ApiError ? err.message : 'Something went wrong.')
      }
    }
  }, [])

  useEffect(() => {
    const signal = { cancelled: false }

    // Inline promise chain for the first load rather than calling load()
    // directly: setState in an effect body trips react-hooks/set-state-in-effect,
    // and this is the shape the rest of the app already uses. The interval below
    // can call load() freely, because a timer callback is not the effect body.
    chatApi
      .threads()
      .then((results) => {
        if (!signal.cancelled) {
          setThreads(results)
          setError(null)
        }
      })
      .catch((err: unknown) => {
        if (!signal.cancelled) {
          setError(err instanceof ApiError ? err.message : 'Something went wrong.')
        }
      })

    const interval = window.setInterval(() => {
      // Polling a hidden tab is pure waste - and on a phone the browser throttles
      // it to seconds-to-minutes anyway, so the data would be stale regardless.
      if (!document.hidden) void load(signal)
    }, POLL_MS)

    const onFocus = () => void load(signal)
    window.addEventListener('focus', onFocus)

    return () => {
      signal.cancelled = true
      window.clearInterval(interval)
      window.removeEventListener('focus', onFocus)
    }
  }, [load])

  return (
    <>
      <PageHeader title="Messages" subtitle="Your conversations with other students" />

      {error && <Alert tone="error">{error}</Alert>}

      {threads === null && !error && (
        <div className="grid place-items-center py-12">
          <Spinner />
        </div>
      )}

      {threads?.length === 0 && (
        <EmptyState
          title="No conversations yet"
          description="Open a listing and tap Message Seller to start one."
        />
      )}

      {threads && threads.length > 0 && (
        <ul className="divide-y divide-gray-200 overflow-hidden rounded-2xl bg-white ring-1 ring-gray-200">
          {threads.map((thread) => {
            const name = thread.otherParticipant
              ? `${thread.otherParticipant.firstName} ${thread.otherParticipant.lastName}`
              : 'Unknown student'

            return (
              <li key={thread.conversationId}>
                <Link
                  to={`/messages/${thread.conversationId}`}
                  className="flex items-center gap-3 p-4 transition hover:bg-gray-50"
                >
                  <Avatar name={name} />

                  <span className="min-w-0 flex-1">
                    <span className="flex items-baseline justify-between gap-2">
                      <span className="truncate font-medium text-ink-900">{name}</span>
                      <span className="shrink-0 text-xs text-ink-400">
                        {relativeTime(thread.lastMessageAt)}
                      </span>
                    </span>

                    {thread.listingTitle && (
                      <span className="mt-0.5 block truncate text-xs text-brand-700">
                        About: {thread.listingTitle}
                      </span>
                    )}

                    <span className="mt-0.5 block truncate text-sm text-ink-500">
                      {thread.lastMessagePreview ?? 'No messages yet'}
                    </span>
                  </span>

                  {thread.unreadCount > 0 && (
                    <Badge tone="brand">{thread.unreadCount}</Badge>
                  )}
                </Link>
              </li>
            )
          })}
        </ul>
      )}
    </>
  )
}
