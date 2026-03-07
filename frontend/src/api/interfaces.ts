/**
 * Represents a pending request waiting for a token refresh to complete.
 * Used by the Axios response interceptor to queue concurrent 401-failed requests
 * and replay them once a new access token is obtained.
 */
export interface QueuedRequest {
  /** Resolves the queued promise with the new access token. */
  resolve: (token: string) => void
  /** Rejects the queued promise when token refresh fails. */
  reject: (error: unknown) => void
}
