/*
  Wallet, top-ups and escrow purchases.

  The generic /api/wallets and /api/transactions endpoints are ADMIN-only on the
  backend - they accepted a userId from the request body, so any signed-in
  student could have credited their own wallet. Everything here uses /api/wallet
  (singular) and /api/purchases, which act on whoever holds the token.

  Money model: buying debits the buyer immediately and holds the funds; the
  seller is paid only when the buyer confirms receipt. So `available` is already
  net of anything held - never subtract `held` from it.
*/

import { authedRequest } from './client'
import type {
  PayFastRedirect,
  Transaction,
  TransferResult,
  WalletSummary,
  WalletTransaction,
} from './types'

export const walletApi = {
  summary: () => authedRequest<WalletSummary>('/api/wallet'),

  /** Every movement, newest first - the audit trail behind the balance. */
  ledger: () => authedRequest<WalletTransaction[]>('/api/wallet/ledger'),

  /**
   * Starts a PayFast top-up.
   *
   * The amount is sent as a STRING even though it comes back as a number.
   * Jackson parses a JSON string straight into BigDecimal, and doing it this way
   * means "100.00" reaches the backend exactly as typed rather than going
   * through a float on the way.
   *
   * Nothing is credited by this call, or by the student returning to the site.
   * The wallet only moves when PayFast's server-to-server notification reaches
   * the backend, so after submitting the form, poll `summary()` rather than
   * assuming success.
   */
  startTopUp: (amount: string) =>
    authedRequest<PayFastRedirect>('/api/wallet/topup', { method: 'POST', body: { amount } }),

  /**
   * Completes your own pending top-up without going to PayFast.
   *
   * Local development only - the endpoint does not exist unless
   * app.payfast.simulator.enabled is true, and never outside development. Call it
   * only when startTopUp reported simulatorEnabled.
   */
  /**
   * Sends money straight to another student's wallet. The amount goes as a
   * string for the same reason as startTopUp. Errors carry a `code`:
   * INSUFFICIENT_FUNDS, RECIPIENT_NOT_FOUND, RECIPIENT_UNAVAILABLE, CANNOT_SEND_TO_SELF.
   */
  sendMoney: (recipientEmail: string, amount: string) =>
    authedRequest<TransferResult>('/api/wallet/transfer', {
      method: 'POST',
      body: { recipientEmail, amount },
    }),

  simulateTopUpCompletion: (merchantPaymentId: string) =>
    authedRequest<{ status: string }>(`/api/dev/payfast/complete/${merchantPaymentId}`, {
      method: 'POST',
    }),
}

export const purchasesApi = {
  /** Everything the signed-in student has bought or sold, newest first. */
  history: () => authedRequest<Transaction[]>('/api/purchases'),

  /**
   * Buys a listing with wallet money.
   *
   * `expectedAmount` is the price the buyer was shown; the backend rejects the
   * purchase with PRICE_CHANGED if the seller has edited it since.
   */
  buy: (listingId: number, expectedAmount: number) =>
    authedRequest<Transaction>('/api/purchases', {
      method: 'POST',
      body: { listingId, expectedAmount },
    }),

  /** Buyer confirms the item arrived. This is what actually pays the seller. */
  confirm: (transactionId: number) =>
    authedRequest<Transaction>(`/api/purchases/${transactionId}/confirm`, { method: 'POST' }),

  /** Either party calls off an open purchase; the buyer is refunded in full. */
  cancel: (transactionId: number) =>
    authedRequest<Transaction>(`/api/purchases/${transactionId}/cancel`, { method: 'POST' }),
}
