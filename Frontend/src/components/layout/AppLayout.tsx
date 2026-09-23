/*
  The shell every signed-in page renders inside.

  Mounted once as a layout route in App.tsx, so no page writes its own header,
  nav or sign-out button. If you are building a page, you render only its
  content - start with <PageHeader> and go from there.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { Outlet } from 'react-router-dom'

import { BottomNav } from './BottomNav'
import { TopBar } from './TopBar'

export function AppLayout() {
  return (
    <div className="min-h-dvh bg-brand-50/40">
      <TopBar />

      {/* pb-24 keeps content clear of the fixed mobile tab bar. */}
      <main className="mx-auto max-w-3xl px-4 py-6 pb-24 sm:pb-8">
        <Outlet />
      </main>

      <BottomNav />
    </div>
  )
}
