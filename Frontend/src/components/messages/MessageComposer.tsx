/*
  The chat composer: text, a photo/video attachment, or a voice note.

  Attachments upload BEFORE the message is sent, which is why there is a staged
  preview state here. That two-step is what makes progress, cancel and retry
  possible - and means a failed 25MB upload never loses the caption the student
  typed while waiting.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useEffect, useRef, useState } from 'react'

import { chatApi } from '@/lib/api/chat'
import { ApiError } from '@/lib/api/client'
import type { ChatMediaUploaded } from '@/lib/api/types'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'

import { supportsVoiceNotes, useVoiceRecorder } from './useVoiceRecorder'

/** Matches ChatMediaStorage's whitelist on the backend. */
const ACCEPTED = 'image/jpeg,image/png,image/gif,image/webp,video/mp4,video/webm'

const MAX_BYTES = 25 * 1024 * 1024

type StagedMedia = {
  uploaded: ChatMediaUploaded
  previewUrl: string
  kind: 'image' | 'video' | 'audio'
}

type MessageComposerProps = {
  conversationId: number
  onSent: () => void
}

export function MessageComposer({ conversationId, onSent }: MessageComposerProps) {
  const [text, setText] = useState('')
  const [staged, setStaged] = useState<StagedMedia | null>(null)
  const [uploadPercent, setUploadPercent] = useState<number | null>(null)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fileInputRef = useRef<HTMLInputElement | null>(null)
  const recorder = useVoiceRecorder()

  // Object URLs are a leak if they are not released.
  useEffect(() => {
    return () => {
      if (staged) URL.revokeObjectURL(staged.previewUrl)
    }
  }, [staged])

  async function upload(file: File, kind: StagedMedia['kind'], durationMs?: number) {
    if (file.size > MAX_BYTES) {
      setError('That file is larger than 25 MB.')
      return
    }

    setError(null)
    setUploadPercent(0)
    try {
      const uploaded = await chatApi.uploadMedia(conversationId, file, {
        durationMs,
        onProgress: setUploadPercent,
      })
      setStaged({ uploaded, previewUrl: URL.createObjectURL(file), kind })
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'That file could not be uploaded.')
    } finally {
      setUploadPercent(null)
    }
  }

  function onPickFile(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    // Reset so picking the same file twice in a row still fires a change event.
    event.target.value = ''
    if (!file) return
    void upload(file, file.type.startsWith('video/') ? 'video' : 'image')
  }

  async function finishRecording() {
    const note = await recorder.stop()
    if (!note) return
    URL.revokeObjectURL(note.previewUrl)
    void upload(note.file, 'audio', note.durationMs)
  }

  function discardStaged() {
    if (staged) URL.revokeObjectURL(staged.previewUrl)
    setStaged(null)
  }

  async function send(event: React.FormEvent) {
    event.preventDefault()
    const content = text.trim()
    if (!content && !staged) return

    setSending(true)
    setError(null)
    try {
      await chatApi.send(conversationId, { content, mediaId: staged?.uploaded.mediaId ?? null })
      setText('')
      discardStaged()
      onSent()
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Your message could not be sent.')
    } finally {
      setSending(false)
    }
  }

  const uploading = uploadPercent !== null

  return (
    <form onSubmit={send} className="border-t border-gray-200 bg-white p-3">
      {error && (
        <div className="mb-2">
          <Alert tone="error">{error}</Alert>
        </div>
      )}
      {recorder.error && (
        <div className="mb-2">
          <Alert tone="error">{recorder.error}</Alert>
        </div>
      )}

      {uploading && (
        <div className="mb-2" role="status" aria-live="polite">
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
            <div
              className="h-full bg-brand-600 transition-[width]"
              style={{ width: `${uploadPercent}%` }}
            />
          </div>
          <span className="mt-1 block text-xs text-ink-500">Uploading… {uploadPercent}%</span>
        </div>
      )}

      {staged && (
        <div className="mb-2 flex items-center gap-3 rounded-xl bg-gray-50 p-2">
          {staged.kind === 'image' && (
            <img src={staged.previewUrl} alt="" className="size-14 rounded-lg object-cover" />
          )}
          {staged.kind === 'video' && (
            <video src={staged.previewUrl} className="size-14 rounded-lg bg-black object-cover" />
          )}
          {staged.kind === 'audio' && (
            <span className="grid size-14 place-items-center rounded-lg bg-brand-100 text-brand-800">
              <MicIcon className="size-6" />
            </span>
          )}
          <span className="flex-1 text-sm text-ink-700">Ready to send</span>
          <button
            type="button"
            onClick={discardStaged}
            className="rounded-lg px-2 py-1 text-sm text-ink-500 hover:bg-gray-200"
          >
            Remove
          </button>
        </div>
      )}

      {recorder.recording ? (
        <div className="flex items-center gap-3">
          <span className="flex flex-1 items-center gap-2 text-sm text-ink-700">
            <span className="size-2.5 animate-pulse rounded-full bg-red-500" />
            Recording {Math.round(recorder.elapsedMs / 1000)}s
          </span>
          <Button type="button" variant="ghost" className="w-auto px-3" onClick={recorder.cancel}>
            Cancel
          </Button>
          <Button type="button" className="w-auto px-3" onClick={finishRecording}>
            Stop
          </Button>
        </div>
      ) : (
        <div className="flex items-end gap-2">
          <input
            ref={fileInputRef}
            type="file"
            accept={ACCEPTED}
            onChange={onPickFile}
            className="sr-only"
            id="chat-attachment"
          />
          <label
            htmlFor="chat-attachment"
            title="Attach a photo or video"
            className="grid size-10 shrink-0 cursor-pointer place-items-center rounded-lg text-ink-500 hover:bg-gray-100"
          >
            <PaperclipIcon className="size-5" />
            <span className="sr-only">Attach a photo or video</span>
          </label>

          {supportsVoiceNotes() && (
            <button
              type="button"
              onClick={recorder.start}
              title="Record a voice note"
              className="grid size-10 shrink-0 place-items-center rounded-lg text-ink-500 hover:bg-gray-100"
            >
              <MicIcon className="size-5" />
              <span className="sr-only">Record a voice note</span>
            </button>
          )}

          <textarea
            value={text}
            onChange={(event) => setText(event.target.value)}
            onKeyDown={(event) => {
              // Enter sends, Shift+Enter makes a new line - what everyone expects
              // from a chat box.
              if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault()
                void send(event)
              }
            }}
            rows={1}
            placeholder="Write a message…"
            aria-label="Message"
            className="max-h-32 min-h-10 flex-1 resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-brand-600"
          />

          <Button
            type="submit"
            className="w-auto px-4"
            loading={sending}
            disabled={uploading || (!text.trim() && !staged)}
          >
            Send
          </Button>
        </div>
      )}
    </form>
  )
}

function PaperclipIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path
        d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function MicIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 2a3 3 0 00-3 3v7a3 3 0 006 0V5a3 3 0 00-3-3z" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M19 10v2a7 7 0 01-14 0v-2M12 19v3" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
