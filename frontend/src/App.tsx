import { Routes as RouterRoutes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import MessagesPage from './pages/MessagesPage'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'
import { useAuth } from './context/AuthContext'
import { Box, useColorModeValue } from '@chakra-ui/react'
import { Routes } from './constants'

/**
 * Root application component that defines the client-side routing structure.
 *
 * Conditionally renders the {@link Navbar} for authenticated users and sets up
 * routes for login, dashboard, and messages pages. Unauthenticated users are
 * redirected to login; authenticated users accessing `/login` are redirected
 * to the dashboard. Any unknown paths fall back to the appropriate default.
 *
 * @returns The routed application layout.
 */
function App() {
  const { isAuthenticated } = useAuth()
  const bg = useColorModeValue('gray.50', 'gray.800')

  return (
    <Box minH="100vh" bg={bg}>
      {isAuthenticated && <Navbar />}
      <RouterRoutes>
        <Route path={Routes.LOGIN} element={isAuthenticated ? <Navigate to={Routes.DASHBOARD} /> : <LoginPage />} />
        <Route path={Routes.DASHBOARD} element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path={Routes.MESSAGES} element={<ProtectedRoute><MessagesPage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to={isAuthenticated ? Routes.DASHBOARD : Routes.LOGIN} />} />
      </RouterRoutes>
    </Box>
  )
}

export default App
