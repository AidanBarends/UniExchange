/*
  Campus bulletin board.

  OWNER: Aidan Barends (230255639) - previously unassigned, picked up after
  Product Details.
  ROUTE: /bulletin

  IMPORTANT - the team's mockup for this page shows a lot more than the
  backend actually supports. BulletinPost only has: title, content, authorId,
  status, isFacultyAnnouncement, timestamps. There is NO likes entity, NO
  comments entity, NO tags entity, and NO post-type/category field
  (Events/Study Groups/Lost & Found in the mockup have nothing behind them).
  None of that is built here.

  NOTE: you POST `isFacultyAnnouncement` but the response comes back as
  `facultyAnnouncement` (Jackson strips the `is` prefix on boolean getters) -
  see the comment in src/lib/api/types.ts.

  status is PUBLISHED | HIDDEN | REMOVED - the backend returns all of them
  from GET /api/bulletin-posts, so PUBLISHED-only filtering happens here.

  Your own components go in src/components/bulletin/.
*/

import { useCallback, useEffect, useState } from 'react'

import { PageHeader } from '@/components/layout/PageHeader'
import { Avatar } from '@/components/ui/Avatar'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { bulletinApi } from '@/lib/api/bulletin'
import { ApiError } from '@/lib/api/client'
import type { BulletinPost, User } from '@/lib/api/types'
import { usersApi } from '@/lib/api/users'

/** Duplicated from ListingDetailsPage.tsx rather than pulled into a shared
 * util - a 12-line date formatter isn't worth introducing a new shared file
 * and coordinating with the team over, but if a THIRD page ends up needing
 * this, that's the point to actually raise it and consolidate. */
const absoluteDateFormatter = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'medium' })
function formatRelativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime()
  const diffMin = Math.floor(diffMs / 60_000)
  if (diffMin < 1) return 'Just now'
  if (diffMin < 60) return `${diffMin} minute${diffMin === 1 ? '' : 's'} ago`
  const diffHr = Math.floor(diffMin / 60)
  if (diffHr < 24) return `${diffHr} hour${diffHr === 1 ? '' : 's'} ago`
  const diffDay = Math.floor(diffHr / 24)
  if (diffDay < 7) return `${diffDay} day${diffDay === 1 ? '' : 's'} ago`
  return absoluteDateFormatter.format(new Date(iso))
}

type LoadState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; posts: BulletinPost[] }

export function BulletinPage() {
  const [state, setState] = useState<LoadState>({ status: 'loading' })
  // authorId -> User, so posts from the same person only fetch once.
  const [authors, setAuthors] = useState<Map<number, User>>(new Map())

  const load = useCallback(async () => {
    setState({ status: 'loading' })

    let posts: BulletinPost[]
    try {
      posts = await bulletinApi.list()
    } catch (error) {
      setState({
        status: 'error',
        message: error instanceof ApiError ? error.message : 'Something went wrong.',
      })
      return
    }

    const published = posts
      .filter((post) => post.status === 'PUBLISHED')
      .sort((a, b) => {
        // Faculty announcements pinned to the top, newest first within
        // each group - matches the scaffold's own TODO note.
        if (a.facultyAnnouncement !== b.facultyAnnouncement) {
          return a.facultyAnnouncement ? -1 : 1
        }
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })

    setState({ status: 'ready', posts: published })

    const uniqueAuthorIds = [...new Set(published.map((post) => post.authorId))]
    void Promise.all(uniqueAuthorIds.map((id) => usersApi.byId(id).catch(() => null))).then((users) => {
      setAuthors((previous) => {
        const next = new Map(previous)
        users.forEach((user, index) => {
          if (user) next.set(uniqueAuthorIds[index], user)
        })
        return next
      })
    })
  }, [])

  useEffect(() => {
    // Deferred a tick so the setState calls inside load() happen from an
    // async continuation rather than synchronously in the effect body -
    // same pattern used in ListingDetailsPage.tsx.
    Promise.resolve().then(() => load())
  }, [load])

  return (
    <>
      <PageHeader title="Campus bulletin" subtitle="Announcements and notices" />

      {state.status === 'loading' && (
        <div className="grid place-items-center py-16">
          <Spinner label="Loading bulletin" className="size-8" />
        </div>
      )}

      {state.status === 'error' && (
        <EmptyState
          title="Couldn't load the bulletin"
          description={state.message}
          action={
            <Button variant="ghost" onClick={() => void load()}>
              Try again
            </Button>
          }
        />
      )}

      {state.status === 'ready' && state.posts.length === 0 && (
        <EmptyState
          title="Nothing posted yet"
          description="Be the first to share something with the campus."
        />
      )}

      {state.status === 'ready' && state.posts.length > 0 && (
        <div className="space-y-4">
          {state.posts.map((post) => {
            const author = authors.get(post.authorId)
            const authorName = author ? `${author.firstName} ${author.lastName}` : null

            return (
              <Card key={post.bulletinPostId}>
                <div className="flex items-center gap-3">
                  <Avatar name={authorName} className="size-9" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-ink-900">{authorName ?? 'Someone'}</p>
                    <p className="text-xs text-ink-500">{formatRelativeTime(post.createdAt)}</p>
                  </div>
                  {post.facultyAnnouncement && <Badge tone="brand">Announcement</Badge>}
                </div>

                <h2 className="mt-3 text-sm font-semibold text-ink-900">{post.title}</h2>
                <p className="mt-1 whitespace-pre-wrap text-sm text-ink-700">{post.content}</p>
              </Card>
            )
          })}
        </div>
      )}
    </>
  )
}