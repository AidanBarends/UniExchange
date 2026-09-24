/*
  Leaves a review for the other party to a completed transaction.

  Note what is NOT sent: the person being rated. The backend derives that from
  the transaction, so the only thing this form chooses is the sale, the score and
  the words. That is what keeps ratings - and the Trusted Seller badge built on
  them - worth anything.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useState } from 'react'

import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Textarea } from '@/components/ui/Textarea'
import { ApiError } from '@/lib/api/client'
import { reviewsApi } from '@/lib/api/reviews'

import { StarRating } from './StarRating'

type ReviewFormProps = {
  transactionId: number
  /** What the reviewer is being asked about, e.g. "Sipho" or "this purchase". */
  subject: string
  onDone: () => void
}

export function ReviewForm({ transactionId, subject, onDone }: ReviewFormProps) {
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await reviewsApi.submit({ transactionId, rating, comment: comment.trim() || undefined })
      onDone()
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Your review could not be saved.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit} className="space-y-3 rounded-xl bg-gray-50 p-4">
      {error && <Alert tone="error">{error}</Alert>}

      <div>
        <span className="mb-1 block text-sm font-medium text-ink-700">
          How was your experience with {subject}?
        </span>
        <StarRating value={rating} onChange={setRating} />
      </div>

      <Textarea
        name="comment"
        label="Comment (optional)"
        rows={3}
        value={comment}
        onChange={(event) => setComment(event.target.value)}
        placeholder="Was the item as described? Did they show up on time?"
      />

      <Button type="submit" loading={submitting} className="w-auto px-4">
        Submit review
      </Button>
    </form>
  )
}
