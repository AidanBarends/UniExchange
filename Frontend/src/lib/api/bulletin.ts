/*
  Campus bulletin board. OWNER: unassigned

  GET is public. Reminder: you send `isFacultyAnnouncement` but read back
  `facultyAnnouncement`.
*/

import { authedRequest, request } from './client'
import type { BulletinPost, BulletinPostCategory, BulletinPostImage, BulletinPostStatus } from './types'

export const bulletinApi = {
  list: () => request<BulletinPost[]>('/api/bulletin-posts'),

  byId: (bulletinPostId: number | string) =>
    request<BulletinPost>(`/api/bulletin-posts/${bulletinPostId}`),

  /** Faculty-flagged posts only. */
  announcements: () => request<BulletinPost[]>('/api/bulletin-posts/announcements'),

  byAuthor: (authorId: number) => request<BulletinPost[]>(`/api/bulletin-posts/author/${authorId}`),

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

  imagesFor: (bulletinPostId: number | string) =>
    request<BulletinPostImage[]>(`/api/bulletin-post-images/bulletin-post/${bulletinPostId}`),

  /** NOTE: you send `isPrimary`, but the response comes back as `primary`. */
  addImage: (body: {
    bulletinPostId: number
    imageUrl: string
    position: number
    isPrimary: boolean
  }) => authedRequest<BulletinPostImage>('/api/bulletin-post-images', { method: 'POST', body }),

  updateImage: (
    imageId: number,
    body: { bulletinPostId: number; imageUrl: string; position: number; isPrimary: boolean },
  ) => authedRequest<BulletinPostImage>(`/api/bulletin-post-images/${imageId}`, { method: 'PUT', body }),

  removeImage: (imageId: number) =>
    authedRequest<void>(`/api/bulletin-post-images/${imageId}`, { method: 'DELETE' }),
}
