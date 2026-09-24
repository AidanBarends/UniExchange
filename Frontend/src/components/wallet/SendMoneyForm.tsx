/*
  Sends wallet money straight to another student, found by student email.

  Submitting only opens a confirmation popup; nothing moves until the student
  confirms there. The same popup then reports the result.

  The sender is never part of the request - the backend always uses whoever
  holds the token.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useState } from 'react'

import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { TextField } from '@/components/ui/TextField'
import { ApiError } from '@/lib/api/client'
import { walletApi } from '@/lib/api/wallet'

import { ConfirmSendModal } from './ConfirmSendModal'
import { isValidAmount } from './money'

const ERROR_MESSAGES: Record<string, string> = {
  INSUFFICIENT_FUNDS: "You don't have enough in your wallet for that.",
  RECIPIENT_NOT_FOUND: 'No student has that email. Check it and try again.',
  RECIPIENT_UNAVAILABLE: "That student's account can't receive money right now.",
  CANNOT_SEND_TO_SELF: "You can't send money to yourself.",
}

type PendingSend = { recipientEmail: string; amount: string }

type SendMoneyFormProps = {
  onSent?: () => void
}

export function SendMoneyForm({ onSent }: SendMoneyFormProps) {
  const [recipientEmail, setRecipientEmail] = useState('')
  const [amount, setAmount] = useState('')
  const [error, setError] = useState<string | null>(null)

  // Frozen at review time so edits behind the popup can't change what is sent.
  const [pending, setPending] = useState<PendingSend | null>(null)
  const [sending, setSending] = useState(false)
  const [sentTo, setSentTo] = useState<string | null>(null)

  function review(event: React.FormEvent) {
    event.preventDefault()

    if (!recipientEmail.trim()) {
      setError("Enter the student's email.")
      return
    }
    if (!isValidAmount(amount)) {
      setError('Enter an amount like 50.00.')
      return
    }

    setError(null)
    setSentTo(null)
    setPending({ recipientEmail: recipientEmail.trim(), amount })
  }

  async function send() {
    if (!pending) return

    setSending(true)
    try {
      const result = await walletApi.sendMoney(pending.recipientEmail, pending.amount)
      setSentTo(result.recipientName)
      setRecipientEmail('')
      setAmount('')
      onSent?.()
    } catch (err: unknown) {
      // Close the popup so the error shows on the form, next to what needs fixing.
      setPending(null)
      if (err instanceof ApiError) {
        setError((err.code && ERROR_MESSAGES[err.code]) || err.message)
      } else {
        setError('Could not send the money.')
      }
    } finally {
      setSending(false)
    }
  }

  function closePopup() {
    setPending(null)
    setSentTo(null)
  }

  return (
    <div className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
      <h2 className="text-base font-semibold text-ink-900">Send money</h2>
      <p className="mt-1 text-sm text-ink-500">
        Moves money from your wallet straight into another student&apos;s. It arrives instantly.
      </p>

      <form onSubmit={review} className="mt-4 space-y-3">
        {error && <Alert tone="error">{error}</Alert>}

        <TextField
          name="recipientEmail"
          label="Student email"
          type="email"
          autoComplete="off"
          placeholder="230000000@mycput.ac.za"
          value={recipientEmail}
          onChange={(event) => setRecipientEmail(event.target.value)}
        />

        <TextField
          name="amount"
          label="Amount (ZAR)"
          inputMode="decimal"
          placeholder="50.00"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
        />

        <Button type="submit">Send money</Button>
      </form>

      {pending && (
        <ConfirmSendModal
          recipientEmail={pending.recipientEmail}
          amount={pending.amount}
          sending={sending}
          sentTo={sentTo}
          onConfirm={() => void send()}
          onClose={closePopup}
        />
      )}
    </div>
  )
}
