/*
  Purchases and sales.

  ROUTE: /purchases

  This is where escrow becomes visible to the student: a PENDING row is money
  that has left the buyer's wallet and has not reached the seller. The buyer's
  "I received it" button is the thing that actually pays the seller, and only
  once that has happened can either side review the other.
*/

import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { PageHeader } from '@/components/layout/PageHeader'
import { ReviewForm } from '@/components/reviews/ReviewForm'
import { formatZar } from '@/components/wallet/money'
import { Alert } from '@/components/ui/Alert'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { Spinner } from '@/components/ui/Spinner'
import { useAuth } from '@/auth/useAuth'
import { ApiError } from '@/lib/api/client'
import { reviewsApi } from '@/lib/api/reviews'
import { purchasesApi } from '@/lib/api/wallet'
import type { Transaction } from '@/lib/api/types'

export function PurchasesPage() {
  const { session } = useAuth()
  const myUserId = session?.userId ?? 0

  const [transactions, setTransactions] = useState<Transaction[] | null>(null)
  const [reviewable, setReviewable] = useState<number[]>([])
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [reviewingId, setReviewingId] = useState<number | null>(null)

  const load = useCallback(async (signal?: { cancelled: boolean }) => {
    try {
      const [history, pending] = await Promise.all([
        purchasesApi.history(),
        reviewsApi.pending(),
      ])
      if (!signal?.cancelled) {
        setTransactions(history)
        setReviewable(pending.map((transaction) => transaction.transactionId))
        setError(null)
      }
    } catch (err: unknown) {
      if (!signal?.cancelled) {
        setError(err instanceof ApiError ? err.message : 'Something went wrong.')
      }
    }
  }, [])

  useEffect(() => {
    const signal = { cancelled: false }

    // Inline rather than calling load() directly: setState in an effect body
    // trips react-hooks/set-state-in-effect, and this matches how every other
    // page in the app fetches. load() is still used for refreshes after an
    // action, where it runs from an event handler rather than an effect.
    Promise.all([purchasesApi.history(), reviewsApi.pending()])
      .then(([history, pending]) => {
        if (!signal.cancelled) {
          setTransactions(history)
          setReviewable(pending.map((transaction) => transaction.transactionId))
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

  async function act(transactionId: number, action: 'confirm' | 'cancel') {
    setBusyId(transactionId)
    setError(null)
    try {
      await (action === 'confirm'
        ? purchasesApi.confirm(transactionId)
        : purchasesApi.cancel(transactionId))
      await load()
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'That did not work.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <>
      <PageHeader title="Purchases" subtitle="What you have bought and sold" />

      {error && <Alert tone="error">{error}</Alert>}

      {transactions === null && !error && (
        <div className="grid place-items-center py-12">
          <Spinner />
        </div>
      )}

      {transactions?.length === 0 && (
        <EmptyState
          title="Nothing yet"
          description="When you buy something with your wallet it will show up here."
        />
      )}

      <ul className="space-y-3">
        {transactions?.map((transaction) => {
          const iAmBuyer = transaction.buyerId === myUserId
          const canReview = reviewable.includes(transaction.transactionId)

          return (
            <li
              key={transaction.transactionId}
              className="rounded-2xl bg-white p-4 ring-1 ring-gray-200"
            >
              <div className="flex flex-wrap items-center gap-x-3 gap-y-2">
                <Badge tone={iAmBuyer ? 'brand' : 'neutral'}>
                  {iAmBuyer ? 'Bought' : 'Sold'}
                </Badge>

                <Link
                  to={`/listings/${transaction.listingId}`}
                  className="font-medium text-ink-900 underline-offset-2 hover:underline"
                >
                  Listing #{transaction.listingId}
                </Link>

                <span className="ml-auto font-semibold text-ink-900">
                  {formatZar(transaction.amount)}
                </span>
              </div>

              <div className="mt-2 flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-ink-500">
                <StatusBadge status={transaction.status} iAmBuyer={iAmBuyer} />
                <span>{new Date(transaction.createdAt).toLocaleDateString()}</span>
              </div>

              {transaction.status === 'PENDING' && (
                <div className="mt-3 flex flex-wrap gap-2">
                  {iAmBuyer && (
                    <Button
                      className="w-auto px-3"
                      loading={busyId === transaction.transactionId}
                      onClick={() => act(transaction.transactionId, 'confirm')}
                    >
                      I received it
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    className="w-auto px-3"
                    loading={busyId === transaction.transactionId}
                    onClick={() => act(transaction.transactionId, 'cancel')}
                  >
                    Cancel and refund
                  </Button>
                </div>
              )}

              {transaction.status === 'COMPLETED' && canReview && (
                <div className="mt-3">
                  {reviewingId === transaction.transactionId ? (
                    <ReviewForm
                      transactionId={transaction.transactionId}
                      subject={iAmBuyer ? 'the seller' : 'the buyer'}
                      onDone={() => {
                        setReviewingId(null)
                        void load()
                      }}
                    />
                  ) : (
                    <Button
                      variant="ghost"
                      className="w-auto px-3"
                      onClick={() => setReviewingId(transaction.transactionId)}
                    >
                      Leave a review
                    </Button>
                  )}
                </div>
              )}
            </li>
          )
        })}
      </ul>
    </>
  )
}

function StatusBadge({ status, iAmBuyer }: { status: Transaction['status']; iAmBuyer: boolean }) {
  if (status === 'PENDING') {
    return (
      <Badge tone="warning">
        {iAmBuyer ? 'Held in escrow - confirm when it arrives' : 'Awaiting buyer confirmation'}
      </Badge>
    )
  }
  if (status === 'COMPLETED') return <Badge tone="success">Complete</Badge>
  if (status === 'CANCELLED') return <Badge tone="neutral">Cancelled and refunded</Badge>
  return <Badge tone="danger">Failed</Badge>
}
