/*
  groupByDay - splits a (already newest-first) notification list into the
  Today / Yesterday / Earlier This Week / Earlier sections shown on the page.
  Pure function so it's easy to test and doesn't care about React.

  OWNER: Joshua Reid Adams (230317693)
*/

import type { Notification } from "@/lib/api/types";

export type NotificationGroup = {
  label: string;
  items: Notification[];
};

function startOfDay(date: Date): number {
  return new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
  ).getTime();
}

export function groupByDay(notifications: Notification[]): NotificationGroup[] {
  const today = startOfDay(new Date());
  const yesterday = today - 24 * 60 * 60 * 1000;
  const weekAgo = today - 7 * 24 * 60 * 60 * 1000;

  const buckets = {
    today: [] as Notification[],
    yesterday: [] as Notification[],
    thisWeek: [] as Notification[],
    earlier: [] as Notification[],
  };

  for (const notification of notifications) {
    const day = startOfDay(new Date(notification.createdAt));
    if (day === today) buckets.today.push(notification);
    else if (day === yesterday) buckets.yesterday.push(notification);
    else if (day >= weekAgo) buckets.thisWeek.push(notification);
    else buckets.earlier.push(notification);
  }

  return [
    { label: "Today", items: buckets.today },
    { label: "Yesterday", items: buckets.yesterday },
    { label: "Earlier This Week", items: buckets.thisWeek },
    { label: "Earlier", items: buckets.earlier },
  ].filter((group) => group.items.length > 0);
}
