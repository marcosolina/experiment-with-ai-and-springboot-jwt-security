import { test, expect } from '@playwright/test'
import jwt from 'jsonwebtoken'
import fs from 'fs'
import path from 'path'

const SHARED_SECRET_BASE64 = 'bXktc3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTI1Ni1iaXRz'
const secret = Buffer.from(SHARED_SECRET_BASE64, 'base64')
const MESSAGES_BASE = process.env.MESSAGES_BASE || 'http://localhost:8082'
const RESULTS_DIR = path.join(__dirname, '..', 'test-results', 'api-reports')

function generateToken(overrides: {
  username?: string
  roles?: string[]
  expiresIn?: string | number
  issuer?: string
  algorithm?: jwt.Algorithm
  secret?: Buffer
} = {}): string {
  const {
    username = 'service-account',
    roles = ['USER'],
    expiresIn = '1h',
    issuer = 'shared-secret',
    algorithm = 'HS256',
    secret: customSecret = secret,
  } = overrides

  return jwt.sign(
    { iss: issuer, sub: username, roles },
    customSecret,
    { algorithm, expiresIn }
  )
}

interface ApiRecord {
  test: string
  timestamp: string
  request: {
    method: string
    url: string
    headers: Record<string, string>
    body?: unknown
  }
  response: {
    status: number
    headers: Record<string, string>
    body: unknown
  }
}

async function saveApiRecord(testName: string, records: ApiRecord[]) {
  fs.mkdirSync(RESULTS_DIR, { recursive: true })
  const filename = testName.replace(/[^a-zA-Z0-9]/g, '-').replace(/-+/g, '-').toLowerCase()
  const filepath = path.join(RESULTS_DIR, `${filename}.json`)
  fs.writeFileSync(filepath, JSON.stringify(records, null, 2))
}

function responseHeaders(response: { headers: () => Record<string, string> }): Record<string, string> {
  const raw = response.headers()
  const filtered: Record<string, string> = {}
  for (const [key, value] of Object.entries(raw)) {
    filtered[key] = value
  }
  return filtered
}

test.describe('Messages API - Shared Secret JWT', () => {

  test('should list messages with valid shared-secret token', async ({ request }) => {
    const token = generateToken()
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages`
    const requestHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

    const response = await request.get(url, { headers: requestHeaders })
    const body = await response.json()

    records.push({
      test: 'list messages with valid shared-secret token',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: requestHeaders },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect(response.status()).toBe(200)
    expect(Array.isArray(body)).toBe(true)
    expect(body.length).toBeGreaterThan(0)

    await saveApiRecord('list-messages-shared-secret', records)
  })

  test('should get single message with valid shared-secret token', async ({ request }) => {
    const token = generateToken()
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages/1`
    const requestHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

    const response = await request.get(url, { headers: requestHeaders })
    const body = await response.json()

    records.push({
      test: 'get single message with valid shared-secret token',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: requestHeaders },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect(response.status()).toBe(200)
    expect(body.id).toBe(1)
    expect(body.content).toBeTruthy()

    await saveApiRecord('get-message-shared-secret', records)
  })

  test('should create message with valid shared-secret token', async ({ request }) => {
    const token = generateToken({ username: 'test-service', roles: ['USER', 'ADMIN'] })
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages`
    const requestHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
    const requestBody = { content: 'Message from shared-secret token' }

    const response = await request.post(url, {
      headers: requestHeaders,
      data: requestBody,
    })
    const body = await response.json()

    records.push({
      test: 'create message with valid shared-secret token',
      timestamp: new Date().toISOString(),
      request: { method: 'POST', url, headers: requestHeaders, body: requestBody },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect(response.status()).toBe(200)
    expect(body.content).toBe('Message from shared-secret token')
    expect(body.author).toBe('test-service')

    await saveApiRecord('create-message-shared-secret', records)
  })

  test('should delete message with valid shared-secret token', async ({ request }) => {
    const token = generateToken()
    const records: ApiRecord[] = []

    // First create a message to delete
    const createUrl = `${MESSAGES_BASE}/api/messages`
    const createHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
    const createBody = { content: 'Message to be deleted' }

    const createResponse = await request.post(createUrl, {
      headers: createHeaders,
      data: createBody,
    })
    const created = await createResponse.json()

    records.push({
      test: 'delete message - step 1: create',
      timestamp: new Date().toISOString(),
      request: { method: 'POST', url: createUrl, headers: createHeaders, body: createBody },
      response: { status: createResponse.status(), headers: responseHeaders(createResponse), body: created },
    })

    // Now delete it
    const deleteUrl = `${MESSAGES_BASE}/api/messages/${created.id}`
    const deleteHeaders = { Authorization: `Bearer ${token}` }

    const deleteResponse = await request.delete(deleteUrl, { headers: deleteHeaders })
    const deleteBody = await deleteResponse.json()

    records.push({
      test: 'delete message - step 2: delete',
      timestamp: new Date().toISOString(),
      request: { method: 'DELETE', url: deleteUrl, headers: deleteHeaders },
      response: { status: deleteResponse.status(), headers: responseHeaders(deleteResponse), body: deleteBody },
    })

    expect(deleteResponse.status()).toBe(200)

    // Verify it's gone
    const verifyResponse = await request.get(deleteUrl, { headers: createHeaders })

    records.push({
      test: 'delete message - step 3: verify deleted',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url: deleteUrl, headers: createHeaders },
      response: { status: verifyResponse.status(), headers: responseHeaders(verifyResponse), body: null },
    })

    expect(verifyResponse.status()).toBe(404)

    await saveApiRecord('delete-message-shared-secret', records)
  })

  test('should reject request with no token', async ({ request }) => {
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages`
    const requestHeaders = { 'Content-Type': 'application/json' }

    const response = await request.get(url, { headers: requestHeaders })

    let body: unknown
    try {
      body = await response.json()
    } catch {
      body = await response.text()
    }

    records.push({
      test: 'reject request with no token',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: requestHeaders },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect([401, 403]).toContain(response.status())

    await saveApiRecord('reject-no-token', records)
  })

  test('should reject request with invalid shared-secret token', async ({ request }) => {
    const wrongSecret = Buffer.from('wrong-secret-key-that-is-at-least-256-bits-long!!')
    const token = generateToken({ secret: wrongSecret })
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages`
    const requestHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

    const response = await request.get(url, { headers: requestHeaders })

    let body: unknown
    try {
      body = await response.json()
    } catch {
      body = await response.text()
    }

    records.push({
      test: 'reject request with invalid shared-secret token',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: requestHeaders },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect([401, 403]).toContain(response.status())

    await saveApiRecord('reject-invalid-secret', records)
  })

  test('should reject request with expired shared-secret token', async ({ request }) => {
    const token = generateToken({ expiresIn: -10 })
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages`
    const requestHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

    const response = await request.get(url, { headers: requestHeaders })

    let body: unknown
    try {
      body = await response.json()
    } catch {
      body = await response.text()
    }

    records.push({
      test: 'reject request with expired shared-secret token',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: requestHeaders },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect([401, 403]).toContain(response.status())

    await saveApiRecord('reject-expired-token', records)
  })

  test('should reject request with unknown issuer', async ({ request }) => {
    const token = generateToken({ issuer: 'unknown-issuer' })
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages`
    const requestHeaders = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

    const response = await request.get(url, { headers: requestHeaders })

    let body: unknown
    try {
      body = await response.json()
    } catch {
      body = await response.text()
    }

    records.push({
      test: 'reject request with unknown issuer',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: requestHeaders },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect([401, 403]).toContain(response.status())

    await saveApiRecord('reject-unknown-issuer', records)
  })

  test('should allow health endpoint without token', async ({ request }) => {
    const records: ApiRecord[] = []

    const url = `${MESSAGES_BASE}/api/messages/health`

    const response = await request.get(url)
    const body = await response.json()

    records.push({
      test: 'health endpoint without token',
      timestamp: new Date().toISOString(),
      request: { method: 'GET', url, headers: {} },
      response: { status: response.status(), headers: responseHeaders(response), body },
    })

    expect(response.status()).toBe(200)
    expect(body.status).toBe('UP')

    await saveApiRecord('health-no-token', records)
  })
})
