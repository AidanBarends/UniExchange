import type { BulletinPostCategory } from '@/lib/api/types'

export const CATEGORY_LABELS: Record<BulletinPostCategory, string> = {
  GENERAL: 'General',
  EVENT: 'Event',
  STUDY_GROUP: 'Study Group',
  LOST_AND_FOUND: 'Lost & Found',
}

export const FILTERABLE_CATEGORIES: BulletinPostCategory[] = ['GENERAL', 'EVENT', 'STUDY_GROUP', 'LOST_AND_FOUND']

export const ALL_CATEGORIES: BulletinPostCategory[] = ['GENERAL', 'EVENT', 'STUDY_GROUP', 'LOST_AND_FOUND']
