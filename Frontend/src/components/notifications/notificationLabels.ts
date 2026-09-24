/*
  notificationLabels - plain constant, split out from NotificationIcon.tsx
  so that file can stay component-only (react-refresh/only-export-components
  needs every export from a component file to be a component).

  OWNER: Joshua Reid Adams (230317693)
*/

import type { Notification } from "@/lib/api/types";

export const NOTIFICATION_TYPE_LABEL: Record<Notification["type"], string> = {
  MESSAGE: "Message",
  LISTING: "Listing",
  TRANSACTION: "Transaction",
  BULLETIN: "Bulletin",
  SYSTEM: "System",
};
