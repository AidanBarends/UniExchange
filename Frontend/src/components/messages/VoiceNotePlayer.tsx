/*
  A voice note with its own transport controls.

  The native <audio controls> is deliberately NOT used. MediaRecorder writes a
  streaming container with no duration in it, so the browser reports
  `Infinity` and the built-in seek bar is dead - you can play, but not scrub or
  see how long the clip is. Since the recorder measured the real duration and
  stored it, we draw the bar ourselves from `durationMs` and drive currentTime
  directly, which restores scrubbing on every browser.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useEffect, useRef, useState } from 'react'

import type { ChatMediaView } from '@/lib/api/types'

type VoiceNotePlayerProps = {
  media: ChatMediaView
  mine: boolean
}

function formatClock(ms: number): string {
  const totalSeconds = Math.max(0, Math.round(ms / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

export function VoiceNotePlayer({ media, mine }: VoiceNotePlayerProps) {
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const [playing, setPlaying] = useState(false)
  const [positionMs, setPositionMs] = useState(0)
  const [unplayable, setUnplayable] = useState(false)

  const durationMs = media.durationMs ?? 0
  const progress = durationMs > 0 ? Math.min(100, (positionMs / durationMs) * 100) : 0

  useEffect(() => {
    const audio = audioRef.current
    if (!audio) return

    const onTime = () => setPositionMs(audio.currentTime * 1000)
    const onEnded = () => {
      setPlaying(false)
      setPositionMs(0)
    }
    // A note recorded in Chrome (WebM/Opus) may not play in Safari. Rather than
    // failing silently, fall back to a download link.
    const onError = () => setUnplayable(true)

    audio.addEventListener('timeupdate', onTime)
    audio.addEventListener('ended', onEnded)
    audio.addEventListener('error', onError)
    return () => {
      audio.removeEventListener('timeupdate', onTime)
      audio.removeEventListener('ended', onEnded)
      audio.removeEventListener('error', onError)
    }
  }, [])

  function toggle() {
    const audio = audioRef.current
    if (!audio) return
    if (playing) {
      audio.pause()
      setPlaying(false)
    } else {
      void audio.play().then(() => setPlaying(true)).catch(() => setUnplayable(true))
    }
  }

  function seek(event: React.ChangeEvent<HTMLInputElement>) {
    const audio = audioRef.current
    if (!audio || durationMs <= 0) return
    const nextMs = (Number(event.target.value) / 100) * durationMs
    audio.currentTime = nextMs / 1000
    setPositionMs(nextMs)
  }

  if (unplayable) {
    return (
      <a
        href={media.url}
        download
        className={`flex items-center gap-2 text-sm underline ${mine ? 'text-white' : 'text-brand-700'}`}
      >
        <DownloadIcon className="size-4" />
        Download voice note ({formatClock(durationMs)})
      </a>
    )
  }

  return (
    <div className="flex min-w-52 items-center gap-3">
      {/* preload="metadata" keeps the thread cheap to open - the bytes only
          arrive when someone actually presses play. */}
      <audio ref={audioRef} src={media.url} preload="metadata" />

      <button
        type="button"
        onClick={toggle}
        aria-label={playing ? 'Pause voice note' : 'Play voice note'}
        className={`grid size-9 shrink-0 place-items-center rounded-full transition ${
          mine ? 'bg-white/20 text-white hover:bg-white/30' : 'bg-brand-100 text-brand-800 hover:bg-brand-200'
        }`}
      >
        {playing ? <PauseIcon className="size-4" /> : <PlayIcon className="size-4" />}
      </button>

      <div className="flex-1">
        <input
          type="range"
          min={0}
          max={100}
          value={progress}
          onChange={seek}
          aria-label="Seek within voice note"
          className={`h-1 w-full cursor-pointer appearance-none rounded-full ${
            mine ? 'bg-white/30 accent-white' : 'bg-brand-100 accent-brand-600'
          }`}
        />
        <span className={`mt-1 block text-xs tabular-nums ${mine ? 'text-white/80' : 'text-ink-500'}`}>
          {formatClock(playing || positionMs > 0 ? positionMs : durationMs)}
        </span>
      </div>
    </div>
  )
}

function PlayIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 4l14 8-14 8V4z" fill="currentColor" stroke="none" />
    </svg>
  )
}

function PauseIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M7 4h3v16H7zM14 4h3v16h-3z" fill="currentColor" stroke="none" />
    </svg>
  )
}

function DownloadIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 3v12m0 0l-4-4m4 4l4-4M4 17v2a2 2 0 002 2h12a2 2 0 002-2v-2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
