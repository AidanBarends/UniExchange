/*
  Shared by BulletinPage and CampusNewsSidebar - both bulletin-only, so this
  is a same-feature extraction, not the kind of cross-team shared util the
  comment on ListingDetailsPage's copy of this logic warns about.
*/

const absoluteDateFormatter = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'medium' })

export function formatRelativeTime(iso: string): string {
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
