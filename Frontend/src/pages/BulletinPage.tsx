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

import { useAuth } from '@/auth/useAuth'
import { PageHeader } from '@/components/layout/PageHeader'
import { CampusNewsSidebar } from '@/components/bulletin/CampusNewsSidebar'
import { PostActions } from '@/components/bulletin/PostActions'
import { PostComposer } from '@/components/bulletin/PostComposer'
import { formatRelativeTime } from '@/components/bulletin/relativeTime'
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
import type { BulletinPostValues } from '@/lib/schemas'

/** Faculty announcements pinned to the top, newest first within each group -
 * matches the scaffold's own TODO note. Shared by the initial load and by
 * inserting a freshly-created post, so a new (non-announcement) post can
 * never jump above a pinned announcement just because it's newest. */
function sortPosts(posts: BulletinPost[]): BulletinPost[] {
  return [...posts].sort((a, b) => {
    if (a.facultyAnnouncement !== b.facultyAnnouncement) {
      return a.facultyAnnouncement ? -1 : 1
    }
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
}

type LoadState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; posts: BulletinPost[] }

export function BulletinPage() {
  const { user } = useAuth()
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

    const published = sortPosts(posts.filter((post) => post.status === 'PUBLISHED'))

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

  const handleCreatePost = async (values: BulletinPostValues) => {
    if (!user) return // page is behind ProtectedRoute, but keeps this honest either way

    const created = await bulletinApi.create({
      authorId: user.userId,
      title: values.title,
      content: values.content,
      status: 'PUBLISHED',
      isFacultyAnnouncement: false,
    })

    setState((previous) => ({
      status: 'ready',
      posts: sortPosts(previous.status === 'ready' ? [created, ...previous.posts] : [created]),
    }))

    // Already know who this is - no need to re-fetch your own user record.
    setAuthors((previous) => new Map(previous).set(user.userId, user))
  }

  // No try/catch here on purpose, same as handleCreatePost above - PostActions
  // (via PostComposer's edit mode) is what knows how to show the failure, so
  // the error is left to propagate there rather than being swallowed here.
  const handleUpdatePost = async (post: BulletinPost, values: BulletinPostValues) => {
    const updated = await bulletinApi.update(post.bulletinPostId, {
      authorId: post.authorId,
      title: values.title,
      content: values.content,
      status: post.status,
      isFacultyAnnouncement: post.facultyAnnouncement,
    })

    setState((previous) =>
      previous.status === 'ready'
        ? {
            status: 'ready',
            posts: sortPosts(
              previous.posts.map((existing) =>
                existing.bulletinPostId === post.bulletinPostId ? updated : existing,
              ),
            ),
          }
        : previous,
    )
  }

  /** Same no-try/catch reasoning as handleUpdatePost - PostActions shows the error. */
  const handleDeletePost = async (post: BulletinPost) => {
    await bulletinApi.remove(post.bulletinPostId)

    setState((previous) =>
      previous.status === 'ready'
        ? { status: 'ready', posts: previous.posts.filter((existing) => existing.bulletinPostId !== post.bulletinPostId) }
        : previous,
    )
  }

  return (
    <>
      <PageHeader title="Campus bulletin" subtitle="Announcements and notices" />

      <div className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-2">
          <div className="mb-4">
            <PostComposer onSubmit={handleCreatePost} />
          </div>

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
                const isOwner = user?.userId === post.authorId

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

                    {isOwner && (
                      <PostActions
                        post={post}
                        onUpdate={(values) => handleUpdatePost(post, values)}
                        onDelete={() => handleDeletePost(post)}
                      />
                    )}
                  </Card>
                )
              })}
            </div>
          )}
        </div>

        <div className="md:col-span-1">
          <CampusNewsSidebar />
        </div>
      </div>
    </>
  )
}