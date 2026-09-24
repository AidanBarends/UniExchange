/*
  Notification inbox.

  OWNER: Joshua Reid Adams (230317693)
  ROUTE: /notifications

  API (through lib/api/notifications.ts - never fetch directly):
   - notificationsApi.forUser(userId)          GET /api/notifications/user/:id
                                                (already newest-first)
   - notificationsApi.markRead(notificationId) PATCH /api/notifications/:id/read

  Filtering (All/Unread/type) and day-grouping (Today/Yesterday/Earlier This
  Week/Earlier) are client-side, same "fetch once, slice locally" pattern as
  FeedPage's sort - forUser() already returns everything for this student.

  Marking read is optimistic: the row/modal update local state immediately,
  the PATCH fires in the background, and emitNotificationsChanged() tells
  TopBar's bell to refetch its count right away instead of waiting on its
  poll interval. See lib/notificationEvents.ts and TopBar.tsx.

  Routing off a notification (row click already marks read; the modal's
  primary button navigates) is centralized in notificationRoute.ts so the
  row and the modal can't disagree about where something goes.

  Components used only by this page live in src/components/notifications/.
*/

import { useEffect, useMemo, useState } from "react";

import { useAuth } from "@/auth/useAuth";
import { groupByDay } from "@/components/notifications/groupByDay";
import { NotificationDetailModal } from "@/components/notifications/NotificationDetailModal";
import {
  NotificationFilters,
  type NotificationFilterKey,
} from "@/components/notifications/NotificationFilters";
import { NotificationRow } from "@/components/notifications/NotificationRow";
import { NotificationsSafetyCallout } from "@/components/notifications/NotificationsSafetyCallout";
import { PageHeader } from "@/components/layout/PageHeader";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { notificationsApi } from "@/lib/api/notifications";
import type { Notification } from "@/lib/api/types";
import { emitNotificationsChanged } from "@/lib/notificationEvents";

export function NotificationsPage() {
  const { session } = useAuth();
  const userId = session?.userId;

  const [notifications, setNotifications] = useState<Notification[] | null>(
    null,
  );
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [filter, setFilter] = useState<NotificationFilterKey>("ALL");
  const [openNotification, setOpenNotification] = useState<Notification | null>(
    null,
  );

  useEffect(() => {
    if (!userId) return;
    let cancelled = false;

    notificationsApi
      .forUser(userId)
      .then((results) => {
        if (!cancelled) setNotifications(results);
      })
      .catch((err: unknown) => {
        if (!cancelled)
          setError(
            err instanceof Error ? err.message : "Something went wrong.",
          );
      });

    return () => {
      cancelled = true;
    };
  }, [userId, refreshKey]);

  const unreadCount = useMemo(
    () =>
      notifications?.filter((notification) => !notification.read).length ?? 0,
    [notifications],
  );

  const filtered = useMemo(() => {
    if (!notifications) return null;
    if (filter === "ALL") return notifications;
    if (filter === "UNREAD")
      return notifications.filter((notification) => !notification.read);
    return notifications.filter((notification) => notification.type === filter);
  }, [notifications, filter]);

  const groups = useMemo(
    () => (filtered ? groupByDay(filtered) : []),
    [filtered],
  );

  function markReadLocally(notificationId: number) {
    setNotifications(
      (current) =>
        current?.map((notification) =>
          notification.notificationId === notificationId
            ? { ...notification, read: true }
            : notification,
        ) ?? current,
    );
  }

  function openRow(notification: Notification) {
    setOpenNotification(notification);
    if (!notification.read) {
      markReadLocally(notification.notificationId);
      notificationsApi
        .markRead(notification.notificationId)
        .then(() => emitNotificationsChanged())
        .catch(() => {
          // Non-critical: worst case the row shows read but the backend
          // still has it unread, which a refresh will reconcile.
        });
    }
  }

  function markAllAsRead() {
    const unread =
      notifications?.filter((notification) => !notification.read) ?? [];
    if (unread.length === 0) return;

    setNotifications(
      (current) =>
        current?.map((notification) => ({ ...notification, read: true })) ??
        current,
    );

    Promise.allSettled(
      unread.map((notification) =>
        notificationsApi.markRead(notification.notificationId),
      ),
    ).then(() => emitNotificationsChanged());
  }

  return (
    <>
      <PageHeader
        title={
          <span className="flex items-center gap-2">
            Notifications
            {unreadCount > 0 && (
              <Badge tone="brand">{unreadCount} unread</Badge>
            )}
          </span>
        }
        subtitle="Messages, sales and campus news"
        action={
          unreadCount > 0 ? (
            <button
              type="button"
              onClick={markAllAsRead}
              className="text-sm font-medium text-brand-700 hover:text-brand-900"
            >
              ✓ Mark all as read
            </button>
          ) : undefined
        }
      />

      {error && (
        <Alert tone="error">
          <div className="flex items-center justify-between gap-3">
            <span>{error}</span>
            <button
              type="button"
              onClick={() => {
                setError(null);
                setRefreshKey((key) => key + 1);
              }}
              className="shrink-0 font-semibold underline"
            >
              Retry
            </button>
          </div>
        </Alert>
      )}

      <div className="mt-2">
        <NotificationFilters
          active={filter}
          unreadCount={unreadCount}
          onChange={setFilter}
        />
      </div>

      <div className="mt-4">
        {notifications === null && !error ? (
          <div className="space-y-2">
            {Array.from({ length: 5 }, (_, index) => (
              <div
                key={index}
                className="h-16 animate-pulse rounded-xl bg-gray-100"
              />
            ))}
          </div>
        ) : error ? null : groups.length === 0 ? (
          <EmptyState
            title={
              filter === "ALL"
                ? "You're all caught up"
                : "No notifications match this filter"
            }
            description={
              filter === "ALL"
                ? "Messages, offers and campus news will show up here as they happen."
                : "Try a different filter, or switch back to All."
            }
          />
        ) : (
          <div className="space-y-6">
            {groups.map((group) => (
              <div key={group.label}>
                <div className="flex items-baseline justify-between px-1">
                  <p className="text-xs font-semibold uppercase tracking-wide text-ink-400">
                    {group.label}
                  </p>
                  <p className="text-xs text-ink-400">
                    {group.items.length}{" "}
                    {group.items.length === 1 ? "item" : "items"}
                  </p>
                </div>
                <ul className="mt-2 divide-y divide-gray-100 overflow-hidden rounded-2xl border border-gray-200">
                  {group.items.map((notification) => (
                    <NotificationRow
                      key={notification.notificationId}
                      notification={notification}
                      onOpen={openRow}
                    />
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="mt-6">
        <NotificationsSafetyCallout />
      </div>

      {openNotification && (
        <NotificationDetailModal
          notification={openNotification}
          onClose={() => setOpenNotification(null)}
        />
      )}
    </>
  );
}
