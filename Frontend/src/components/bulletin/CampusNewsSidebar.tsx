/*
  Faculty-announcement sidebar for the bulletin page.

  Backed by GET /api/bulletin-posts/announcements, which is
  findByIsFacultyAnnouncementTrue() on the backend - it does NOT filter by
  status the way GET /api/bulletin-posts does, so HIDDEN/REMOVED
  announcements come back too and are filtered out here, same as
  BulletinPage does for the main feed.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { useEffect, useState } from 'react'

import { formatRelativeTime } from './relativeTime'
import { Card } from '@/components/ui/Card'
import { Spinner } from '@/components/ui/Spinner'
import { bulletinApi } from '@/lib/api/bulletin'
import { ApiError } from '@/lib/api/client'
import type { BulletinPost } from '@/lib/api/types'

type LoadState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; posts: BulletinPost[] }

export function CampusNewsSidebar() {
  const [state, setState] = useState<LoadState>({ status: 'loading' })

  useEffect(() => {
    let cancelled = false

    bulletinApi
      .announcements()
      .then((posts) => {
        if (cancelled) return
        const published = posts
          .filter((post) => post.status === 'PUBLISHED')
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        setState({ status: 'ready', posts: published })
      })
      .catch((error: unknown) => {
        if (cancelled) return
        setState({
          status: 'error',
          message: error instanceof ApiError ? error.message : 'Something went wrong.',
        })
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <Card>
      <h2 className="text-sm font-semibold text-ink-900">Campus news</h2>

      {state.status === 'loading' && (
        <div className="grid place-items-center py-6">
          <Spinner label="Loading campus news" className="size-6" />
        </div>
      )}

      {state.status === 'error' && <p className="mt-2 text-sm text-ink-500">{state.message}</p>}

      {state.status === 'ready' && state.posts.length === 0 && (
        <p className="mt-2 text-sm text-ink-500">No announcements right now.</p>
      )}

      {state.status === 'ready' && state.posts.length > 0 && (
        <ul className="mt-3 space-y-3">
          {state.posts.map((post) => (
            <li key={post.bulletinPostId} className="border-t border-gray-100 pt-3 first:border-t-0 first:pt-0">
              <p className="text-sm font-medium text-ink-900">{post.title}</p>
              <p className="mt-1 line-clamp-2 text-sm text-ink-500">{post.content}</p>
              <p className="mt-1 text-xs text-ink-400">{formatRelativeTime(post.createdAt)}</p>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}
