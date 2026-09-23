/*
  Session state.

  The backend is stateless with a one-hour token and no refresh endpoint, so the
  expiry is stored next to the token and checked on load - otherwise the app
  would boot "logged in" holding a token every request rejects.

  Reading and writing localStorage lives in @/lib/session so the API client can
  attach the token without importing anything from React.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

import { authApi } from '@/lib/api/auth'
import { ApiError } from '@/lib/api/client'
import type { AuthResponse, User } from '@/lib/api/types'
import {
  clearStoredSession,
  readStoredSession,
  writeDeviceToken,
  writeStoredSession,
} from '@/lib/session'
import type { StoredSession } from '@/lib/session'

import { AuthContext } from './authContext'
import type { AuthContextValue } from './authContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<StoredSession | null>(readStoredSession)
  const [user, setUser] = useState<User | null>(null)

  const signOut = useCallback(() => {
    clearStoredSession()
    setSession(null)
    setUser(null)
  }, [])

  const signIn = useCallback((response: AuthResponse, remember: boolean) => {
    const stored: StoredSession = {
      token: response.token,
      // The backend already sized expiresIn by rememberMe - weeks when ticked,
      // an hour when not - so this needs no branch of its own.
      expiresAt: Date.now() + response.expiresIn * 1000,
      email: response.email,
      roles: response.roles,
      userId: response.userId,
      remembered: remember,
    }
    writeStoredSession(stored, remember)

    // Only /verify-otp returns one. A trusted sign-in leaves it null and the
    // token already in storage stays exactly where it is.
    if (response.deviceToken) {
      writeDeviceToken(response.deviceToken, remember)
    }

    setSession(stored)
    setUser(null)
  }, [])

  /*
    Load the profile whenever we hold a session. State is only ever set from the
    async continuation, never synchronously in the effect body, so this does not
    cascade renders. Safe under StrictMode's double invocation via `cancelled`.
  */
  useEffect(() => {
    if (!session) return

    let cancelled = false

    authApi
      .me(session.token)
      .then((fetched) => {
        if (!cancelled) setUser(fetched)
      })
      .catch((error: unknown) => {
        // A rejected token means the session is dead; anything else is
        // transient and the UI falls back to the stored email.
        if (!cancelled && error instanceof ApiError && error.status === 401) {
          signOut()
        }
      })

    return () => {
      cancelled = true
    }
  }, [session, signOut])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      user,
      // Derived rather than stored: we have a session but no profile yet.
      loadingUser: session !== null && user === null,
      isAuthenticated: session !== null,
      signIn,
      signOut,
    }),
    [session, user, signIn, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
