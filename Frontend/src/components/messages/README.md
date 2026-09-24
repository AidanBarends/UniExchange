# Messaging components

**Owner: Yaseen Kannemeyer (240453182)**

Components used only by messages (/messages, /messages/:conversationId) live here, so nobody else's work touches them.

Anything genuinely reusable across pages belongs in `../ui/` instead — and if
you move something there, say so in the group chat so the others can use it.

## What is here

| File | |
|---|---|
| `MessageBubble.tsx` | One message, with its image / video / voice-note attachment |
| `MessageComposer.tsx` | Text, attach, and hold-to-record. Uploads before sending |
| `VoiceNotePlayer.tsx` | Custom transport for voice notes |
| `useVoiceRecorder.ts` | `MediaRecorder` wrapper, with the browser differences handled |

## Two browser facts that shaped these

**There is no single audio format.** Chrome, Edge and Firefox record WebM/Opus;
Safari only encodes MP4/AAC and reports `isTypeSupported('audio/webm')` as false.
`useVoiceRecorder` negotiates a type and then reads `recorder.mimeType` back —
never trust the type you asked for. At playback, `VoiceNotePlayer` feature-detects
with `canPlayType()` and falls back to a download link, because a note recorded in
Chrome may not play on an iPhone.

**MediaRecorder output has no duration.** It writes a streaming container, so
`audio.duration` is `Infinity` and the native seek bar does nothing — no amount of
correct HTTP range handling changes that. The recorder times the take itself and
sends `durationMs`, and `VoiceNotePlayer` draws its own progress bar from it.
That is also why the composer sends `durationMs` with the upload.

Voice notes need a secure context (`https` or `localhost`). A phone pointed at
`http://192.168.x.x:5173` sees `navigator.mediaDevices` as undefined, which is why
`supportsVoiceNotes()` checks `isSecureContext` up front and the button hides
rather than failing on tap.
