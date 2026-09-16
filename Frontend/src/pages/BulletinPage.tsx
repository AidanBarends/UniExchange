/*
  Campus bulletin board.

  OWNER: Aidan Barends (230255639) - previously unassigned, picked up after
  Product Details.
  ROUTE: /bulletin

  IMPORTANT - the team's mockup for this page still shows more than the
  backend supports: NO likes entity, NO comments entity, NO tags entity.
  BulletinPost now has a real category field (see BulletinPostCategory in
  types.ts) - Filter Feed, the composer's category picker, and each post's
  category badge are all wired to it for real. None of the rest is built.

  NOTE: you POST `isFacultyAnnouncement` but the response comes back as
  `facultyAnnouncement` (Jackson strips the `is` prefix on boolean getters) -
  see the comment in src/lib/api/types.ts.

  status is PUBLISHED | HIDDEN | REMOVED - the backend returns all of them
  from both GET /api/bulletin-posts and GET .../category/:category, so
  PUBLISHED-only filtering happens here either way.

  Your own components go in src/components/bulletin/.
*/

import { useCallback, useEffect, useState } from 'react'

import { useAuth } from '@/auth/useAuth'
import { CampusNewsSidebar } from '@/components/bulletin/CampusNewsSidebar'
import { FilterFeedSidebar } from '@/components/bulletin/FilterFeedSidebar'
import { PostCard } from '@/components/bulletin/PostCard'
import { PostComposer } from '@/components/bulletin/PostComposer'
import { PageHeader } from '@/components/layout/PageHeader'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { bulletinApi } from '@/lib/api/bulletin'
import { ApiError } from '@/lib/api/client'
import type { BulletinPost, BulletinPostCategory, User } from '@/lib/api/types'
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
 * matches the scaffold's own TODO note. Shared by the initial load, filter
 * changes, and inserting a freshly-created post, so nothing can jump above a
 * pinned announcement just because it's newest. */
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
  // null = "All Posts". Owned here, not inside FilterFeedSidebar, since it
  // decides which endpoint load() calls.
  const [selectedCategory, setSelectedCategory] = useState<BulletinPostCategory | null>(null)

  const load = useCallback(async (category: BulletinPostCategory | null) => {
    setState({ status: 'loading' })

    let posts: BulletinPost[]
    try {
      posts = category === null ? await bulletinApi.list() : await bulletinApi.byCategory(category)
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
    // same pattern used in ListingDetailsPage.tsx. Re-runs whenever the
    // filter changes, which is exactly what should re-fetch.
    Promise.resolve().then(() => load(selectedCategory))
  }, [selectedCategory, load])

  const handleCreatePost = async (values: BulletinPostValues) => {
    if (!user) return // page is behind ProtectedRoute, but keeps this honest either way

    const created = await bulletinApi.create({
      authorId: user.userId,
      title: values.title,
      content: values.content,
      status: 'PUBLISHED',
      isFacultyAnnouncement: false,
      category: values.category,
    })

    // Only insert into the visible list if it actually belongs to the
    // current filter - posting an Event while filtered to Lost & Found
    // shouldn't make it appear where it doesn't belong. It's still created
    // either way; just not shown under a filter it doesn't match.
    if (selectedCategory === null || created.category === selectedCategory) {
      setState((previous) => ({
        status: 'ready',
        posts: sortPosts(previous.status === 'ready' ? [created, ...previous.posts] : [created]),
      }))
    }

    // Already know who this is - no need to re-fetch your own user record.
    setAuthors((previous) => new Map(previous).set(user.userId, user))
  }

  // Belt-and-suspenders: the Edit/Delete buttons only render for isOwner
  // already, but that's a UI decision, not enforcement - BulletinPostController's
  // PUT and DELETE now also reject a non-author server-side (403), so this
  // is genuine defense in depth, not the only thing standing in the way.
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
      category: values.category,
    })

    setState((previous) => {
      if (previous.status !== 'ready') return { status: 'ready', posts: [updated] }

      // If editing moved this post out of the currently-selected category,
      // it should disappear from view rather than sit there contradicting
      // the active filter.
      if (selectedCategory !== null && updated.category !== selectedCategory) {
        return {
          status: 'ready',
          posts: previous.posts.filter((p) => p.bulletinPostId !== updated.bulletinPostId),
        }
      }

      return {
        status: 'ready',
        posts: sortPosts(previous.posts.map((p) => (p.bulletinPostId === updated.bulletinPostId ? updated : p))),
      }
    })
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

      <div className="grid gap-6 lg:grid-cols-4">
        <div className="hidden lg:block lg:col-span-1">
          <FilterFeedSidebar selected={selectedCategory} onSelect={setSelectedCategory} />
        </div>

        <div className="space-y-4 lg:col-span-2">
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
                <Button variant="ghost" onClick={() => void load(selectedCategory)}>
                  Try again
                </Button>
              }
            />
          )}

          {state.status === 'ready' && state.posts.length === 0 && (
            <EmptyState
              title={selectedCategory === null ? 'Nothing posted yet' : 'Nothing in this category yet'}
              description={
                selectedCategory === null
                  ? 'Be the first to share something with the campus.'
                  : 'Try a different filter, or be the first to post here.'
              }
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

        <div className="lg:col-span-1">
          <CampusNewsSidebar />
        </div>
      </div>
    </>
  )
}