export interface Message {
  id: number
  content: string
  author: string
  createdAt: string
}

export interface HealthStatus {
  status: string
  supervisorConnected: boolean
}
