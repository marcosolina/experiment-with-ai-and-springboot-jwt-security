/**
 * Backend API endpoint paths used by the Axios HTTP client.
 * Auth endpoints target the supervisor service; messages endpoints target the resource server.
 */
export const API_ENDPOINTS = {
  /** Authenticates a user and returns access + refresh tokens. */
  AUTH_LOGIN: '/api/auth/login',
  /** Registers a new user account. */
  AUTH_REGISTER: '/api/auth/register',
  /** Exchanges a refresh token for a new access + refresh token pair. */
  AUTH_REFRESH: '/api/auth/refresh',
  /** Invalidates the current refresh token (server-side logout). */
  AUTH_LOGOUT: '/api/auth/logout',
  /** Retrieves the authenticated user's profile. */
  USERS_ME: '/api/users/me',
  /** CRUD endpoint for messages on the resource server. */
  MESSAGES: '/api/messages',
  /** Health check endpoint indicating supervisor connectivity status. */
  MESSAGES_HEALTH: '/api/messages/health',
} as const
