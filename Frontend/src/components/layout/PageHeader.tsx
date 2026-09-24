/*
  The title block at the top of every page inside AppLayout. Use this rather
  than a bare <h1> so all pages line up.

    <PageHeader title="Feed" subtitle="What's for sale on your campus" />
    <PageHeader title="My listings" action={<Button>New</Button>} />
    <PageHeader title={<span className="flex items-center gap-2">Notifications <Badge>3</Badge></span>} />
*/

import type { ReactNode } from "react";

type PageHeaderProps = {
  /** Usually a plain string; can be a node when a badge/icon sits next to it. */
  title: ReactNode;
  subtitle?: ReactNode;
  /** Usually a Button; sits to the right of the title on wider screens. */
  action?: ReactNode;
};

export function PageHeader({ title, subtitle, action }: PageHeaderProps) {
  return (
    <div className="mb-6 flex flex-wrap items-start justify-between gap-3">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-ink-900 sm:text-3xl">
          {title}
        </h1>
        {subtitle && <p className="mt-1 text-sm text-ink-500">{subtitle}</p>}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  );
}
