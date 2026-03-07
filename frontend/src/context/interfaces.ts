/**
 * Represents an authenticated user as returned by the supervisor's `/api/users/me` endpoint.
 */
export interface User {
  /** Unique identifier of the user. */
  id: string
  /** Display name / login name. */
  username: string
  /** Roles assigned to the user (e.g., "ROLE_USER", "ROLE_ADMIN"). */
  roles: string[]
}

/**
 * Shape of the authentication context value provided by {@link AuthProvider}.
 */
export interface AuthContextType {
  /** The currently authenticated user, or `null` if not logged in. */
  user: User | null
  /** Whether a user is currently authenticated. */
  isAuthenticated: boolean
  /** Whether an initial session restoration attempt is in progress. */
  isLoading: boolean
  /**
   * Authenticates the user with the given credentials.
   * @param username - The user's login name.
   * @param password - The user's password.
   */
  login: (username: string, password: string) => Promise<void>
  /** Logs the user out and invalidates the refresh token on the server. */
  logout: () => Promise<void>
}
