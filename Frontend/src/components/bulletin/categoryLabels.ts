/*
  Display labels for BulletinPostCategory, shared by FilterFeedSidebar,
  PostComposer and PostCard so the three don't each hardcode their own copy
  of the same four strings.

  Owner: Aidan Barends (230255639), for /bulletin only.
*/

import type { BulletinPostCategory } from '@/lib/api/types'

export const CATEGORY_LABELS: Record<BulletinPostCategory, string> = {
  GENERAL: 'General',
  EVENT: 'Event',
  STUDY_GROUP: 'Study Group',
  LOST_AND_FOUND: 'Lost & Found',
}

/** The three real, filterable categories - GENERAL is the "no specific
 * category" default and isn't offered as a filter option of its own. */
export const FILTERABLE_CATEGORIES: BulletinPostCategory[] = ['EVENT', 'STUDY_GROUP', 'LOST_AND_FOUND']

/** All four, including GENERAL - what the composer's picker offers, since a
 * new post can legitimately just not fit any specific category. */
export const ALL_CATEGORIES: BulletinPostCategory[] = ['GENERAL', 'EVENT', 'STUDY_GROUP', 'LOST_AND_FOUND']
