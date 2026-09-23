/*
  Initials in a circle. No profile images exist in the domain yet, so this
  derives initials from a name and is deterministic per user.
*/

type AvatarProps = {
  name?: string | null
  className?: string
}

function initialsOf(name: string | null | undefined): string {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

export function Avatar({ name, className = 'size-10' }: AvatarProps) {
  return (
    <span
      aria-hidden="true"
      className={`${className} grid shrink-0 place-items-center rounded-full bg-brand-100 text-sm font-semibold text-brand-800`}
    >
      {initialsOf(name)}
    </span>
  )
}
