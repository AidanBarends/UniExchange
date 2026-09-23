/*
  CampusLiveFeed - the right-rail "Campus Live Feed" card from the desktop
  mockup: a live-badge header and a scrollable list of recent activity
  (avatar, actor name, message, relative time).

  PLACEHOLDER DATA: there's no activity-feed endpoint yet. AuditLogController
  is the closest existing thing on the backend but it's admin-scoped, not a
  public activity stream. Swap ITEMS below for a real fetch (or drop this
  card) once that decision is made.

  Owner: Joshua Reid Adams (230317693)
*/

import { Avatar } from "@/components/ui/Avatar";
import { Card } from "@/components/ui/Card";

type LiveFeedItem = {
  id: string;
  actor: string;
  message: string;
  timeAgo: string;
};

const ITEMS: LiveFeedItem[] = [
  {
    id: "1",
    actor: "A student",
    message: "marked an item as traded",
    timeAgo: "5m ago",
  },
  {
    id: "2",
    actor: "A student",
    message: 'posted a "wanted" request on the Bulletin',
    timeAgo: "18m ago",
  },
  {
    id: "3",
    actor: "A student",
    message: "dropped the price on a listing",
    timeAgo: "32m ago",
  },
  {
    id: "4",
    actor: "A study group",
    message: "opened seats for tonight",
    timeAgo: "1h ago",
  },
  {
    id: "5",
    actor: "A student",
    message: "joined and was verified via student email",
    timeAgo: "2h ago",
  },
];

export function CampusLiveFeed() {
  return (
    <Card className="p-4">
      <div className="flex items-center justify-between">
        <p className="text-sm font-semibold text-ink-900">Campus Live Feed</p>
        <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-emerald-700">
          <span
            className="size-1.5 rounded-full bg-emerald-500"
            aria-hidden="true"
          />
          Live
        </span>
      </div>

      <ul className="mt-3 max-h-80 space-y-3 overflow-y-auto pr-1">
        {ITEMS.map((item) => (
          <li key={item.id} className="flex gap-2.5">
            <Avatar name={item.actor} className="size-7 shrink-0" />
            <div className="min-w-0">
              <p className="text-sm text-ink-700">
                <span className="font-semibold text-ink-900">{item.actor}</span>{" "}
                {item.message}
              </p>
              <p className="mt-0.5 text-xs text-ink-400">{item.timeAgo}</p>
            </div>
          </li>
        ))}
      </ul>

      <button
        type="button"
        className="mt-3 text-sm font-medium text-brand-700 hover:text-brand-900"
      >
        Open Campus Bulletin board →
      </button>
    </Card>
  );
}
