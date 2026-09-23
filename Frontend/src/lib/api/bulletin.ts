/*
  Campus bulletin board. OWNER: unassigned

  GET is public. Reminder: you send `isFacultyAnnouncement` but read back
  `facultyAnnouncement`.
*/

import { authedRequest, request } from './client'
import type { BulletinPost, BulletinPostStatus } from './types'

export const bulletinApi = {
  list: () => request<BulletinPost[]>('/api/bulletin-posts'),

  byId: (bulletinPostId: number | string) =>
    request<BulletinPost>(`/api/bulletin-posts/${bulletinPostId}`),

  /** Faculty-flagged posts only. */
  announcements: () => request<BulletinPost[]>('/api/bulletin-posts/announcements'),

  byAuthor: (authorId: number) => request<BulletinPost[]>(`/api/bulletin-posts/author/${authorId}`),

  create: (body: {
    authorId: number
    title: string
    content: string
    status: BulletinPostStatus
    isFacultyAnnouncement: boolean
  }) => authedRequest<BulletinPost>('/api/bulletin-posts', { method: 'POST', body }),
}
