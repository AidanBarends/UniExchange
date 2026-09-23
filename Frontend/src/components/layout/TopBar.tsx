/*
  App header: wordmark home link, desktop nav, notifications bell, sign out.

  The bell is always visible; the unread dot is a slot the notifications owner
  can light up once GET /api/notifications/user/:id/unread is wired.
*/

import { Link, useLocation } from 'react-router-dom'

import { useAuth } from '@/auth/useAuth'
import { Button } from '@/components/ui/Button'

import { BellIcon } from './NavIcons'
import { Logo } from './Logo'
import { NAV_ITEMS } from './navigation'

export function TopBar() {
  const { signOut } = useAuth()
  const { pathname } = useLocation()
  const onNotifications = pathname.startsWith('/notifications')

  return (
    <header className="sticky top-0 z-10 border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-3xl items-center gap-3 px-4 py-3">
        <Link to="/feed" aria-label="UniExchange home" className="rounded-lg">
          <Logo />
        </Link>

        {/* Desktop nav - the phone gets BottomNav instead. */}
        <nav aria-label="Primary" className="ml-4 hidden sm:block">
          <ul className="flex items-center gap-1">
            {NAV_ITEMS.map(({ to, label, match }) => {
              const active = match(pathname)
              return (
                <li key={to}>
                  <Link
                    to={to}
                    aria-current={active ? 'page' : undefined}
                    className={
                      'rounded-lg px-3 py-1.5 text-sm font-medium transition ' +
                      (active
                        ? 'bg-brand-50 text-brand-800'
                        : 'text-ink-500 hover:bg-gray-50 hover:text-ink-900')
                    }
                  >
                    {label}
                  </Link>
                </li>
              )
            })}
          </ul>
        </nav>

        <div className="ml-auto flex items-center gap-1">
          <Link
            to="/notifications"
            aria-label="Notifications"
            aria-current={onNotifications ? 'page' : undefined}
            className={
              'relative rounded-lg p-2 transition ' +
              (onNotifications
                ? 'bg-brand-50 text-brand-800'
                : 'text-ink-500 hover:bg-gray-50 hover:text-ink-900')
            }
          >
            <BellIcon className="size-5" />
            {/*
              TODO (notifications owner): render this dot only when
              notificationsApi.unreadForUser(userId) comes back non-empty.
              <span className="absolute right-1.5 top-1.5 size-2 rounded-full bg-red-500" />
            */}
          </Link>

          <Button variant="ghost" onClick={signOut} className="w-auto px-3">
            Sign out
          </Button>
        </div>
      </div>
    </header>
  )
}
