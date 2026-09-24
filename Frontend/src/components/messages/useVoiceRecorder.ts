/*
  Records a voice note with MediaRecorder.

  Three browser realities this has to work around, none of them obvious:

  1. THERE IS NO SINGLE FORMAT. Chrome, Edge and Firefox record WebM/Opus.
     Safari (macOS and iOS) only encodes MP4/AAC and reports
     isTypeSupported('audio/webm') as false. So the type is negotiated, and we
     read `recorder.mimeType` back afterwards rather than trusting what we asked
     for - browsers may fall back, and blob.type is an empty string on some paths.

  2. THE DURATION IS NOT IN THE FILE. MediaRecorder writes a streaming container
     with no Duration element, so an <audio> tag loaded with the result reports
     `Infinity` and its seek bar does nothing. No amount of correct HTTP range
     handling fixes that - it is the container. We time the recording here and
     send the number alongside, and the UI draws its own progress bar from it.

  3. getUserMedia NEEDS A SECURE CONTEXT. https or localhost only. A teammate
     testing on http://192.168.x.x from their phone gets `navigator.mediaDevices`
     as undefined with no error, which is why `supportsVoiceNotes` checks
     isSecureContext explicitly rather than letting it fail at record time.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useCallback, useEffect, useRef, useState } from 'react'

/** Most-preferred first. The first supported entry wins. */
const CANDIDATES = [
  'audio/webm;codecs=opus',
  'audio/ogg;codecs=opus',
  'audio/mp4;codecs=mp4a.40.2',
  'audio/mp4',
  'audio/webm',
]

export type RecordedVoiceNote = {
  file: File
  durationMs: number
  /** Object URL for local playback before sending. Revoke it when done. */
  previewUrl: string
}

export function supportsVoiceNotes(): boolean {
  return (
    typeof MediaRecorder !== 'undefined' &&
    typeof navigator.mediaDevices?.getUserMedia === 'function' &&
    window.isSecureContext
  )
}

function pickMimeType(): string | undefined {
  if (typeof MediaRecorder === 'undefined') return undefined
  return CANDIDATES.find((type) => MediaRecorder.isTypeSupported(type))
}

function extensionFor(mimeType: string): string {
  if (mimeType.includes('mp4')) return 'm4a'
  if (mimeType.includes('ogg')) return 'ogg'
  return 'weba'
}

export function useVoiceRecorder() {
  const [recording, setRecording] = useState(false)
  const [elapsedMs, setElapsedMs] = useState(0)
  const [error, setError] = useState<string | null>(null)

  const recorderRef = useRef<MediaRecorder | null>(null)
  const chunksRef = useRef<Blob[]>([])
  const startedAtRef = useRef(0)
  const streamRef = useRef<MediaStream | null>(null)
  const tickRef = useRef<number | null>(null)

  /** Always release the microphone - the browser shows a recording indicator until we do. */
  const stopTracks = useCallback(() => {
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null
    if (tickRef.current !== null) {
      window.clearInterval(tickRef.current)
      tickRef.current = null
    }
  }, [])

  useEffect(() => stopTracks, [stopTracks])

  const start = useCallback(async () => {
    setError(null)

    if (!supportsVoiceNotes()) {
      setError(
        window.isSecureContext
          ? 'Voice notes are not supported in this browser.'
          : 'Voice notes need a secure connection (https or localhost).',
      )
      return
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      streamRef.current = stream

      const mimeType = pickMimeType()
      const recorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream)

      chunksRef.current = []
      recorder.addEventListener('dataavailable', (event) => {
        if (event.data.size > 0) chunksRef.current.push(event.data)
      })

      recorderRef.current = recorder
      startedAtRef.current = performance.now()

      // 250ms timeslice so an abrupt stop still yields the audio recorded so far.
      recorder.start(250)
      setRecording(true)
      setElapsedMs(0)

      tickRef.current = window.setInterval(
        () => setElapsedMs(performance.now() - startedAtRef.current),
        100,
      )
    } catch {
      // Almost always a denied microphone permission.
      setError('UniExchange needs microphone access to record a voice note.')
      stopTracks()
    }
  }, [stopTracks])

  const stop = useCallback((): Promise<RecordedVoiceNote | null> => {
    const recorder = recorderRef.current
    if (!recorder || recorder.state === 'inactive') {
      setRecording(false)
      return Promise.resolve(null)
    }

    return new Promise((resolve) => {
      recorder.addEventListener(
        'stop',
        () => {
          const durationMs = Math.round(performance.now() - startedAtRef.current)
          // The recorder's OWN type, not the one we requested - see note 1.
          const mimeType = recorder.mimeType || 'audio/webm'
          const blob = new Blob(chunksRef.current, { type: mimeType })

          stopTracks()
          setRecording(false)
          setElapsedMs(0)

          if (blob.size === 0) {
            resolve(null)
            return
          }

          const file = new File([blob], `voice-note.${extensionFor(mimeType)}`, { type: mimeType })
          resolve({ file, durationMs, previewUrl: URL.createObjectURL(blob) })
        },
        { once: true },
      )
      recorder.stop()
    })
  }, [stopTracks])

  const cancel = useCallback(() => {
    const recorder = recorderRef.current
    if (recorder && recorder.state !== 'inactive') recorder.stop()
    chunksRef.current = []
    stopTracks()
    setRecording(false)
    setElapsedMs(0)
  }, [stopTracks])

  return { recording, elapsedMs, error, start, stop, cancel }
}
