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
import { PostCard } from '@/components/bulletin/PostCard'
import { PostComposer } from '@/components/bulletin/PostComposer'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { bulletinApi } from '@/lib/api/bulletin'
import { ApiError } from '@/lib/api/client'
import type { BulletinPost, User } from '@/lib/api/types'
import { usersApi } from '@/lib/api/users'
import type { BulletinPostValues } from '@/lib/schemas'

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
  const [actionError, setActionError] = useState<string | null>(null)

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

  // Belt-and-suspenders: the Edit/Delete buttons only render for isOwner
  // already, but that's a UI decision, not enforcement - BulletinPostController's
  // PUT and DELETE accept the request from any authenticated user, not just
  // the post's author. Refusing here client-side closes nothing on its own -
  // anyone can still call the API directly - but it stops this page from
  // being the thing that fires an unauthorized request. The real fix has to
  // be a server-side ownership check.
  const handleUpdatePost = async (post: BulletinPost, values: BulletinPostValues) => {
    if (!user || user.userId !== post.authorId) {
      throw new Error('Only the author can edit this post.')
    }

    const updated = await bulletinApi.update(post.bulletinPostId, {
      authorId: post.authorId,
      title: values.title,
      content: values.content,
      status: post.status,
      isFacultyAnnouncement: post.facultyAnnouncement,
    })

    setState((previous) => ({
      status: 'ready',
      posts:
        previous.status === 'ready'
          ? sortPosts(previous.posts.map((p) => (p.bulletinPostId === updated.bulletinPostId ? updated : p)))
          : [updated],
    }))
  }

  const handleDeletePost = async (post: BulletinPost) => {
    if (!user || user.userId !== post.authorId) {
      setActionError('Only the author can delete this post.')
      return
    }

    try {
      await bulletinApi.remove(post.bulletinPostId)
      setState((previous) =>
        previous.status === 'ready'
          ? { status: 'ready', posts: previous.posts.filter((p) => p.bulletinPostId !== post.bulletinPostId) }
          : previous,
      )
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : 'Could not delete this post.')
    }
  }

  return (
    <>
      <PageHeader title="Campus bulletin" subtitle="Announcements and notices" />

      <div className="grid gap-6 md:grid-cols-3">
        <div className="space-y-4 md:col-span-2">
          <PostComposer onSubmit={handleCreatePost} />

          {actionError && <Alert>{actionError}</Alert>}

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

          {state.status === 'ready' &&
            state.posts.map((post) => {
              const author = authors.get(post.authorId)
              const authorName = author ? `${author.firstName} ${author.lastName}` : null
              const isOwner = user?.userId === post.authorId

              return (
                <PostCard
                  key={post.bulletinPostId}
                  post={post}
                  authorName={authorName}
                  isOwner={isOwner}
                  formatRelativeTime={formatRelativeTime}
                  onSave={(values) => handleUpdatePost(post, values)}
                  onDelete={() => handleDeletePost(post)}
                />
              )
            })}
        </div>

        <div className="md:col-span-1">
          <CampusNewsSidebar />
        </div>
      </div>
    </>
  )
}