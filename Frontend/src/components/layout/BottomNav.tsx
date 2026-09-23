/*
  Mobile tab bar. Hidden from sm up, where TopBar shows the same destinations
  as a horizontal row instead.

  AppLayout adds pb-24 to <main> so content is never hidden behind this.
*/

import { Link, useLocation } from 'react-router-dom'

import { NAV_ITEMS } from './navigation'

export function BottomNav() {
  const { pathname } = useLocation()

  return (
    <nav
      aria-label="Primary"
      className="fixed inset-x-0 bottom-0 z-10 border-t border-gray-200 bg-white/95 backdrop-blur sm:hidden"
    >
      <ul className="mx-auto flex max-w-3xl">
        {NAV_ITEMS.map(({ to, label, Icon, match }) => {
          const active = match(pathname)
          return (
            <li key={to} className="flex-1">
              <Link
                to={to}
                aria-current={active ? 'page' : undefined}
                className={
                  'flex flex-col items-center gap-1 py-2.5 text-xs font-medium transition ' +
                  (active ? 'text-brand-700' : 'text-ink-400 hover:text-ink-700')
                }
              >
                <Icon className="size-5" />
                {label}
              </Link>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
