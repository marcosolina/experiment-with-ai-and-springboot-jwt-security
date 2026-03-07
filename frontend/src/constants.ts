/**
 * Application route paths used for client-side navigation with react-router-dom.
 */
export enum Routes {
  /** Login page route. */
  LOGIN = '/login',
  /** User profile dashboard route. */
  DASHBOARD = '/dashboard',
  /** Messages CRUD page route. */
  MESSAGES = '/messages',
}

/**
 * Keys used to persist values in browser localStorage.
 */
export const STORAGE_KEYS = {
  /** Key for storing the JWT refresh token. */
  REFRESH_TOKEN: 'refreshToken',
} as const
