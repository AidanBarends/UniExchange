/*
  404. Replaces a silent redirect to "/" - a mistyped URL used to bounce with no
  explanation, which is confusing when a route genuinely is not built yet.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { Link, useLocation } from 'react-router-dom'

import { Logo } from '@/components/layout/Logo'

export function NotFoundPage() {
  const { pathname } = useLocation()

  return (
    <div className="grid min-h-dvh place-items-center bg-brand-50/40 px-4">
      <main className="w-full max-w-md text-center">
        <div className="mb-6 flex justify-center">
          <Logo />
        </div>

        <div className="rounded-2xl border border-gray-200 bg-white p-8 shadow-sm">
          <p className="text-sm font-semibold uppercase tracking-wide text-brand-600">404</p>
          <h1 className="mt-1 text-xl font-semibold tracking-tight text-ink-900">
            We couldn&apos;t find that page
          </h1>
          <p className="mt-2 text-sm text-ink-500">
            Nothing lives at <code className="text-ink-700">{pathname}</code>.
          </p>

          <Link
            to="/feed"
            className="mt-6 inline-flex items-center justify-center rounded-lg bg-brand-600 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-brand-700"
          >
            Back to the feed
          </Link>
        </div>
      </main>
    </div>
  )
}
