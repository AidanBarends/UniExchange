/*
  Homepage feed - what is for sale on campus (T2 mockup: "2 Marketplace Feed").

  OWNER: Joshua Reid Adams (230317693)
  ROUTE: /feed   (this is where students land after signing in)

  API used (all public GETs, through lib/api modules - never fetch directly):
   - listingsApi.search({ campusId, categoryId, title })  GET /api/listings/search
     (backend already returns ACTIVE listings only)
   - listingsApi.categories()                             GET /api/categories
   - listingsApi.list()                                   GET /api/listings
     (one-off, powers the sidebar category counts)
   - authApi.campuses()                                   GET /api/campuses
   - bulletinApi.list()                                    GET /api/bulletin-posts
     (filtered to PUBLISHED, newest LIVE_FEED_LIMIT, powers the right-rail
     live feed - no author name shown yet, see CampusLiveFeed.tsx)

  The signed-in student's campus (useAuth().user?.campusId) is the default
  campus filter - that is the whole "hyper-local" point of the product.

  LAYOUT: three columns on large screens, matching the desktop mockup -
  FeedSidebar (categories/filters) | main feed | right rail (live activity +
  safe-exchange callout). The right rail is presentational scaffolding for
  now (see CampusLiveFeed.tsx / SafeExchangeCard.tsx) - hidden below `xl` so
  it never competes with the feed on medium screens.

  UX details (borrowed patterns: Preline skeleton loading, Origin UI filter
  pills, standard sort control):
   - skeleton grid on first load instead of a spinner
   - previous results stay visible (dimmed) while filters refetch
   - sort control: newest / price up / price down, applied client-side
   - active-filter pills in a result toolbar, each individually removable
   - search box has an inline clear button

  Components used only by this page live in src/components/feed/.
*/

import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "@/auth/useAuth";
import { ActiveFilters } from "@/components/feed/ActiveFilters";
import { CampusLiveFeed } from "@/components/feed/CampusLiveFeed";
import { CategoryChips } from "@/components/feed/CategoryChips";
import { FeedSidebar } from "@/components/feed/FeedSidebar";
import { ListingCardSkeleton } from "@/components/feed/ListingCardSkeleton";
import { ListingGrid } from "@/components/feed/ListingGrid";
import { SafeExchangeCard } from "@/components/feed/SafeExchangeCard";
import { PageHeader } from "@/components/layout/PageHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { TextField } from "@/components/ui/TextField";
import { authApi } from "@/lib/api/auth";
import { bulletinApi } from "@/lib/api/bulletin";
import { listingsApi } from "@/lib/api/listings";
import type { BulletinPost, Campus, Category, Listing } from "@/lib/api/types";
import { usersApi } from "@/lib/api/users";

/** How many recent bulletin posts the live-feed card shows. */
const LIVE_FEED_LIMIT = 6;

const SEARCH_DEBOUNCE_MS = 350;

type SortKey = "newest" | "priceAsc" | "priceDesc";

const SORTERS: Record<SortKey, (a: Listing, b: Listing) => number> = {
  newest: (a, b) => b.createdAt.localeCompare(a.createdAt),
  priceAsc: (a, b) => a.price - b.price,
  priceDesc: (a, b) => b.price - a.price,
};

/* Decorative dot-grid backdrop for the empty state (Pattern Craft style). */
const DOT_GRID =
  "bg-[radial-gradient(circle,_theme(colors.brand.200)_1px,_transparent_1px)] [background-size:16px_16px]";

export function FeedPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Reference data (loaded once) + per-category ACTIVE counts for the sidebar.
  const [categories, setCategories] = useState<Category[]>([]);
  const [campuses, setCampuses] = useState<Campus[]>([]);
  const [counts, setCounts] = useState<Record<number, number>>({});

  // Recent bulletin posts for the right-rail live feed. null = still loading;
  // [] is a real "nothing posted yet" state, not an error.
  const [livePosts, setLivePosts] = useState<BulletinPost[] | null>(null);
  // authorId -> display name, resolved only for the authors of livePosts.
  const [authorNames, setAuthorNames] = useState<Record<number, string>>({});

  // Filters. campusId defaults to the student's own campus.
  const [campusId, setCampusId] = useState<number | null>(
    user?.campusId ?? null,
  );
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [searchInput, setSearchInput] = useState("");
  const [title, setTitle] = useState("");
  const [sortKey, setSortKey] = useState<SortKey>("newest");

  /*
    Results. `listings` stays null until the first successful response arrives;
    while later requests are in flight the previous grid stays up (stale-while-
    revalidate, dimmed) instead of flashing a spinner on every filter change.
  */
  const [listings, setListings] = useState<Listing[] | null>(null);
  const [firstLoadDone, setFirstLoadDone] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  // Debounce the search box before it becomes a request parameter.
  useEffect(() => {
    const timer = setTimeout(
      () => setTitle(searchInput.trim()),
      SEARCH_DEBOUNCE_MS,
    );
    return () => clearTimeout(timer);
  }, [searchInput]);

  // Categories, campuses and count tallies never change while the page is open.
  useEffect(() => {
    let cancelled = false;

    async function loadReferenceData() {
      try {
        const [categoryList, campusList, everyListing] = await Promise.all([
          listingsApi.categories(),
          authApi.campuses(),
          listingsApi.list(),
        ]);
        if (cancelled) return;

        setCategories(categoryList);
        setCampuses(campusList);

        const tallies: Record<number, number> = {};
        for (const listing of everyListing) {
          if (listing.status === "ACTIVE") {
            tallies[listing.categoryId] =
              (tallies[listing.categoryId] ?? 0) + 1;
          }
        }
        setCounts(tallies);
      } catch {
        // Counts and pickers are non-critical; the listings effect surfaces
        // errors users actually care about.
      }
    }

    async function loadLiveFeed() {
      try {
        // bulletinApi.list() has no "recent"/status filter server-side, so
        // filter to PUBLISHED and take the newest few here.
        const allPosts = await bulletinApi.list();
        if (cancelled) return;

        const recent = allPosts
          .filter((post) => post.status === "PUBLISHED")
          .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
          .slice(0, LIVE_FEED_LIMIT);

        setLivePosts(recent);

        // Resolve just the distinct authors behind these posts (at most
        // LIVE_FEED_LIMIT lookups, usually fewer since students repost).
        // usersApi.byId is authedRequest - fine here, the whole page is
        // behind ProtectedRoute. A failed lookup just leaves that author
        // unresolved; CampusLiveFeed falls back to a generic label for it.
        const uniqueAuthorIds = [
          ...new Set(recent.map((post) => post.authorId)),
        ];
        const authorEntries = await Promise.all(
          uniqueAuthorIds.map(
            async (authorId): Promise<[number, string] | null> => {
              try {
                const author = await usersApi.byId(authorId);
                const name: string = `${author.firstName} ${author.lastName}`;
                return [authorId, name];
              } catch {
                return null;
              }
            },
          ),
        );
        if (cancelled) return;

        const resolved: Record<number, string> = {};
        for (const entry of authorEntries) {
          if (entry !== null) resolved[entry[0]] = entry[1];
        }
        setAuthorNames(resolved);
      } catch {
        // The live feed is non-critical chrome; fail quietly to an empty list
        // rather than surfacing an error banner over the whole page.
        if (!cancelled) setLivePosts([]);
      }
    }

    loadReferenceData();
    loadLiveFeed();
    return () => {
      cancelled = true;
    };
  }, []);

  /*
    Listings follow the active filters. StrictMode-safe via the cancelled flag,
    and every setState happens in an async callback - none in the effect body.
  */
  useEffect(() => {
    let cancelled = false;

    listingsApi
      .search({
        campusId: campusId ?? undefined,
        categoryId: categoryId ?? undefined,
        title: title || undefined,
      })
      .then((results) => {
        if (cancelled) return;
        setListings(results);
      })
      .catch((err: unknown) => {
        if (!cancelled)
          setError(
            err instanceof Error ? err.message : "Something went wrong.",
          );
      })
      .finally(() => {
        if (!cancelled) setFirstLoadDone(true);
      });

    return () => {
      cancelled = true;
    };
  }, [campusId, categoryId, title, refreshKey]);

  const campusNames: Record<number, string> = {};
  for (const campus of campuses) campusNames[campus.campusId] = campus.name;

  const totalActive = useMemo(
    () => Object.values(counts).reduce((sum, count) => sum + count, 0),
    [counts],
  );

  // Sorting is client-side: search already returned every matching ACTIVE row.
  const sortedListings = useMemo(() => {
    if (!listings) return null;
    return [...listings].sort(SORTERS[sortKey]);
  }, [listings, sortKey]);

  const activeCampusName =
    campusId !== null ? campusNames[campusId] : undefined;
  const activeCategoryName = categories.find(
    (c) => c.categoryId === categoryId,
  )?.name;
  const hasActiveFilters = Boolean(
    activeCampusName || activeCategoryName || title,
  );

  function clearAllFilters() {
    setCampusId(null);
    setCategoryId(null);
    setSearchInput("");
    setTitle("");
  }

  return (
    <>
      <PageHeader
        title="Recent Listings"
        subtitle="What's for sale on your campus"
      />

      {error && (
        <Alert tone="error">
          <div className="flex items-center justify-between gap-3">
            <span>{error}</span>
            <button
              type="button"
              onClick={() => setRefreshKey((key) => key + 1)}
              className="shrink-0 font-semibold underline"
            >
              Retry
            </button>
          </div>
        </Alert>
      )}

      <div className="mt-2 flex items-start gap-6">
        <FeedSidebar
          categories={categories}
          counts={counts}
          totalActive={totalActive}
          activeCategoryId={categoryId}
          onSelectCategory={setCategoryId}
        />

        <div className="min-w-0 flex-1">
          {/* The desktop mockup keeps search in the top bar; that area is the
              shared layout, so ours lives in the page like the mobile mockup. */}
          <div className="grid gap-3 sm:grid-cols-[1fr_auto]">
            <div className="relative">
              <TextField
                label="Search"
                name="feedSearch"
                placeholder="Search textbooks, electronics…"
                value={searchInput}
                onChange={(event) => setSearchInput(event.target.value)}
              />
              {searchInput && (
                <button
                  type="button"
                  onClick={() => setSearchInput("")}
                  aria-label="Clear search"
                  className="absolute right-2.5 top-[38px] text-ink-400 hover:text-ink-700"
                >
                  <svg
                    aria-hidden="true"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    className="size-4"
                  >
                    <path d="M18 6 6 18M6 6l12 12" />
                  </svg>
                </button>
              )}
            </div>

            <div className="grid gap-3 sm:grid-cols-[10rem_9rem]">
              <Select
                label="Campus"
                name="feedCampus"
                value={campusId ?? ""}
                onChange={(event) =>
                  setCampusId(
                    event.target.value === ""
                      ? null
                      : Number(event.target.value),
                  )
                }
              >
                <option value="">All campuses</option>
                {campuses.map((campus) => (
                  <option key={campus.campusId} value={campus.campusId}>
                    {campus.name}
                  </option>
                ))}
              </Select>

              <Select
                label="Sort"
                name="feedSort"
                value={sortKey}
                onChange={(event) => setSortKey(event.target.value as SortKey)}
              >
                <option value="newest">Newest</option>
                <option value="priceAsc">Price: low to high</option>
                <option value="priceDesc">Price: high to low</option>
              </Select>
            </div>
          </div>

          <div className="mt-3">
            <CategoryChips
              categories={categories}
              activeCategoryId={categoryId}
              onSelectCategory={setCategoryId}
            />
          </div>

          {/* Status line matching the mockup's "Showing N active items" row,
              with a live indicator on the right. Removable filter pills only
              render once a filter is actually active, same as before. */}
          {firstLoadDone && !error && sortedListings && (
            <div className="mt-4 flex flex-wrap items-center justify-between gap-2">
              {hasActiveFilters ? (
                <ActiveFilters
                  resultCount={sortedListings.length}
                  campusName={activeCampusName}
                  categoryName={activeCategoryName}
                  search={title || undefined}
                  onClearCampus={() => setCampusId(null)}
                  onClearCategory={() => setCategoryId(null)}
                  onClearSearch={() => setSearchInput("")}
                  onClearAll={clearAllFilters}
                />
              ) : (
                <p className="text-sm text-ink-500">
                  Showing {sortedListings.length} active{" "}
                  {sortedListings.length === 1 ? "item" : "items"} on campus
                </p>
              )}

              <span className="inline-flex items-center gap-1.5 text-xs font-medium text-emerald-700">
                <span
                  className="size-1.5 rounded-full bg-emerald-500"
                  aria-hidden="true"
                />
                Real-time feed
              </span>
            </div>
          )}

          <div className="mt-4">
            {!firstLoadDone ? (
              /* First load: skeleton grid, same shape as the real cards. */
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {Array.from({ length: 6 }, (_, index) => (
                  <ListingCardSkeleton key={index} />
                ))}
              </div>
            ) : error ? null : sortedListings === null ||
              sortedListings.length === 0 ? (
              <div className={`${DOT_GRID} rounded-2xl p-1`}>
                <div className="rounded-xl bg-white/80 backdrop-blur-[1px]">
                  <div className="p-8 text-center">
                    <p className="text-sm font-medium text-ink-700">
                      Nothing for sale here yet
                    </p>
                    <p className="mx-auto mt-1.5 max-w-sm text-sm text-ink-500">
                      No active listings match these filters. Try another campus
                      or category - or be the first to sell.
                    </p>
                    <div className="mt-4 flex justify-center">
                      <Button onClick={() => navigate("/listings/new")}>
                        Sell something
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <ListingGrid
                listings={sortedListings}
                campusNames={campusNames}
              />
            )}
          </div>

          {/* Footer prompt, matching the mockup's "didn't find it?" card. */}
          <div className="mt-6 rounded-2xl border border-dashed border-gray-300 p-6 text-center">
            <p className="text-sm text-ink-500">
              Looking for something specific that isn't listed?
            </p>
            <button
              type="button"
              onClick={() => navigate("/bulletin")}
              className="mt-1 text-sm font-semibold text-brand-700 hover:text-brand-900"
            >
              Post a "Wanted" request on the Campus Bulletin →
            </button>
          </div>
        </div>

        <aside className="hidden w-80 shrink-0 space-y-4 xl:block">
          <div className="sticky top-24 space-y-4">
            <CampusLiveFeed
              posts={livePosts ?? []}
              authorNames={authorNames}
              loading={livePosts === null}
            />
            <SafeExchangeCard />
          </div>
        </aside>
      </div>
    </>
  );
}
