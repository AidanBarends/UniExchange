/*
  The five destinations in the app shell, in one place so the mobile tab bar and
  the desktop nav can never drift apart.

  Kept out of the component files so Vite's fast refresh keeps working - a module
  that mixes components with other exports loses HMR.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import type { ComponentType } from 'react'

import { BulletinIcon, FeedIcon, MessagesIcon, ProfileIcon, SellIcon } from './NavIcons'

export type NavItem = {
  to: string
  label: string
  Icon: ComponentType<{ className?: string }>
  /** Highlight the tab for nested paths too, e.g. /messages/7 lights up Messages. */
  match: (pathname: string) => boolean
}

export const NAV_ITEMS: NavItem[] = [
  {
    to: '/feed',
    label: 'Feed',
    Icon: FeedIcon,
    match: (p) => p === '/feed' || (p.startsWith('/listings') && p !== '/listings/new'),
  },
  {
    to: '/bulletin',
    label: 'Bulletin',
    Icon: BulletinIcon,
    match: (p) => p.startsWith('/bulletin'),
  },
  {
    to: '/listings/new',
    label: 'Sell',
    Icon: SellIcon,
    match: (p) => p === '/listings/new',
  },
  {
    to: '/messages',
    label: 'Messages',
    Icon: MessagesIcon,
    match: (p) => p.startsWith('/messages'),
  },
  {
    to: '/profile',
    label: 'Profile',
    Icon: ProfileIcon,
    match: (p) => p.startsWith('/profile'),
  },
]
