/*
  FeedSidebar - the desktop left rail from the T2 desktop mockup: a Sell Item
  button, the Categories list with counts ("All Categories 342"), a Condition
  Filter card, and a trust callout - matching the mockup's stacked rail.

  Counts are ACTIVE listings per category, computed once in FeedPage from a
  single GET /api/listings call. Hidden below `lg` - mobile uses CategoryChips.

  Owner: Joshua Reid Adams (230317693)
*/

import { useNavigate } from "react-router-dom";

import { CategoryIcon } from "./CategoryIcon";
import { ConditionFilter } from "./ConditionFilter";
import { TrustCallout } from "./TrustCallout";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { Category } from "@/lib/api/types";

type FeedSidebarProps = {
  categories: Category[];
  /** categoryId -> number of ACTIVE listings, from FeedPage. */
  counts: Record<number, number>;
  /** Total ACTIVE listings across all categories (the "All Categories" count). */
  totalActive: number;
  /** null means "All Categories". */
  activeCategoryId: number | null;
  onSelectCategory: (categoryId: number | null) => void;
};

export function FeedSidebar({
  categories,
  counts,
  totalActive,
  activeCategoryId,
  onSelectCategory,
}: FeedSidebarProps) {
  const navigate = useNavigate();

  const row =
    "flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-sm transition " +
    "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600";
  const rowActive = "bg-brand-50 font-semibold text-brand-800";
  const rowIdle = "text-ink-700 hover:bg-gray-50";

  return (
    <aside className="hidden w-60 shrink-0 lg:block">
      <div className="sticky top-24 space-y-4">
        <Button onClick={() => navigate("/listings/new")}>+ Sell Item</Button>

        <Card className="p-2">
          <p className="px-2 pb-2 pt-1 text-xs font-semibold uppercase tracking-wide text-ink-400">
            Categories
          </p>
          <ul className="space-y-0.5">
            <li>
              <button
                type="button"
                onClick={() => onSelectCategory(null)}
                aria-pressed={activeCategoryId === null}
                className={`${row} ${activeCategoryId === null ? rowActive : rowIdle}`}
              >
                <span className="flex items-center gap-2">
                  <svg
                    aria-hidden="true"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    className="size-4"
                  >
                    <rect x="3" y="3" width="7" height="7" rx="1" />
                    <rect x="14" y="3" width="7" height="7" rx="1" />
                    <rect x="3" y="14" width="7" height="7" rx="1" />
                    <rect x="14" y="14" width="7" height="7" rx="1" />
                  </svg>
                  All Categories
                </span>
                <span className="text-xs text-ink-400">{totalActive}</span>
              </button>
            </li>

            {categories.map((category) => (
              <li key={category.categoryId}>
                <button
                  type="button"
                  onClick={() => onSelectCategory(category.categoryId)}
                  aria-pressed={activeCategoryId === category.categoryId}
                  className={`${row} ${activeCategoryId === category.categoryId ? rowActive : rowIdle}`}
                >
                  <span className="flex min-w-0 items-center gap-2">
                    <CategoryIcon
                      name={category.name}
                      className="size-4 shrink-0"
                    />
                    <span className="truncate">{category.name}</span>
                  </span>
                  <span className="text-xs text-ink-400">
                    {counts[category.categoryId] ?? 0}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </Card>

        <ConditionFilter />

        <TrustCallout />
      </div>
    </aside>
  );
}
