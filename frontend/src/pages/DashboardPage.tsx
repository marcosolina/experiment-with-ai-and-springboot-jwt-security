import {
  Container, Card, CardBody, CardHeader, Heading, Text, Badge, HStack, VStack, Button
} from '@chakra-ui/react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  return (
    <Container maxW="container.md" py={8}>
      <VStack spacing={6}>
        <Heading data-testid="dashboard-title">Dashboard</Heading>
        <Card w="full">
          <CardHeader>
            <Heading size="md">User Profile</Heading>
          </CardHeader>
          <CardBody>
            <VStack align="start" spacing={3}>
              <HStack>
                <Text fontWeight="bold">Username:</Text>
                <Text data-testid="dashboard-username">{user?.username}</Text>
              </HStack>
              <HStack>
                <Text fontWeight="bold">Roles:</Text>
                <HStack data-testid="dashboard-roles">
                  {user?.roles.map((role) => (
                    <Badge key={role} colorScheme="blue">{role}</Badge>
                  ))}
                </HStack>
              </HStack>
            </VStack>
          </CardBody>
        </Card>
        <Button colorScheme="blue" onClick={() => navigate('/messages')} data-testid="dashboard-go-messages">
          View Messages
        </Button>
      </VStack>
    </Container>
  )
}
