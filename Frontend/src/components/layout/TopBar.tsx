/*
  App header: wordmark home link, desktop nav, notifications bell, sign out.

  Unread dot: fetches notificationsApi.unreadForUser(userId) on mount, then
  keeps it current three ways -
   - polls every UNREAD_POLL_MS while the tab is open
   - refetches on window focus (switching back to the tab updates it without
     waiting for the interval)
   - refetches immediately when the /notifications page marks something
     read, via the tiny pub/sub in lib/notificationEvents.ts, so the dot
     clears live instead of lagging behind by up to a full poll interval

  No websocket/SSE anywhere else in this stack, so polling is the
  pragmatic choice here rather than introducing a new transport for one
  badge.
*/

import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";

import { useAuth } from "@/auth/useAuth";
import { Button } from "@/components/ui/Button";
import { notificationsApi } from "@/lib/api/notifications";
import { onNotificationsChanged } from "@/lib/notificationEvents";

import { BellIcon, WalletIcon } from "./NavIcons";
import { Logo } from "./Logo";
import { NAV_ITEMS } from "./navigation";

const UNREAD_POLL_MS = 45_000;

export function TopBar() {
  const { signOut, session } = useAuth();
  const { pathname } = useLocation();
  const onNotifications = pathname.startsWith("/notifications");
  const onWallet = pathname.startsWith("/wallet") || pathname.startsWith("/purchases");
  const userId = session?.userId;

  const [hasUnread, setHasUnread] = useState(false);

  useEffect(() => {
    if (!userId) return;

    let cancelled = false;

    function refetch() {
      notificationsApi
        .unreadForUser(userId as number)
        .then((unread) => {
          if (!cancelled) setHasUnread(unread.length > 0);
        })
        .catch(() => {
          // Non-critical chrome; leave the dot as it was on a failed check.
        });
    }

    refetch();
    const interval = setInterval(refetch, UNREAD_POLL_MS);
    window.addEventListener("focus", refetch);
    const unsubscribe = onNotificationsChanged(refetch);

    return () => {
      cancelled = true;
      clearInterval(interval);
      window.removeEventListener("focus", refetch);
      unsubscribe();
    };
  }, [userId]);

  return (
    <header className="sticky top-0 z-10 border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-screen-2xl items-center gap-3 px-4 py-3 sm:px-6 lg:px-8">
        <Link to="/feed" aria-label="UniExchange home" className="rounded-lg">
          <Logo />
        </Link>

        {/* Desktop nav - the phone gets BottomNav instead. */}
        <nav aria-label="Primary" className="ml-4 hidden sm:block">
          <ul className="flex items-center gap-1">
            {NAV_ITEMS.map(({ to, label, match }) => {
              const active = match(pathname);
              return (
                <li key={to}>
                  <Link
                    to={to}
                    aria-current={active ? "page" : undefined}
                    className={
                      "rounded-lg px-3 py-1.5 text-sm font-medium transition " +
                      (active
                        ? "bg-brand-50 text-brand-800"
                        : "text-ink-500 hover:bg-gray-50 hover:text-ink-900")
                    }
                  >
                    {label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        <div className="ml-auto flex items-center gap-1">
          {/* Wallet lives here rather than in NAV_ITEMS: the mobile tab bar
              already holds five destinations and a sixth makes each one too
              narrow to hit reliably. */}
          <Link
            to="/wallet"
            aria-label="Wallet"
            aria-current={onWallet ? "page" : undefined}
            className={
              "rounded-lg p-2 transition " +
              (onWallet
                ? "bg-brand-50 text-brand-800"
                : "text-ink-500 hover:bg-gray-50 hover:text-ink-900")
            }
          >
            <WalletIcon className="size-5" />
          </Link>

          <Link
            to="/notifications"
            aria-label={hasUnread ? "Notifications (unread)" : "Notifications"}
            aria-current={onNotifications ? "page" : undefined}
            className={
              "relative rounded-lg p-2 transition " +
              (onNotifications
                ? "bg-brand-50 text-brand-800"
                : "text-ink-500 hover:bg-gray-50 hover:text-ink-900")
            }
          >
            <BellIcon className="size-5" />
            {hasUnread && (
              <span
                aria-hidden="true"
                className="absolute right-1.5 top-1.5 size-2 rounded-full bg-red-500"
              />
            )}
          </Link>

          <Button variant="ghost" onClick={signOut} className="w-auto px-3">
            Sign out
          </Button>
        </div>
      </div>
    </header>
  );
}
