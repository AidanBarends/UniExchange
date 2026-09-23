/*
  The five tab icons plus the notification bell.

  Hand-written rather than an icon package, matching Logo and Button: 24x24
  viewBox, fill="none", stroke="currentColor", strokeWidth 2, rounded caps.
  Size them with Tailwind's size-* utility at the call site.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

type IconProps = { className?: string }

const BASE = {
  viewBox: '0 0 24 24',
  fill: 'none' as const,
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
  'aria-hidden': true,
}

export function FeedIcon({ className = 'size-5' }: IconProps) {
  return (
    <svg {...BASE} className={className}>
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V21h14V9.5" />
    </svg>
  )
}

export function BulletinIcon({ className = 'size-5' }: IconProps) {
  return (
    <svg {...BASE} className={className}>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <path d="M7 9h10M7 13h6" />
    </svg>
  )
}

export function SellIcon({ className = 'size-5' }: IconProps) {
  return (
    <svg {...BASE} className={className}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8v8M8 12h8" />
    </svg>
  )
}

export function MessagesIcon({ className = 'size-5' }: IconProps) {
  return (
    <svg {...BASE} className={className}>
      <path d="M21 15a2 2 0 0 1-2 2H8l-4 4V6a2 2 0 0 1 2-2h13a2 2 0 0 1 2 2Z" />
    </svg>
  )
}

export function ProfileIcon({ className = 'size-5' }: IconProps) {
  return (
    <svg {...BASE} className={className}>
      <circle cx="12" cy="8" r="3.5" />
      <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
    </svg>
  )
}

export function BellIcon({ className = 'size-5' }: IconProps) {
  return (
    <svg {...BASE} className={className}>
      <path d="M18 8a6 6 0 0 0-12 0c0 6-2 7-2 7h16s-2-1-2-7" />
      <path d="M10.5 20a2 2 0 0 0 3 0" />
    </svg>
  )
}
