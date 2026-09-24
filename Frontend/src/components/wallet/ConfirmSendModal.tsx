/*
  The send-money popup, in two steps:

    1. Confirm - transfers are instant and cannot be reversed, so the amount and
       recipient are shown once more before anything moves.
    2. Sent    - once the backend has moved the money, the same popup says so.

  Styled after NotificationDetailModal so the app has one modal look.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useEffect, useRef } from 'react'

import { Button } from '@/components/ui/Button'

import { formatZar } from './money'

type ConfirmSendModalProps = {
  recipientEmail: string
  amount: string
  sending: boolean
  /** Set once the transfer succeeded; switches the popup to its "sent" view. */
  sentTo: string | null
  onConfirm: () => void
  onClose: () => void
}

export function ConfirmSendModal({
  recipientEmail,
  amount,
  sending,
  sentTo,
  onConfirm,
  onClose,
}: ConfirmSendModalProps) {
  const panelRef = useRef<HTMLDivElement>(null)

  // Escape and backdrop clicks close - but not mid-send, so the outcome isn't missed.
  useEffect(() => {
    panelRef.current?.focus()
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !sending) onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [onClose, sending])

  return (
    <div
      className="fixed inset-0 z-20 flex items-center justify-center bg-ink-900/40 p-4"
      onClick={() => !sending && onClose()}
    >
      <div
        ref={panelRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="send-money-title"
        onClick={(event) => event.stopPropagation()}
        className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-5 shadow-lg focus:outline-none"
      >
        {sentTo ? (
          <>
            <div className="mx-auto grid size-12 place-items-center rounded-full bg-green-50 text-green-600">
              <svg
                aria-hidden="true"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                className="size-6"
              >
                <path d="m5 12 5 5 9-10" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </div>
            <h2 id="send-money-title" className="mt-3 text-center text-lg font-semibold text-ink-900">
              Money sent
            </h2>
            <p className="mt-1 text-center text-sm text-ink-500">
              {formatZar(amount)} is now in {sentTo}&apos;s wallet.
            </p>
            <div className="mt-6 flex justify-center">
              <Button onClick={onClose} className="w-auto px-6">
                Done
              </Button>
            </div>
          </>
        ) : (
          <>
            <h2 id="send-money-title" className="text-lg font-semibold text-ink-900">
              Send {formatZar(amount)}?
            </h2>

            <dl className="mt-4 space-y-2 rounded-xl bg-gray-50 p-4 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-ink-500">To</dt>
                <dd className="break-all text-right font-medium text-ink-900">{recipientEmail}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-ink-500">Amount</dt>
                <dd className="font-medium text-ink-900">{formatZar(amount)}</dd>
              </div>
            </dl>

            <p className="mt-3 text-xs text-ink-500">
              The money arrives in their wallet immediately and can&apos;t be taken back.
            </p>

            <div className="mt-6 flex items-center justify-end gap-2">
              <Button variant="ghost" onClick={onClose} disabled={sending} className="w-auto px-3">
                Cancel
              </Button>
              <Button onClick={onConfirm} loading={sending} className="w-auto px-3">
                Send {formatZar(amount)}
              </Button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
