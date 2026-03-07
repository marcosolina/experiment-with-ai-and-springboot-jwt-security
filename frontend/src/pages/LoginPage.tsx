import { useState } from 'react'
import {
  Card, CardBody, CardHeader, Heading, Input, Button, Alert, AlertIcon,
  VStack, FormControl, FormLabel, Center
} from '@chakra-ui/react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Routes } from '../constants'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      await login(username, password)
      navigate(Routes.DASHBOARD)
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      const message = axiosError?.response?.data?.message || 'Login failed. Please check your credentials.'
      setError(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Center minH="100vh">
      <Card w="400px" data-testid="login-card">
        <CardHeader>
          <Heading size="lg" textAlign="center">Sign In</Heading>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit}>
            <VStack spacing={4}>
              {error && (
                <Alert status="error" borderRadius="md" data-testid="login-error">
                  <AlertIcon />
                  {error}
                </Alert>
              )}
              <FormControl isRequired>
                <FormLabel>Username</FormLabel>
                <Input
                  data-testid="login-username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter username"
                />
              </FormControl>
              <FormControl isRequired>
                <FormLabel>Password</FormLabel>
                <Input
                  data-testid="login-password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter password"
                />
              </FormControl>
              <Button
                data-testid="login-submit"
                type="submit"
                colorScheme="blue"
                w="full"
                isLoading={isSubmitting}
              >
                Sign In
              </Button>
            </VStack>
          </form>
        </CardBody>
      </Card>
    </Center>
  )
}
