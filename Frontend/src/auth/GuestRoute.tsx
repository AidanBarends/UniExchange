/*
  The mirror of ProtectedRoute: gate for the auth screens, which only make sense
  when nobody is signed in.

  Without this, the browser BACK button walks a signed-in student straight back
  onto the login form. React Router restores /login from history and LoginPage
  renders quite happily - the session is still perfectly valid, but the screen
  says otherwise, so it reads as having been silently signed out. Typing /login
  in the address bar does the same thing.

  Sign-out is the only thing that should ever show these screens again, and it
  clears the session, so this guard stops letting you through at exactly the
  right moment.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from './useAuth'

export function GuestRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (isAuthenticated) {
    /*
      Honour the destination the student was originally heading for - both
      ProtectedRoute and the login -> /verify hand-off put it in `from`. It also
      keeps this guard out of the way on the last step of the OTP flow: verifying
      signs you in while still on /verify, and without `from` this redirect could
      beat VerifyOtpPage's own navigate and land everyone on /feed regardless of
      where they were going.
    */
    const from = (location.state as { from?: string } | null)?.from
    return <Navigate to={from ?? '/feed'} replace />
  }

  return <Outlet />
}
