/*
  The title block at the top of every page inside AppLayout. Use this rather
  than a bare <h1> so all seven pages line up.

    <PageHeader title="Feed" subtitle="What's for sale on your campus" />
    <PageHeader title="My listings" action={<Button>New</Button>} />
*/

import type { ReactNode } from 'react'

type PageHeaderProps = {
  title: string
  subtitle?: ReactNode
  /** Usually a Button; sits to the right of the title on wider screens. */
  action?: ReactNode
}

export function PageHeader({ title, subtitle, action }: PageHeaderProps) {
  return (
    <div className="mb-6 flex flex-wrap items-start justify-between gap-3">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-ink-900">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-ink-500">{subtitle}</p>}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  )
}
