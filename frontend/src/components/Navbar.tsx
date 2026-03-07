import { Flex, Heading, Text, Button, Spacer, HStack } from '@chakra-ui/react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <Flex as="nav" bg="blue.600" color="white" px={6} py={3} align="center" data-testid="navbar">
      <Heading size="md">POC Security</Heading>
      <HStack ml={8} spacing={4}>
        <Button
          variant={location.pathname === '/dashboard' ? 'solid' : 'ghost'}
          colorScheme="whiteAlpha"
          size="sm"
          onClick={() => navigate('/dashboard')}
          data-testid="nav-dashboard"
        >
          Dashboard
        </Button>
        <Button
          variant={location.pathname === '/messages' ? 'solid' : 'ghost'}
          colorScheme="whiteAlpha"
          size="sm"
          onClick={() => navigate('/messages')}
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
