/*
  Notifications. OWNER: unassigned

  Reminder: the unread flag is sent as `isRead` but arrives as `read`.
*/

import { authedRequest } from './client'
import type { Notification } from './types'

export const notificationsApi = {
  /** Newest first. */
  forUser: (userId: number) => authedRequest<Notification[]>(`/api/notifications/user/${userId}`),

  unreadForUser: (userId: number) =>
    authedRequest<Notification[]>(`/api/notifications/user/${userId}/unread`),

  markRead: (notificationId: number) =>
    authedRequest<Notification>(`/api/notifications/${notificationId}/read`, { method: 'PATCH' }),
}
