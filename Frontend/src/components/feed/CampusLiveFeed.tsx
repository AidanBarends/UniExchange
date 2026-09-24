/*
  CampusLiveFeed - the right-rail "Campus Live Feed" card from the desktop
  mockup. Backed by real bulletin posts now (GET /api/bulletin-posts via
  bulletinApi.list(), fetched once in FeedPage alongside the other reference
  data and passed down as `posts`).

  No fake rows: if there are no PUBLISHED posts yet, this renders a plain
  empty state instead of placeholder content, since the whole point is that
  it should only show real student activity.

  Author names: `posts` only carries `authorId`, so FeedPage separately
  resolves the distinct authors behind the visible posts via usersApi.byId
  and passes the result as `authorNames` (authorId -> "First Last"). If a
  particular lookup failed or hasn't resolved yet, that post falls back to
  "A student" rather than blocking the row.

  Owner: Joshua Reid Adams (230317693)
*/

import { useNavigate } from "react-router-dom";

import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import type { BulletinPost } from "@/lib/api/types";

type CampusLiveFeedProps = {
  /** Recent PUBLISHED bulletin posts, newest first. Pass [] while loading or empty. */
  posts: BulletinPost[];
  /** authorId -> "First Last", for whichever authors FeedPage managed to resolve. */
  authorNames: Record<number, string>;
  loading?: boolean;
};

const CATEGORY_LABEL: Record<BulletinPost["category"], string> = {
  GENERAL: "General",
  EVENT: "Event",
  STUDY_GROUP: "Study Group",
  LOST_AND_FOUND: "Lost & Found",
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

export function CampusLiveFeed({
  posts,
  authorNames,
  loading = false,
}: CampusLiveFeedProps) {
  const navigate = useNavigate();

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

      {loading ? (
        <div className="mt-3 space-y-3">
          {Array.from({ length: 3 }, (_, index) => (
            <div
              key={index}
              className="h-10 animate-pulse rounded-lg bg-gray-100"
            />
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="mt-3 rounded-lg border border-dashed border-gray-200 p-4 text-center">
          <p className="text-sm text-ink-500">No bulletin activity yet.</p>
          <p className="mt-0.5 text-xs text-ink-400">
            Posts on the Campus Bulletin will show up here as they happen.
          </p>
        </div>
      ) : (
        <ul className="mt-3 max-h-80 space-y-3 overflow-y-auto pr-1">
          {posts.map((post) => (
            <li key={post.bulletinPostId} className="flex gap-2.5">
              <span className="mt-0.5 shrink-0">
                <Badge tone={post.facultyAnnouncement ? "brand" : "neutral"}>
                  {CATEGORY_LABEL[post.category]}
                </Badge>
              </span>
              <div className="min-w-0">
                <p className="text-sm text-ink-700">
                  <span className="font-semibold text-ink-900">
                    {authorNames[post.authorId] ?? "A student"}
                  </span>{" "}
                  posted{" "}
                  <span className="font-medium text-ink-900">
                    "{post.title}"
                  </span>
                </p>
                <p className="mt-0.5 text-xs text-ink-400">
                  {timeAgo(post.createdAt)}
                </p>
              </div>
            </li>
          ))}
        </ul>
      )}

      <button
        type="button"
        onClick={() => navigate("/bulletin")}
        className="mt-3 text-sm font-medium text-brand-700 hover:text-brand-900"
      >
        Open Campus Bulletin board →
      </button>
    </Card>
  );
}
