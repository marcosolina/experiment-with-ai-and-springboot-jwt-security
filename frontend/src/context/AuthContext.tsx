import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import api, { setAccessToken } from '../api/axiosConfig'
import type { User, AuthContextType } from './interfaces'
import { API_ENDPOINTS } from '../api/constants'
import { STORAGE_KEYS } from '../constants'

const AuthContext = createContext<AuthContextType | undefined>(undefined)

/**
 * Provides authentication state and actions to the component tree via React Context.
 *
 * On mount, attempts to restore a previous session by refreshing the access token
 * using a refresh token stored in localStorage. Exposes `login` and `logout`
 * functions and tracks the current `user`, `isAuthenticated`, and `isLoading` state.
 *
 * @param props.children - Child components that will have access to the auth context.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const fetchUserProfile = useCallback(async () => {
    try {
      const response = await api.get(API_ENDPOINTS.USERS_ME)
      setUser(response.data)
    } catch {
      setUser(null)
      setAccessToken(null)
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
    }
  }, [])

  useEffect(() => {
    const initAuth = async () => {
      const refreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
      if (refreshToken) {
        try {
          const response = await api.post(API_ENDPOINTS.AUTH_REFRESH, { refreshToken })
          setAccessToken(response.data.accessToken)
          localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.data.refreshToken)
          await fetchUserProfile()
        } catch {
          setAccessToken(null)
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
        }
      }
      setIsLoading(false)
    }
    initAuth()
  }, [fetchUserProfile])

  const login = async (username: string, password: string) => {
    const response = await api.post(API_ENDPOINTS.AUTH_LOGIN, { username, password })
    const { accessToken, refreshToken } = response.data
    setAccessToken(accessToken)
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
    await fetchUserProfile()
  }

  const logout = async () => {
    const refreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
    try {
      if (refreshToken) {
        await api.post(API_ENDPOINTS.AUTH_LOGOUT, { refreshToken })
      }
    } finally {
      setUser(null)
      setAccessToken(null)
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
    }
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

/**
 * Custom hook that returns the current authentication context.
 *
 * Must be called from a component wrapped by {@link AuthProvider}.
 *
 * @returns The authentication context containing user state and auth actions.
 * @throws Error if called outside of an {@link AuthProvider}.
 */
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
