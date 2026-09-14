/*
  Campus bulletin board.

  OWNER: Aidan Barends (230255639) - previously unassigned.

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

  /**
   * PUT replaces the whole post - authorId/status/isFacultyAnnouncement have
   * to be sent again even when only title/content changed, since
   * BulletinPostRequest has no partial-update variant.
   *
   * NOTE: like create, this is permitAll on the backend (see SecurityConfig) -
   * PUT accepts the request from anyone, not just the post's author. Gating
   * the edit UI to isOwner (see PostActions) is a UI decision, not enforcement.
   */
  update: (
    bulletinPostId: number,
    body: {
      authorId: number
      title: string
      content: string
      status: BulletinPostStatus
      isFacultyAnnouncement: boolean
    },
  ) => authedRequest<BulletinPost>(`/api/bulletin-posts/${bulletinPostId}`, { method: 'PUT', body }),

  /** Same permitAll caveat as update - see the note there. */
  remove: (bulletinPostId: number) =>
    authedRequest<void>(`/api/bulletin-posts/${bulletinPostId}`, { method: 'DELETE' }),
}