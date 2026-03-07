import { useState, useEffect, useCallback } from 'react'
import {
  Container, Heading, VStack, HStack, Input, Button, IconButton,
  Table, Thead, Tbody, Tr, Th, Td, Badge, Card, CardBody,
  Spinner, Center, useToast, Text
} from '@chakra-ui/react'
import { DeleteIcon } from '@chakra-ui/icons'
import api from '../api/axiosConfig'
import { API_ENDPOINTS } from '../api/constants'
import type { Message, HealthStatus } from './interfaces'

export default function MessagesPage() {
  const [messages, setMessages] = useState<Message[]>([])
  const [newMessage, setNewMessage] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [health, setHealth] = useState<HealthStatus | null>(null)
  const toast = useToast()

  const fetchMessages = useCallback(async () => {
    try {
      const response = await api.get(API_ENDPOINTS.MESSAGES)
      setMessages(response.data)
    } catch {
      toast({ title: 'Failed to fetch messages', status: 'error', duration: 3000 })
    } finally {
      setIsLoading(false)
    }
  }, [toast])

  const fetchHealth = useCallback(async () => {
    try {
      const response = await api.get(API_ENDPOINTS.MESSAGES_HEALTH)
      setHealth(response.data)
    } catch {
      setHealth(null)
    }
  }, [])

  useEffect(() => {
    fetchMessages()
    fetchHealth()
  }, [fetchMessages, fetchHealth])

  const handleCreate = async () => {
    if (!newMessage.trim()) return
    setIsSubmitting(true)
    try {
      await api.post(API_ENDPOINTS.MESSAGES, { content: newMessage })
      setNewMessage('')
      await fetchMessages()
      toast({ title: 'Message created', status: 'success', duration: 2000 })
    } catch {
      toast({ title: 'Failed to create message', status: 'error', duration: 3000 })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await api.delete(`${API_ENDPOINTS.MESSAGES}/${id}`)
      await fetchMessages()
      toast({ title: 'Message deleted', status: 'success', duration: 2000 })
    } catch {
      toast({ title: 'Failed to delete message', status: 'error', duration: 3000 })
    }
  }

  if (isLoading) {
    return <Center h="50vh"><Spinner size="xl" /></Center>
  }

  return (
    <Container maxW="container.lg" py={8}>
      <VStack spacing={6} align="stretch">
        <HStack justify="space-between">
          <Heading>Messages</Heading>
          {health && (
            <Badge
              colorScheme={health.supervisorConnected ? 'green' : 'orange'}
              data-testid="messages-health"
              fontSize="sm"
              px={3}
              py={1}
            >
              Supervisor: {health.supervisorConnected ? 'Connected' : 'Disconnected'}
            </Badge>
          )}
        </HStack>

        <Card>
          <CardBody>
            <HStack>
              <Input
                data-testid="message-input"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                placeholder="Type a new message..."
                onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
              />
              <Button
                data-testid="message-submit"
                colorScheme="blue"
                onClick={handleCreate}
                isLoading={isSubmitting}
              >
                Send
              </Button>
            </HStack>
          </CardBody>
        </Card>

        <Card>
          <CardBody p={0}>
            <Table variant="simple" data-testid="messages-list">
              <Thead>
                <Tr>
                  <Th>ID</Th>
                  <Th>Content</Th>
                  <Th>Author</Th>
                  <Th>Created</Th>
                  <Th>Actions</Th>
                </Tr>
              </Thead>
              <Tbody>
                {messages.length === 0 ? (
                  <Tr>
                    <Td colSpan={5}>
                      <Text textAlign="center" color="gray.500">No messages yet</Text>
                    </Td>
                  </Tr>
                ) : (
                  messages.map((msg) => (
                    <Tr key={msg.id} data-testid={`message-item-${msg.id}`}>
                      <Td>{msg.id}</Td>
                      <Td>{msg.content}</Td>
                      <Td>{msg.author}</Td>
                      <Td>{new Date(msg.createdAt).toLocaleString()}</Td>
                      <Td>
                        <IconButton
                          aria-label="Delete message"
                          icon={<DeleteIcon />}
                          size="sm"
                          colorScheme="red"
                          variant="ghost"
                          data-testid={`message-delete-${msg.id}`}
                          onClick={() => handleDelete(msg.id)}
                        />
                      </Td>
                    </Tr>
                  ))
                )}
              </Tbody>
            </Table>
          </CardBody>
        </Card>
      </VStack>
    </Container>
  )
}
