import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import api, { setAccessToken } from '../api/axiosConfig'

interface User {
  id: string
  username: string
  roles: string[]
}

interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const fetchUserProfile = useCallback(async () => {
    try {
      const response = await api.get('/api/users/me')
      setUser(response.data)
    } catch {
      setUser(null)
      setAccessToken(null)
      localStorage.removeItem('refreshToken')
    }
  }, [])

  useEffect(() => {
    const initAuth = async () => {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const response = await api.post('/api/auth/refresh', { refreshToken })
          setAccessToken(response.data.accessToken)
          localStorage.setItem('refreshToken', response.data.refreshToken)
          await fetchUserProfile()
        } catch {
          setAccessToken(null)
          localStorage.removeItem('refreshToken')
        }
      }
      setIsLoading(false)
    }
    initAuth()
  }, [fetchUserProfile])

  const login = async (username: string, password: string) => {
    const response = await api.post('/api/auth/login', { username, password })
    const { accessToken, refreshToken } = response.data
    setAccessToken(accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    await fetchUserProfile()
  }

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    try {
      if (refreshToken) {
        await api.post('/api/auth/logout', { refreshToken })
      }
    } finally {
      setUser(null)
      setAccessToken(null)
      localStorage.removeItem('refreshToken')
    }
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
