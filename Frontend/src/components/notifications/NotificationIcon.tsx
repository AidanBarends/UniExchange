/*
  NotificationIcon - the tinted icon square used on each notification row and
  in the detail modal. One place for the type -> icon/color mapping so the
  row and the modal can never drift apart.

  OWNER: Joshua Reid Adams (230317693)
*/

import type { Notification } from "@/lib/api/types";

type NotificationIconProps = {
  type: Notification["type"];
  className?: string;
};

/* Tinted-square background/foreground per type - same palette family as
   Badge's tones (brand/emerald/amber/neutral), just applied to a square
   instead of a pill so it reads as an icon slot, not a status label. */
const TONE: Record<Notification["type"], string> = {
  MESSAGE: "bg-brand-50 text-brand-700",
  LISTING: "bg-amber-50 text-amber-700",
  TRANSACTION: "bg-emerald-50 text-emerald-700",
  BULLETIN: "bg-brand-50 text-brand-700",
  SYSTEM: "bg-gray-100 text-ink-500",
};

function IconPath({ type }: { type: Notification["type"] }) {
  switch (type) {
    case "MESSAGE":
      return (
        <path
          d="M4 5h16v11H7l-3 3V5Z"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      );
    case "LISTING":
      return (
        <>
          <path
            d="m3 12 8.5-8.5a2 2 0 0 1 1.42-.6L19 3l.1 6.08a2 2 0 0 1-.6 1.42L10 19l-7-7Z"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <circle cx="14.5" cy="8.5" r="1" fill="currentColor" stroke="none" />
        </>
      );
    case "TRANSACTION":
      return (
        <>
          <rect x="3" y="6" width="18" height="13" rx="2" />
          <path d="M3 10h18M7 15h4" strokeLinecap="round" />
        </>
      );
    case "BULLETIN":
      return (
        <path
          d="M3 11v2a2 2 0 0 0 2 2h1l3 4v-4h8a2 2 0 0 0 2-2v-6a2 2 0 0 0-2-2H8L3 9v2Z"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      );
    case "SYSTEM":
      return (
        <>
          <path
            d="M12 3 4 6v5c0 5 3.5 8.5 8 10 4.5-1.5 8-5 8-10V6l-8-3Z"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d="m9 12 2 2 4-4"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </>
      );
  }
}

export function NotificationIcon({
  type,
  className = "size-9",
}: NotificationIconProps) {
  return (
    <span
      className={`grid shrink-0 place-items-center rounded-xl ${TONE[type]} ${className}`}
    >
      <svg
        aria-hidden="true"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        className="size-[55%]"
      >
        <IconPath type={type} />
      </svg>
    </span>
  );
}
