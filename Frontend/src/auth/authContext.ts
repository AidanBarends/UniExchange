/*
  The context object and its types, kept in a component-free module so Vite's
  fast refresh keeps working (a file that mixes components with other exports
  loses HMR).

  The storage key and StoredSession shape now live in @/lib/session, because the
  API client needs them too and importing them from here would create a cycle.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { createContext } from 'react'

import type { AuthResponse, User } from '@/lib/api/types'
import type { StoredSession } from '@/lib/session'

export type AuthContextValue = {
  session: StoredSession | null
  user: User | null
  loadingUser: boolean
  isAuthenticated: boolean
  /**
   * Stores the session. `remember` is the "Remember me" checkbox and decides
   * which browser store everything lands in - localStorage when ticked,
   * sessionStorage when not - so closing the browser signs an unticked student
   * out and brings the OTP back. It also persists response.deviceToken when the
   * backend just issued one.
   */
  signIn: (response: AuthResponse, remember: boolean) => void
  signOut: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
