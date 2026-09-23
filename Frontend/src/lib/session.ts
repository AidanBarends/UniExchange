/*
  Where the signed-in session and the trusted-device token live in the browser.

  This is a tiny module on purpose. Both auth/authContext.ts and lib/api/client.ts
  need the storage key: if the API client imported it from authContext, and
  authContext imported its types from lib/api, that would be an import cycle.
  Keeping the keys and the stored shape here breaks the cycle cleanly.

  Web storage rather than a cookie because the backend reads the Authorization
  header only (SessionCreationPolicy.STATELESS, CSRF disabled), and a cookie set
  by localhost:8080 would be cross-site for localhost:5173.

  WHICH STORE, AND WHY IT MATTERS
  -------------------------------
  This is what actually implements "Remember me":

    remembered  -> localStorage    survives closing the browser and restarting.
    not         -> sessionStorage  dies with the browser. Next sign-in gets an OTP.

  sessionStorage being wiped when the browser closes IS the "the session was
  lost" trigger - we do not have to detect anything. Both the session and the
  device token follow the same flag, so they can never disagree about whether
  this browser is supposed to be remembered.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

export const SESSION_STORAGE_KEY = 'uniexchange.session'

/*
  Proof this browser already completed an OTP, so the next sign-in can skip it.
  Opaque and issued by the backend; the browser only ever stores and replays it.
  It is NOT a way in on its own - the password is still required every time.
*/
export const DEVICE_STORAGE_KEY = 'uniexchange.device'

export type StoredSession = {
  token: string
  /** Epoch milliseconds. A remembered token lasts weeks, otherwise an hour. */
  expiresAt: number
  email: string
  roles: string[]
  userId: number
  /** Which store this came from, so a refresh writes it back to the same one. */
  remembered: boolean
}

/*
  Every access is wrapped: a private window, blocked site data or a browser with
  storage disabled all throw on the *property access* itself, not just on use.
*/
function store(remember: boolean): Storage | null {
  try {
    return remember ? window.localStorage : window.sessionStorage
  } catch {
    return null
  }
}

function readKey(key: string): { value: string; remembered: boolean } | null {
  for (const remembered of [true, false]) {
    try {
      const value = store(remembered)?.getItem(key)
      if (value) return { value, remembered }
    } catch {
      // Try the other store rather than giving up.
    }
  }
  return null
}

/**
 * Writes to the chosen store and clears the other one, so toggling the checkbox
 * can never leave a stale copy behind that outlives what the student asked for.
 */
function writeKey(key: string, value: string, remember: boolean): void {
  try {
    store(remember)?.setItem(key, value)
  } catch {
    // Still usable for this tab even if persistence fails.
  }
  try {
    store(!remember)?.removeItem(key)
  } catch {
    // Nothing to clean up if the other store is unavailable.
  }
}

function clearKey(key: string): void {
  for (const remembered of [true, false]) {
    try {
      store(remembered)?.removeItem(key)
    } catch {
      // In-memory state is what actually gates the UI.
    }
  }
}

/** The stored session, or null when absent, malformed or expired. */
export function readStoredSession(): StoredSession | null {
  const found = readKey(SESSION_STORAGE_KEY)
  if (!found) return null

  try {
    const parsed = JSON.parse(found.value) as StoredSession
    if (!parsed.token || typeof parsed.expiresAt !== 'number') return null

    // Treat an expired token as no session at all - there is no refresh endpoint.
    if (parsed.expiresAt <= Date.now()) {
      clearStoredSession()
      return null
    }

    // Trust where it was actually found over what was written into it.
    return { ...parsed, remembered: found.remembered }
  } catch {
    return null
  }
}

export function writeStoredSession(session: StoredSession, remember: boolean): void {
  writeKey(SESSION_STORAGE_KEY, JSON.stringify({ ...session, remembered: remember }), remember)
}

export function clearStoredSession(): void {
  clearKey(SESSION_STORAGE_KEY)
}

/** The bearer token for the current session, or null. Used by authedRequest(). */
export function currentToken(): string | null {
  return readStoredSession()?.token ?? null
}

/**
 * The trusted-device token to send with a sign-in, or null when this browser has
 * never completed an OTP (or has since lost the proof that it did).
 */
export function readDeviceToken(): string | null {
  return readKey(DEVICE_STORAGE_KEY)?.value ?? null
}

export function writeDeviceToken(token: string, remember: boolean): void {
  writeKey(DEVICE_STORAGE_KEY, token, remember)
}

/*
  Deliberately NOT called on sign-out. Remembering a device means "don't ask me
  for a code on this computer again" - signing out and back in should still want
  the password, but not a second code. An unticked sign-in put the token in
  sessionStorage, so closing the browser clears it anyway and the OTP returns.

  This exists for a future "forget this device" control.
*/
export function clearDeviceToken(): void {
  clearKey(DEVICE_STORAGE_KEY)
}
