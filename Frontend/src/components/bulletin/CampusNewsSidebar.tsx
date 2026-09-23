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