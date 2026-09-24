/*
  NotificationFilters - the All / Unread / Messages / Listings / Transactions
  / Bulletin / System pill row. Same pill visual language as
  components/feed/CategoryChips.tsx and ConditionFilter.tsx, kept local
  here rather than imported across pages per the components-are-page-scoped
  convention (see components/notifications/README.md).

  Filtering itself is client-side in NotificationsPage, same pattern as
  Feed's sort - notificationsApi.forUser already returns everything.

  OWNER: Joshua Reid Adams (230317693)
*/

import type { Notification } from "@/lib/api/types";

export type NotificationFilterKey = "ALL" | "UNREAD" | Notification["type"];

type NotificationFiltersProps = {
  active: NotificationFilterKey;
  unreadCount: number;
  onChange: (key: NotificationFilterKey) => void;
};

const TABS: { key: NotificationFilterKey; label: string }[] = [
  { key: "ALL", label: "All" },
  { key: "UNREAD", label: "Unread" },
  { key: "MESSAGE", label: "Messages" },
  { key: "LISTING", label: "Listings" },
  { key: "TRANSACTION", label: "Transactions" },
  { key: "BULLETIN", label: "Bulletin" },
  { key: "SYSTEM", label: "System" },
];

const PILL_BASE =
  "inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border px-3.5 py-1.5 text-sm font-medium transition " +
  "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600";
const PILL_ACTIVE = "border-brand-600 bg-brand-600 text-white";
const PILL_IDLE =
  "border-gray-300 bg-white text-ink-700 hover:border-brand-300";

export function NotificationFilters({
  active,
  unreadCount,
  onChange,
}: NotificationFiltersProps) {
  return (
    <div
      className="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1"
      role="tablist"
      aria-label="Filter notifications"
    >
      {TABS.map((tab) => (
        <button
          key={tab.key}
          type="button"
          role="tab"
          aria-selected={active === tab.key}
          onClick={() => onChange(tab.key)}
          className={`${PILL_BASE} ${active === tab.key ? PILL_ACTIVE : PILL_IDLE}`}
        >
          {tab.label}
          {tab.key === "UNREAD" && unreadCount > 0 && (
            <span
              className={
                "grid size-4 place-items-center rounded-full text-[10px] font-semibold " +
                (active === tab.key
                  ? "bg-white/25 text-white"
                  : "bg-brand-50 text-brand-700")
              }
            >
              {unreadCount > 9 ? "9+" : unreadCount}
            </span>
          )}
        </button>
      ))}
    </div>
  );
}
