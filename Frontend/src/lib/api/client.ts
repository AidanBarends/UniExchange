/*
  The one place that knows how to speak HTTP to the Spring Boot API.

  Feature modules in this folder (listings.ts, messages.ts, ...) build on
  request() and authedRequest(). Pages must NOT call fetch directly - keeping
  every call behind a typed function is what makes the backend contract
  greppable in one folder.

  Two constraints from the backend's SecurityConfig worth remembering:
   - CORS allows ONLY the Authorization, Content-Type and Range request
     headers. Adding any other custom header (X-Requested-With and friends)
     makes the preflight fail with no useful error in the console.
   - Auth is a stateless bearer token. The backend reads no cookies, so
     credentials are never sent.

  One consequence of that second point shows up in chat: an <img>, <audio> or
  <video> tag cannot carry an Authorization header, so private attachments are
  not fetched through this client at all. The backend hands back a pre-signed,
  short-lived URL and the element loads it directly. Use those URLs exactly as
  given - do not prefix BASE_URL or re-sign them.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { currentToken } from '@/lib/session'

export const BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
).replace(/\/+$/, '')

/** Shape of the backend's GlobalExceptionHandler envelope. */
type ErrorEnvelope = {
  timestamp?: string
  status?: number
  error?: string
  message?: string
  /** Per-field messages from bean validation, e.g. { email: "..." }. */
  fields?: Record<string, string>
  /** Machine-readable discriminator, e.g. "EMAIL_NOT_VERIFIED". */
  code?: string
}

export class ApiError extends Error {
  readonly status: number
  readonly fields: Record<string, string>
  readonly code?: string

  constructor(status: number, message: string, fields: Record<string, string> = {}, code?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fields = fields
    this.code = code
  }

  /** True when the account exists but has not verified its email yet. */
  get isUnverified(): boolean {
    return this.code === 'EMAIL_NOT_VERIFIED'
  }
}

export type RequestOptions = {
  method?: string
  body?: unknown
  token?: string | null
  /** Extra query string values. Undefined and null entries are dropped. */
  query?: Record<string, string | number | boolean | null | undefined>
  /**
   * Called when the server rejects the token. Lets AuthProvider clear the
   * session without this module importing React or the router.
   */
  onUnauthorized?: () => void
}

/**
 * An unauthenticated call. Use this only for endpoints the backend marks
 * permitAll (auth endpoints, and GET on listings/categories/campuses/
 * bulletin-posts). Everything else needs authedRequest.
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, token, query, onUnauthorized } = options

  const headers: Record<string, string> = {}
  if (body !== undefined && !(body instanceof FormData)) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let response: Response
  try {
    response = await fetch(`${BASE_URL}${path}${buildQuery(query)}`, {
      method,
      headers,
      body: body === undefined || body instanceof FormData ? body : JSON.stringify(body),
    })
  } catch {
    // fetch only rejects on a network-level failure, so this is genuinely
    // "the API is unreachable" rather than an HTTP error status.
    throw new ApiError(0, 'Cannot reach the UniExchange server. Is the backend running?')
  }

  if (response.status === 401) {
    onUnauthorized?.()
  }

  if (response.status === 204) {
    return undefined as T
  }

  const raw = await response.text()
  const payload: unknown = raw ? safeJson(raw) : null

  if (!response.ok) {
    const envelope = (payload ?? {}) as ErrorEnvelope
    throw new ApiError(
      response.status,
      envelope.message ?? fallbackMessage(response.status),
      envelope.fields ?? {},
      envelope.code,
    )
  }

  return payload as T
}

/**
 * The one to use for almost everything. Attaches the bearer token from the
 * stored session automatically, so feature modules stay one-liners and nobody
 * has to remember to thread `session.token` through from a component.
 *
 *   export const listingsApi = {
 *     list: () => authedRequest<Listing[]>('/api/listings'),
 *   }
 */
export function authedRequest<T>(
  path: string,
  options: Omit<RequestOptions, 'token'> = {},
): Promise<T> {
  return request<T>(path, { ...options, token: currentToken() })
}

/**
 * Uploads a file as multipart/form-data with the current session's bearer
 * token. Deliberately separate from authedRequest: that one always JSON-
 * encodes the body, but a browser must set its own multipart boundary in
 * Content-Type, which it only does when it builds that header itself - so
 * this sends FormData and sets no Content-Type at all.
 */
export async function authedUpload<T>(path: string, file: File): Promise<T> {
  const formData = new FormData()
  formData.append('file', file)

  const token = currentToken()
  const headers: Record<string, string> = {}
  if (token) headers.Authorization = `Bearer ${token}`

  let response: Response
  try {
    response = await fetch(`${BASE_URL}${path}`, { method: 'POST', headers, body: formData })
  } catch {
    throw new ApiError(0, 'Cannot reach the UniExchange server. Is the backend running?')
  }

  const raw = await response.text()
  const payload: unknown = raw ? safeJson(raw) : null

  if (!response.ok) {
    const envelope = (payload ?? {}) as ErrorEnvelope
    throw new ApiError(
      response.status,
      envelope.message ?? fallbackMessage(response.status),
      envelope.fields ?? {},
      envelope.code,
    )
  }

  return payload as T
}

/**
 * Uploads a file with extra form fields and real progress reporting.
 *
 * XMLHttpRequest rather than fetch, and not by preference: fetch has no upload
 * progress events at all. A 25MB video on campus wifi with no progress bar looks
 * identical to a frozen page, so the student cancels and retries forever.
 *
 * Query values (rather than form fields) for the extras, because the backend
 * reads them with @RequestParam alongside the @RequestPart file.
 */
export function authedUploadWithProgress<T>(
  path: string,
  file: File,
  options: {
    query?: RequestOptions['query']
    onProgress?: (percent: number) => void
    signal?: AbortSignal
  } = {},
): Promise<T> {
  const { query, onProgress, signal } = options

  return new Promise<T>((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', file)

    const xhr = new XMLHttpRequest()
    xhr.open('POST', `${BASE_URL}${path}${buildQuery(query)}`)

    const token = currentToken()
    if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`)

    if (onProgress) {
      xhr.upload.addEventListener('progress', (event) => {
        // lengthComputable is false for a chunked body; reporting 0 there is
        // better than reporting a wrong number.
        if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100))
      })
    }

    xhr.addEventListener('load', () => {
      const payload: unknown = xhr.responseText ? safeJson(xhr.responseText) : null

      if (xhr.status >= 200 && xhr.status < 300) {
        resolve(payload as T)
        return
      }

      const envelope = (payload ?? {}) as ErrorEnvelope
      reject(
        new ApiError(
          xhr.status,
          envelope.message ?? fallbackMessage(xhr.status),
          envelope.fields ?? {},
          envelope.code,
        ),
      )
    })

    xhr.addEventListener('error', () =>
      reject(new ApiError(0, 'Cannot reach the UniExchange server. Is the backend running?')),
    )
    xhr.addEventListener('abort', () => reject(new ApiError(0, 'Upload cancelled.')))

    signal?.addEventListener('abort', () => xhr.abort())

    xhr.send(formData)
  })
}

function buildQuery(query: RequestOptions['query']): string {
  if (!query) return ''

  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value))
    }
  }

  const encoded = params.toString()
  return encoded ? `?${encoded}` : ''
}

function safeJson(raw: string): unknown {
  try {
    return JSON.parse(raw)
  } catch {
    return { message: raw }
  }
}

function fallbackMessage(status: number): string {
  // The backend's 401 entry point returns an empty body, so supply the text.
  if (status === 401) return 'Your session has expired. Please sign in again.'
  if (status === 403) return 'You are not allowed to do that.'
  if (status === 404) return 'We could not find that.'
  return `Request failed (${status}).`
}
