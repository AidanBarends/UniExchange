/*
  One message in a thread: your own on the right, theirs on the left.

  Attachment URLs come from the backend already signed and bound to you as the
  viewer, so they go straight into src with no Authorization header - a media
  element cannot send one. Do not prefix BASE_URL onto them.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useState } from 'react'

import type { ChatMessageView } from '@/lib/api/types'

import { VoiceNotePlayer } from './VoiceNotePlayer'

type MessageBubbleProps = {
  message: ChatMessageView
  mine: boolean
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function MessageBubble({ message, mine }: MessageBubbleProps) {
  const hasText = message.content.trim().length > 0

  return (
    <div className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[min(32rem,80%)] rounded-2xl px-3 py-2 ${
          mine ? 'bg-brand-600 text-white' : 'bg-white text-ink-900 ring-1 ring-gray-200'
        }`}
      >
        {message.media && <Attachment message={message} mine={mine} />}

        {hasText && (
          <p className={`whitespace-pre-wrap break-words text-sm ${message.media ? 'mt-2' : ''}`}>
            {message.content}
          </p>
        )}

        <span className={`mt-1 block text-right text-xs ${mine ? 'text-white/70' : 'text-ink-400'}`}>
          {formatTime(message.sentAt)}
        </span>
      </div>
    </div>
  )
}

function Attachment({ message, mine }: { message: ChatMessageView; mine: boolean }) {
  const media = message.media
  const [failed, setFailed] = useState(false)

  if (!media) return null

  if (failed) {
    return (
      <a
        href={media.url}
        download
        className={`text-sm underline ${mine ? 'text-white' : 'text-brand-700'}`}
      >
        Download attachment
      </a>
    )
  }

  if (media.mediaType === 'AUDIO') {
    return <VoiceNotePlayer media={media} mine={mine} />
  }

  if (media.mediaType === 'VIDEO') {
    return (
      // controls, and preload="metadata" so opening a thread does not pull down
      // every video in it. Seeking works because the backend answers Range
      // requests for this URL.
      <video
        src={media.url}
        controls
        preload="metadata"
        onError={() => setFailed(true)}
        className="max-h-80 w-full rounded-xl bg-black"
      />
    )
  }

  return (
    <a href={media.url} target="_blank" rel="noreferrer">
      <img
        src={media.url}
        alt={media.originalFilename ?? 'Shared image'}
        loading="lazy"
        onError={() => setFailed(true)}
        className="max-h-80 rounded-xl object-cover"
      />
    </a>
  )
}
