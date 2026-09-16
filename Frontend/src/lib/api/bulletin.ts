/*
  Campus bulletin board.

  OWNER: Aidan Barends (230255639) - previously unassigned.

  GET is public. Reminder: you send `isFacultyAnnouncement` but read back
  `facultyAnnouncement`.

  update/remove now require auth and are ownership-checked server-side
  (BulletinPostController returns 403 for a non-author) - see
  BulletinPostController.java.
*/

import { authedRequest, request } from './client'
import type { BulletinPost, BulletinPostCategory, BulletinPostStatus } from './types'

export const bulletinApi = {
  list: () => request<BulletinPost[]>('/api/bulletin-posts'),

  byId: (bulletinPostId: number | string) =>
    request<BulletinPost>(`/api/bulletin-posts/${bulletinPostId}`),

  /** Faculty-flagged posts only. */
  announcements: () => request<BulletinPost[]>('/api/bulletin-posts/announcements'),

  byAuthor: (authorId: number) => request<BulletinPost[]>(`/api/bulletin-posts/author/${authorId}`),

  /** GET /api/bulletin-posts/category/:category - the path variable binds
   * directly to the BulletinPostCategory enum on the backend, so it must be
   * one of the exact enum names (e.g. "EVENT"), not a display label. */
  byCategory: (category: BulletinPostCategory) =>
    request<BulletinPost[]>(`/api/bulletin-posts/category/${category}`),

  create: (body: {
    authorId: number
    title: string
    content: string
    status: BulletinPostStatus
    isFacultyAnnouncement: boolean
    category: BulletinPostCategory
  }) => authedRequest<BulletinPost>('/api/bulletin-posts', { method: 'POST', body }),

  /**
   * PUT replaces the whole post - authorId/status/isFacultyAnnouncement have
   * to be sent again even when only title/content changed, since
   * BulletinPostRequest has no partial-update variant.
   */
  update: (
    bulletinPostId: number,
    body: {
      authorId: number
      title: string
      content: string
      status: BulletinPostStatus
      isFacultyAnnouncement: boolean
      category: BulletinPostCategory
    },
  ) => authedRequest<BulletinPost>(`/api/bulletin-posts/${bulletinPostId}`, { method: 'PUT', body }),

  remove: (bulletinPostId: number) =>
    authedRequest<void>(`/api/bulletin-posts/${bulletinPostId}`, { method: 'DELETE' }),
}
