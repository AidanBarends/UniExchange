/*
  NotificationRow - one row in the /notifications list. Dense list style
  (border separators, not individual shadowed cards) to match how many of
  these can be on screen at once.

  OWNER: Joshua Reid Adams (230317693)
*/

import { NotificationIcon } from "./NotificationIcon";
import type { Notification } from "@/lib/api/types";

type NotificationRowProps = {
  notification: Notification;
  onOpen: (notification: Notification) => void;
};

function timeAgo(iso: string): string {
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(iso).toLocaleDateString("en-ZA", {
    day: "numeric",
    month: "short",
  });
}

export function NotificationRow({
  notification,
  onOpen,
}: NotificationRowProps) {
  const unread = !notification.read;

  return (
    <li>
      <button
        type="button"
        onClick={() => onOpen(notification)}
        className={
          "flex w-full items-start gap-3 px-4 py-3 text-left transition hover:bg-gray-50 " +
          "focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-brand-600 " +
          (unread ? "bg-brand-50/40" : "bg-white")
        }
      >
        <span className="relative mt-0.5 shrink-0">
          <NotificationIcon type={notification.type} />
          {unread && (
            <span
              aria-hidden="true"
              className="absolute -left-1.5 top-1/2 size-1.5 -translate-y-1/2 rounded-full bg-brand-600"
            />
          )}
        </span>

        <span className="min-w-0 flex-1">
          <span
            className={`block text-sm ${unread ? "font-semibold text-ink-900" : "font-medium text-ink-800"}`}
          >
            {notification.title}
          </span>
          {notification.content && (
            <span className="mt-0.5 block truncate text-sm text-ink-500">
              {notification.content}
            </span>
          )}
          <span className="mt-1 block text-xs text-ink-400">
            {timeAgo(notification.createdAt)}
          </span>
        </span>

        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.75"
          className="mt-1.5 size-4 shrink-0 text-ink-400"
        >
          <path d="m9 6 6 6-6 6" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>
    </li>
  );
}
