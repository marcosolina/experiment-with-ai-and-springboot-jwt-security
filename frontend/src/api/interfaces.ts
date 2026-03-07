export interface QueuedRequest {
  resolve: (token: string) => void
  reject: (error: unknown) => void
}
