/*
  Authentication. OWNER: Mogamat Yaseen Kannemeyer 240453182 - this one is done.

  Only /verify-otp issues a token: registration deliberately returns 202 with no
  token so an unverifiable address can never obtain credentials, and it is also
  the only place a device can earn the right to skip the code next time.

  Signing in therefore has TWO outcomes - see login() below.

  Every body here is built field by field rather than by spreading a form object.
  The login form now carries a rememberMe field that must be sent and a
  confirmPassword-style field that must not; spreading makes that distinction
  invisible, and TypeScript will not catch it because a variable is not subject
  to excess-property checking.
*/

import { request } from './client'
import type { AuthResponse, Campus, RegistrationResponse, User } from './types'

/**
 * What /api/auth/login returns. A `token` means the device was trusted and the
 * student is signed in; no `token` means a code has just been emailed and the
 * flow continues at /verify-otp.
 *
 * Narrow it with `'token' in result`.
 */
export type LoginResult = AuthResponse | RegistrationResponse

export const authApi = {
  /** 202 + no token. The account stays PENDING_VERIFICATION until the code is used. */
  register: (body: {
    email: string
    firstName: string
    lastName: string
    password: string
    campusId?: number | null
  }) =>
    request<RegistrationResponse>('/api/auth/register', {
      method: 'POST',
      body: {
        email: body.email,
        firstName: body.firstName,
        lastName: body.lastName,
        password: body.password,
        campusId: body.campusId ?? null,
      },
    }),

  /**
   * The only endpoint that issues a JWT, and the only one that trusts a device.
   * `rememberMe` decides both how long the session lasts and how long this
   * browser may skip the code.
   */
  verifyOtp: (body: { email: string; code: string; rememberMe: boolean }) =>
    request<AuthResponse>('/api/auth/verify-otp', {
      method: 'POST',
      body: { email: body.email, code: body.code, rememberMe: body.rememberMe },
    }),

  resendOtp: (body: { email: string }) =>
    request<RegistrationResponse>('/api/auth/resend-otp', {
      method: 'POST',
      body: { email: body.email },
    }),

  /**
   * Sign in. The password is always required; the emailed code is required on
   * top of it unless `deviceToken` proves this browser has been trusted before.
   *
   *   const result = await authApi.login({ ... })
   *   if ('token' in result) signIn(result, rememberMe)   // trusted device
   *   else navigate('/verify', ...)                       // a code was sent
   *
   * Still throws 403 EMAIL_NOT_VERIFIED when the account never used its first code.
   */
  login: (body: {
    email: string
    password: string
    deviceToken: string | null
    rememberMe: boolean
  }) =>
    request<LoginResult>('/api/auth/login', {
      method: 'POST',
      body: {
        email: body.email,
        password: body.password,
        deviceToken: body.deviceToken,
        rememberMe: body.rememberMe,
      },
    }),

  /*
    Takes the token explicitly rather than using authedRequest, because
    AuthProvider calls this while deciding whether the stored session is still
    good - it must not read the session it is in the middle of validating.
  */
  me: (token: string, onUnauthorized?: () => void) =>
    request<User>('/api/auth/me', { token, onUnauthorized }),

  /** Public - used to populate the campus picker on the signup form. */
  campuses: () => request<Campus[]>('/api/campuses'),
}
