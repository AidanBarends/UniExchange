/*
  CategoryIcon - a small stroke icon per category, used in the feed sidebar
  rows (the T2 desktop mockup shows an icon next to every category).

  Categories come from the database, so we match on name keywords and fall
  back to a tag icon for anything unmapped. Stroke style mirrors the app's
  existing icon look (currentColor, 1.5 width, 24 viewBox).

  Owner: Joshua Reid Adams (230317693)
*/

type CategoryIconProps = {
  name: string
  className?: string
}

export function CategoryIcon({ name, className = 'size-4' }: CategoryIconProps) {
  const n = name.toLowerCase()

  const iconProps = {
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.5,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
    className,
    'aria-hidden': true,
  }

  if (n.includes('text') || n.includes('book') || n.includes('study')) {
    // Book
    return (
      <svg {...iconProps}>
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
      </svg>
    )
  }

  if (n.includes('electron') || n.includes('device') || n.includes('laptop') || n.includes('phone')) {
    // CPU chip
    return (
      <svg {...iconProps}>
        <rect x="5" y="5" width="14" height="14" rx="2" />
        <rect x="10" y="10" width="4" height="4" />
        <path d="M9 2v3M15 2v3M9 19v3M15 19v3M2 9h3M2 15h3M19 9h3M19 15h3" />
      </svg>
    )
  }

  if (n.includes('cloth') || n.includes('fashion') || n.includes('apparel')) {
    // Shirt
    return (
      <svg {...iconProps}>
        <path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z" />
      </svg>
    )
  }

  if (n.includes('service') || n.includes('repair')) {
    // Wrench
    return (
      <svg {...iconProps}>
        <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
      </svg>
    )
  }

  if (n.includes('furnitur') || n.includes('desk') || n.includes('chair') || n.includes('room')) {
    // Armchair
    return (
      <svg {...iconProps}>
        <path d="M19 9V6a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v3" />
        <path d="M3 16a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-5a2 2 0 0 0-4 0v2H7v-2a2 2 0 0 0-4 0Z" />
        <path d="M5 18v2M19 18v2" />
      </svg>
    )
  }

  // Tag - fallback for any other category
  return (
    <svg {...iconProps}>
      <path d="M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z" />
      <circle cx="7.5" cy="7.5" r="0.5" fill="currentColor" />
    </svg>
  )
}
