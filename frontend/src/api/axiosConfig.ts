/**
 * @module axiosConfig
 *
 * Configures a shared Axios instance with JWT authentication interceptors.
 *
 * - **Request interceptor**: attaches the current access token as a Bearer
 *   Authorization header on every outgoing request.
 * - **Response interceptor**: intercepts 401 responses to transparently refresh
 *   the access token. Concurrent requests that fail while a refresh is in
 *   progress are queued and replayed once the new token is available. If the
 *   refresh itself fails, the user is redirected to the login page.
 */

import axios from 'axios'
import type { QueuedRequest } from './interfaces'
import { API_ENDPOINTS } from './constants'
import { Routes, STORAGE_KEYS } from '../constants'

/** In-memory JWT access token (not persisted to storage). */
let accessToken: string | null = null
/** Whether a token refresh request is currently in flight. */
let isRefreshing = false
/** Queue of requests waiting for the ongoing token refresh to complete. */
let failedQueue: QueuedRequest[] = []

/**
 * Resolves or rejects all queued requests after a token refresh attempt.
 * @param error - The error if the refresh failed, or `null` on success.
 * @param token - The new access token on success, or `null` on failure.
 */
const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token!)
    }
  })
  failedQueue = []
}

/**
 * Stores or clears the in-memory JWT access token.
 * @param token - The new access token, or `null` to clear it.
 */
export const setAccessToken = (token: string | null) => {
  accessToken = token
}

/**
 * Returns the current in-memory JWT access token.
 * @returns The access token string, or `null` if not set.
 */
export const getAccessToken = () => accessToken

/** Axios instance configured with JWT auth interceptors. */
const api = axios.create()

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
      if (!refreshToken) {
        isRefreshing = false
        return Promise.reject(error)
      }

      try {
        const response = await axios.post(API_ENDPOINTS.AUTH_REFRESH, { refreshToken })
        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data
        setAccessToken(newAccessToken)
        localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, newRefreshToken)
        processQueue(null, newAccessToken)
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        setAccessToken(null)
        localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
        window.location.href = Routes.LOGIN
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
