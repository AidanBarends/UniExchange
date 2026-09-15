/*
  "Campus News" sidebar from the mockup - real data, not a fabricated
  section: GET /api/bulletin-posts/announcements returns exactly the
  isFacultyAnnouncement posts this sidebar needs.

  This is a second, independent fetch of the same underlying posts already
  pinned to the top of the main feed (BulletinPage sorts facultyAnnouncement
  posts first). That duplication is intentional, not a bug - it matches the
  mockup's layout, where announcements get a persistent side panel as well as
  being highlighted inline.

  The mockup tags one item "IMPORTANT" and leaves others plain, implying a
  priority/severity distinction - there's no such field on BulletinPost, so
  every item here is equally "a faculty announcement" by definition. Rather
  than invent which ones look more urgent, every item gets the same real
  "Announcement" badge (true for all of them) plus a left accent border for
  visual weight, instead of a fake one-off "IMPORTANT" tag.

  Deliberately skipped from the mockup: the "Quick Links" panel underneath
  Campus News (Community Guidelines / Help Center / Contact Admin) - none of
  those routes exist anywhere in the app yet, and linking to pages that
  don't exist is worse than not having the panel.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import { useEffect, useState } from 'react'

import { Badge } from '@/components/ui/Badge'
import { Card } from '@/components/ui/Card'
import { Spinner } from '@/components/ui/Spinner'
import { bulletinApi } from '@/lib/api/bulletin'
import type { BulletinPost } from '@/lib/api/types'

const MAX_ITEMS = 5

export function CampusNewsSidebar() {
  const [posts, setPosts] = useState<BulletinPost[] | null>(null)

  useEffect(() => {
    let cancelled = false
    void bulletinApi
      .announcements()
      .then((results) => {
        if (cancelled) return
        const sorted = [...results]
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .slice(0, MAX_ITEMS)
        setPosts(sorted)
      })
      .catch(() => {
        if (!cancelled) setPosts([])
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <Card>
      <p className="mb-3 text-sm font-medium text-ink-700">Campus News</p>

      {posts === null && (
        <div className="flex items-center gap-2 py-2">
          <Spinner label="Loading campus news" />
        </div>
      )}

      {posts !== null && posts.length === 0 && (
        <p className="text-sm text-ink-400 italic">No announcements right now.</p>
      )}

      {posts !== null && posts.length > 0 && (
        <ul className="space-y-3">
          {posts.map((post) => (
            <li
              key={post.bulletinPostId}
              className="border-b border-gray-100 border-l-2 border-l-brand-600 py-0.5 pl-3 pb-3 last:border-b-0 last:pb-0"
            >
              <Badge tone="brand">Announcement</Badge>
              <p className="mt-1 text-sm font-medium text-ink-900">{post.title}</p>
              <p className="mt-0.5 line-clamp-2 text-xs text-ink-500">{post.content}</p>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}