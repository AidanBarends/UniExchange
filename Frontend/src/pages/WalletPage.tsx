/*
  Wallet: balance, escrow, ledger and top-ups.

  ROUTE: /wallet

  Note the three figures at the top. `available` is what can be spent right now
  and is ALREADY net of anything held in escrow - money leaves the buyer's wallet
  the moment they buy. `held` is shown next to it so the difference is visible,
  never subtracted from it.
*/

import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'

import { PageHeader } from '@/components/layout/PageHeader'
import { TopUpForm } from '@/components/wallet/TopUpForm'
import { formatZar } from '@/components/wallet/money'
import { Alert } from '@/components/ui/Alert'
import { Badge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { walletApi } from '@/lib/api/wallet'
import { ApiError } from '@/lib/api/client'
import type { WalletSummary, WalletTransaction } from '@/lib/api/types'

/**
 * After returning from PayFast the balance may not have moved yet - the
 * confirming callback is server-to-server and can land a moment later. Rather
 * than showing a stale zero, re-check a few times before giving up.
 */
const POST_TOPUP_POLLS = 10
const POST_TOPUP_INTERVAL_MS = 2_000

export function WalletPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const justToppedUp = searchParams.get('topup') === 'done'
  const cancelledTopUp = searchParams.get('topup') === 'cancelled'

  const [summary, setSummary] = useState<WalletSummary | null>(null)
  const [ledger, setLedger] = useState<WalletTransaction[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [awaitingPayment, setAwaitingPayment] = useState(justToppedUp)

  const load = useCallback(async (signal?: { cancelled: boolean }) => {
    try {
      const [nextSummary, nextLedger] = await Promise.all([walletApi.summary(), walletApi.ledger()])
      if (!signal?.cancelled) {
        setSummary(nextSummary)
        setLedger(nextLedger)
        setError(null)
      }
      return nextSummary
    } catch (err: unknown) {
      if (!signal?.cancelled) {
        setError(err instanceof ApiError ? err.message : 'Something went wrong.')
      }
      return null
    }
  }, [])

  useEffect(() => {
    const signal = { cancelled: false }

    // Inline rather than calling load() directly: setState in an effect body
    // trips react-hooks/set-state-in-effect, and this matches how every other
    // page in the app fetches. load() is still used by the post-top-up poll,
    // which runs from a timer callback rather than the effect body.
    Promise.all([walletApi.summary(), walletApi.ledger()])
      .then(([nextSummary, nextLedger]) => {
        if (!signal.cancelled) {
          setSummary(nextSummary)
          setLedger(nextLedger)
          setError(null)
        }
      })
      .catch((err: unknown) => {
        if (!signal.cancelled) {
          setError(err instanceof ApiError ? err.message : 'Something went wrong.')
        }
      })

    return () => {
      signal.cancelled = true
    }
  }, [])

  // Poll briefly after a return from PayFast, then stop.
  useEffect(() => {
    if (!justToppedUp) return

    const signal = { cancelled: false }
    let attempts = 0
    const before = summary?.total

    const interval = window.setInterval(() => {
      attempts += 1
      void load(signal).then((fresh) => {
        if (signal.cancelled) return
        // Stop as soon as the total changes, or when we run out of patience.
        if ((fresh && fresh.total !== before) || attempts >= POST_TOPUP_POLLS) {
          window.clearInterval(interval)
          setAwaitingPayment(false)
          setSearchParams({}, { replace: true })
        }
      })
    }, POST_TOPUP_INTERVAL_MS)

    return () => {
      signal.cancelled = true
      window.clearInterval(interval)
    }
    // Intentionally keyed only on the flag: re-running this when the summary
    // changes would restart the poll every time it succeeds.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [justToppedUp])

  return (
    <>
      <PageHeader title="Wallet" subtitle="Pay other students, and get paid" />

      {error && <Alert tone="error">{error}</Alert>}

      {cancelledTopUp && <Alert tone="info">Top-up cancelled. Nothing was charged.</Alert>}

      {awaitingPayment && (
        <Alert tone="info">
          Waiting for PayFast to confirm your payment. This usually takes a few seconds.
        </Alert>
      )}

      {summary === null && !error ? (
        <div className="grid place-items-center py-12">
          <Spinner />
        </div>
      ) : (
        summary && (
          <div className="space-y-6">
            <div className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
              <span className="text-sm text-ink-500">Available to spend</span>
              <p className="mt-1 text-3xl font-semibold text-ink-900">
                {formatZar(summary.available)}
              </p>

              {Number(summary.held) > 0 && (
                <div className="mt-4 flex flex-wrap items-center gap-x-6 gap-y-2 border-t border-gray-200 pt-4 text-sm">
                  <span className="text-ink-500">
                    In escrow{' '}
                    <strong className="font-medium text-ink-900">{formatZar(summary.held)}</strong>
                  </span>
                  <span className="text-ink-500">
                    Total{' '}
                    <strong className="font-medium text-ink-900">{formatZar(summary.total)}</strong>
                  </span>
                </div>
              )}

              {Number(summary.held) > 0 && (
                <p className="mt-2 text-xs text-ink-400">
                  Money in escrow has left your wallet and is being held until you confirm you
                  received the item.
                </p>
              )}
            </div>

            <TopUpForm mode={summary.topUpMode} onSimulated={() => void load()} />

            <div className="overflow-hidden rounded-2xl bg-white ring-1 ring-gray-200">
              <h2 className="border-b border-gray-200 px-5 py-4 text-base font-semibold text-ink-900">
                Activity
              </h2>

              {ledger?.length === 0 ? (
                <div className="p-5">
                  <EmptyState
                    title="Nothing yet"
                    description="Top up your wallet to start buying from other students."
                  />
                </div>
              ) : (
                <ul className="divide-y divide-gray-200">
                  {ledger?.map((entry) => {
                    const incoming = entry.type === 'CREDIT' || entry.type === 'REFUND'
                    return (
                      <li key={entry.walletTransactionId} className="flex items-center gap-3 px-5 py-3">
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-sm text-ink-900">
                            {entry.description ?? entry.type}
                          </span>
                          <span className="block text-xs text-ink-400">
                            {new Date(entry.createdAt).toLocaleString()}
                          </span>
                        </span>

                        <Badge tone={incoming ? 'success' : 'neutral'}>
                          {incoming ? '+' : '-'}
                          {formatZar(entry.amount)}
                        </Badge>
                      </li>
                    )
                  })}
                </ul>
              )}
            </div>
          </div>
        )
      )}
    </>
  )
}
