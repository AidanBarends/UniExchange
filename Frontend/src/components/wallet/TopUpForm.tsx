/*
  Starts a PayFast top-up.

  The response is rendered as a hidden, self-submitting form rather than turned
  into a redirect URL. PayFast's signature covers the field values, so they have
  to reach PayFast exactly as the backend produced them - rebuilding the URL
  client-side risks re-encoding a character and having the payment rejected.

  Nothing here credits the wallet. Only PayFast's server-to-server notification
  does that, which is why the caller polls the balance afterwards rather than
  assuming the return trip means success.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useRef, useState } from 'react'

import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { TextField } from '@/components/ui/TextField'
import { walletApi } from '@/lib/api/wallet'
import { ApiError } from '@/lib/api/client'
import type { PayFastRedirect, WalletSummary } from '@/lib/api/types'

import { isValidAmount } from './money'

const QUICK_AMOUNTS = ['50.00', '100.00', '250.00', '500.00']

type TopUpFormProps = {
  /** What a top-up will actually do here. Drives the notice and the button label. */
  mode: WalletSummary['topUpMode']
  onSimulated?: () => void
}

export function TopUpForm({ mode, onSimulated }: TopUpFormProps) {
  const [amount, setAmount] = useState('100.00')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const formRef = useRef<HTMLFormElement | null>(null)
  const [redirect, setRedirect] = useState<PayFastRedirect | null>(null)

  async function startTopUp(event: React.FormEvent) {
    event.preventDefault()

    if (!isValidAmount(amount)) {
      setError('Enter an amount like 100.00.')
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      const response = await walletApi.startTopUp(amount)

      /*
       Local development: complete it here instead of redirecting.

       PayFast confirms a payment by calling the backend from their own servers,
       and that can never reach localhost - so sending the student to the sandbox
       would take the money and the callback would never arrive. The backend tells
       us when it is running with the simulator on, and this uses the same
       crediting path the real callback does.
      */
      if (response.simulatorEnabled) {
        await walletApi.simulateTopUpCompletion(response.merchantPaymentId)
        setSubmitting(false)
        onSimulated?.()
        return
      }

      setRedirect(response)
      // Wait a tick so React has rendered the hidden form before submitting it.
      window.setTimeout(() => formRef.current?.submit(), 0)
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Could not start the top-up.')
      setSubmitting(false)
    }
  }

  return (
    <div className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
      <h2 className="text-base font-semibold text-ink-900">Top up</h2>
      <p className="mt-1 text-sm text-ink-500">
        {mode === 'SIMULATED'
          ? 'Your balance is credited straight away.'
          : 'You will be taken to PayFast to pay. Your balance updates once PayFast confirms it.'}
      </p>

      {/*
        The notice says what will actually happen in THIS environment. An earlier
        version always claimed sandbox and told the student to "use the sandbox
        test details", which was unhelpful two ways: with the simulator on they
        never reach PayFast at all, and the copy named no details and linked
        nowhere, so there was nothing to act on.
      */}
      {mode === 'SIMULATED' && (
        <div className="mt-3 rounded-xl bg-amber-50 p-3 text-xs text-amber-800">
          <strong className="font-semibold">Local demo mode.</strong> PayFast is not contacted at
          all — the top-up is completed in the app, because PayFast&apos;s confirmation cannot
          reach a machine running on localhost. No money moves, and nothing is charged.
        </div>
      )}

      {mode === 'SANDBOX' && (
        <div className="mt-3 rounded-xl bg-amber-50 p-3 text-xs text-amber-800">
          <strong className="font-semibold">Sandbox mode.</strong> This opens PayFast&apos;s test
          environment, so nothing is charged. You will need PayFast&apos;s sandbox test buyer
          details to finish on their screen — see{' '}
          <a
            href="https://developers.payfast.co.za/docs#sandbox"
            target="_blank"
            rel="noreferrer"
            className="font-medium underline"
          >
            developers.payfast.co.za
          </a>
          .
        </div>
      )}

      <form onSubmit={startTopUp} className="mt-4 space-y-3">
        {error && <Alert tone="error">{error}</Alert>}

        <div className="flex flex-wrap gap-2">
          {QUICK_AMOUNTS.map((preset) => (
            <button
              key={preset}
              type="button"
              onClick={() => setAmount(preset)}
              className={`rounded-lg px-3 py-1.5 text-sm ring-1 transition ${
                amount === preset
                  ? 'bg-brand-600 text-white ring-brand-600'
                  : 'bg-white text-ink-700 ring-gray-300 hover:bg-gray-50'
              }`}
            >
              R{preset}
            </button>
          ))}
        </div>

        <TextField
          name="amount"
          label="Amount (ZAR)"
          inputMode="decimal"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
        />

        <Button type="submit" loading={submitting}>
          {mode === 'SIMULATED' ? 'Add funds' : 'Continue to PayFast'}
        </Button>
      </form>

      {/*
        Rendered only after the backend replies, and submitted immediately. The
        field order matters to the signature, which is why it is preserved from
        the server response rather than rebuilt.
      */}
      {redirect && (
        <form ref={formRef} action={redirect.processUrl} method="POST" className="hidden">
          {Object.entries(redirect.fields).map(([name, value]) => (
            <input key={name} type="hidden" name={name} value={value} readOnly />
          ))}
        </form>
      )}
    </div>
  )
}
