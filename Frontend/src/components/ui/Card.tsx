/*
  The standard white panel. Pass `to` to make the whole card a link - listing
  cards in the feed want that.
*/

import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'

type CardProps = {
  children: ReactNode
  /** When set, the card becomes a router link to this path. */
  to?: string
  className?: string
}

const BASE = 'block rounded-2xl border border-gray-200 bg-white p-4 shadow-sm'
const INTERACTIVE = ' transition hover:border-brand-300 hover:shadow focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600'

export function Card({ children, to, className = '' }: CardProps) {
  if (to) {
    return (
      <Link to={to} className={`${BASE}${INTERACTIVE} ${className}`}>
        {children}
      </Link>
    )
  }

  return <div className={`${BASE} ${className}`}>{children}</div>
}
