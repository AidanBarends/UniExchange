/*
  NotificationDetailModal - opens when a row is clicked. Deliberately shows
  only fields that actually exist on Notification (title, content, type,
  createdAt, read) - no fabricated seller handles, meetup locations, or
  escrow copy, since none of that is backed by real data yet (Notification
  has no structured fields for price/location/etc., just a plain `content`
  string). See notificationRoute.ts for the "what can I fit here later"
  path (e.g. enriching LISTING notifications with a real listing lookup).

  Marks the notification read as soon as it's opened (same optimistic
  update NotificationsPage already does for a direct row click) - the
  caller passes an already-updated `notification` in, this component
  doesn't call the API itself.

  OWNER: Joshua Reid Adams (230317693)
*/

import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

import { NotificationIcon } from "./NotificationIcon";
import { NOTIFICATION_TYPE_LABEL } from "./notificationLabels";
import { notificationRoute } from "./notificationRoute";
import { Button } from "@/components/ui/Button";
import type { Notification } from "@/lib/api/types";

type NotificationDetailModalProps = {
  notification: Notification;
  onClose: () => void;
};

function fullTimestamp(iso: string): string {
  const date = new Date(iso);
  const datePart = date.toLocaleDateString("en-ZA", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
  const timePart = date.toLocaleTimeString("en-ZA", {
    hour: "numeric",
    minute: "2-digit",
  });
  return `${datePart} at ${timePart}`;
}

export function NotificationDetailModal({
  notification,
  onClose,
}: NotificationDetailModalProps) {
  const navigate = useNavigate();
  const panelRef = useRef<HTMLDivElement>(null);
  const route = notificationRoute(notification);

  // Close on Escape; focus the panel on open for keyboard/screen-reader users.
  useEffect(() => {
    panelRef.current?.focus();
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-20 flex items-center justify-center bg-ink-900/40 p-4"
      onClick={onClose}
    >
      <div
        ref={panelRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="notification-modal-title"
        onClick={(event) => event.stopPropagation()}
        className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-5 shadow-lg focus:outline-none"
      >
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <NotificationIcon type={notification.type} className="size-10" />
            <span className="text-xs font-semibold uppercase tracking-wide text-ink-400">
              {NOTIFICATION_TYPE_LABEL[notification.type]}
            </span>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="rounded-lg p-1 text-ink-400 transition hover:bg-gray-50 hover:text-ink-700"
          >
            <svg
              aria-hidden="true"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              className="size-5"
            >
              <path d="M18 6 6 18M6 6l12 12" strokeLinecap="round" />
            </svg>
          </button>
        </div>

        <h2
          id="notification-modal-title"
          className="mt-3 text-lg font-semibold text-ink-900"
        >
          {notification.title}
        </h2>

        <p className="mt-1 flex items-center gap-1.5 text-xs text-ink-400">
          <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            className="size-3.5"
          >
            <circle cx="12" cy="12" r="9" />
            <path
              d="M12 7v5l3 2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          {fullTimestamp(notification.createdAt)}
        </p>

        {notification.content && (
          <p className="mt-4 whitespace-pre-line text-sm text-ink-700">
            {notification.content}
          </p>
        )}

        <div className="mt-6 flex items-center justify-end gap-2">
          <Button variant="ghost" onClick={onClose} className="w-auto px-3">
            Dismiss
          </Button>
          {route && (
            <Button
              onClick={() => navigate(route.path)}
              className="w-auto px-3"
            >
              {route.label} →
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}
