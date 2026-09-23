/*
  Shell for the three auth screens. Mobile-first: a full-bleed card on a phone,
  a centred narrow card from sm up.
*/

import type { ReactNode } from 'react'

import { Logo } from './Logo'

type AuthLayoutProps = {
  title: string
  subtitle?: ReactNode
  children: ReactNode
  footer?: ReactNode
}

export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="min-h-dvh bg-brand-50/40 px-4 py-8 sm:grid sm:place-items-center sm:py-12">
      <main className="mx-auto w-full max-w-md">
        <div className="mb-6 flex justify-center">
          <Logo />
        </div>

        <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm sm:p-8">
          <h1 className="text-xl font-semibold tracking-tight text-ink-900">{title}</h1>
          {subtitle && <p className="mt-1.5 text-sm text-ink-500">{subtitle}</p>}

          <div className="mt-6">{children}</div>
        </div>

        {footer && <div className="mt-5 text-center text-sm text-ink-500">{footer}</div>}

        <p className="mt-8 text-center text-xs text-ink-400">
          A verified marketplace for CPUT students.
        </p>
      </main>
    </div>
  )
}
