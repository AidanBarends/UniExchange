/*
  notificationRoute - given a notification, decide where (if anywhere) it
  should take the user, and what to call that action.

  Deliberately conservative: entityType on the DTO is an untyped string
  (see types.ts), so this keys off `type` instead (a real enum) plus
  entityId. Only routes that actually exist in App.tsx are offered:

    MESSAGE     -> /messages/:entityId   ("Open Conversation")
    LISTING     -> /listings/:entityId   ("View Listing")
    BULLETIN    -> /bulletin             ("View on Bulletin")
    TRANSACTION -> /purchases            ("View Purchase")
    SYSTEM      -> no route

  When this returns null, the row/modal should just mark the notification
  read and not offer a navigation action - better than linking somewhere
  that 404s.

  OWNER: Joshua Reid Adams (230317693)
*/

import type { Notification } from "@/lib/api/types";

export function notificationRoute(
  notification: Notification,
): { path: string; label: string } | null {
  switch (notification.type) {
    case "MESSAGE":
      return notification.entityId
        ? {
            path: `/messages/${notification.entityId}`,
            label: "Open Conversation",
          }
        : null;
    case "LISTING":
      return notification.entityId
        ? { path: `/listings/${notification.entityId}`, label: "View Listing" }
        : null;
    case "BULLETIN":
      return { path: "/bulletin", label: "View on Bulletin" };
    case "TRANSACTION":
      // /purchases rather than a per-transaction page: entityId is a transaction
      // id, and the list is where the buyer confirms receipt or leaves a review,
      // which is what these notifications are prompting for.
      return { path: "/purchases", label: "View Purchase" };
    case "SYSTEM":
      return null;
  }
}
