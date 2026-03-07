/**
 * Represents a message entity as returned by the resource server's messages API.
 */
export interface Message {
  /** Unique numeric identifier of the message. */
  id: number
  /** Text content of the message. */
  content: string
  /** Username of the message author. */
  author: string
  /** ISO 8601 timestamp of when the message was created. */
  createdAt: string
}

/**
 * Health check response from the resource server, indicating its
 * connectivity to the supervisor auth server.
 */
export interface HealthStatus {
  /** Overall health status string (e.g., "UP"). */
  status: string
  /** Whether the resource server can reach the supervisor for token validation. */
  supervisorConnected: boolean
}
