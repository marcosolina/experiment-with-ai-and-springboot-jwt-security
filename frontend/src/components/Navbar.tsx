import { Flex, Heading, Text, Button, Spacer, HStack } from '@chakra-ui/react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Routes } from '../constants'

/**
 * Top navigation bar displayed when the user is authenticated.
 *
 * Renders navigation links to the dashboard and messages pages with
 * active-state highlighting based on the current route. Also shows the
 * current username and a logout button.
 *
 * @returns The navigation bar UI.
 */
export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = async () => {
    await logout()
    navigate(Routes.LOGIN)
  }

  return (
    <Flex as="nav" bg="blue.600" color="white" px={6} py={3} align="center" data-testid="navbar">
      <Heading size="md">POC Security</Heading>
      <HStack ml={8} spacing={4}>
        <Button
          variant={location.pathname === Routes.DASHBOARD ? 'solid' : 'ghost'}
          colorScheme="whiteAlpha"
          size="sm"
          onClick={() => navigate(Routes.DASHBOARD)}
          data-testid="nav-dashboard"
        >
          Dashboard
        </Button>
        <Button
          variant={location.pathname === Routes.MESSAGES ? 'solid' : 'ghost'}
          colorScheme="whiteAlpha"
          size="sm"
          onClick={() => navigate(Routes.MESSAGES)}
          data-testid="nav-messages"
        >
          Messages
        </Button>
      </HStack>
      <Spacer />
      <HStack spacing={4}>
        <Text fontSize="sm" data-testid="navbar-username">{user?.username}</Text>
        <Button size="sm" colorScheme="red" onClick={handleLogout} data-testid="navbar-logout">
          Logout
        </Button>
      </HStack>
    </Flex>
  )
}
