import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Center, Spinner } from '@chakra-ui/react'
import type { ReactNode } from 'react'

export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <Center h="100vh">
        <Spinner size="xl" data-testid="loading-spinner" />
      </Center>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" />
  }

  return <>{children}</>
}
