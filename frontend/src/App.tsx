import { Routes as RouterRoutes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import MessagesPage from './pages/MessagesPage'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'
import { useAuth } from './context/AuthContext'
import { Box } from '@chakra-ui/react'
import { Routes } from './constants'

function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Box minH="100vh" bg="gray.50">
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
