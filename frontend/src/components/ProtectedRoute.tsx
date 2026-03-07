import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Center, Spinner } from '@chakra-ui/react'
import type { ReactNode } from 'react'

/**
 * Route guard component that restricts access to authenticated users.
 *
 * While the auth state is loading, a full-screen spinner is displayed.
 * If the user is not authenticated, they are redirected to the login page.
 * Otherwise, the child components are rendered.
 *
 * @param props.children - The protected content to render when authenticated.
 * @returns The children, a loading spinner, or a redirect to login.
 */
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
