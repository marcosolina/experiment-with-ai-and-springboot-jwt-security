import jwt from 'jsonwebtoken'

// Same shared secret as configured in messages app (base64 encoded)
const SHARED_SECRET_BASE64 = 'bXktc3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTI1Ni1iaXRz'
const secret = Buffer.from(SHARED_SECRET_BASE64, 'base64')

// Parse CLI arguments
const args = process.argv.slice(2)
const params = {}
for (let i = 0; i < args.length; i += 2) {
  const key = args[i]?.replace('--', '')
  const value = args[i + 1]
  if (key && value) params[key] = value
}

const username = params.username || 'service-account'
const roles = params.roles ? params.roles.split(',') : ['USER']
const expiryHours = parseInt(params.expiry || '1', 10)

const payload = {
  iss: 'shared-secret',
  sub: username,
  roles: roles,
}

const token = jwt.sign(payload, secret, {
  algorithm: 'HS256',
  expiresIn: `${expiryHours}h`,
})

console.log('')
console.log('=== Generated JWT Token (HS256 / shared-secret) ===')
console.log('')
console.log(token)
console.log('')
console.log('=== Token Claims ===')
console.log('')
console.log(JSON.stringify(jwt.decode(token), null, 2))
console.log('')
console.log('=== Usage ===')
console.log('')
console.log(`curl -H "Authorization: Bearer ${token}" http://localhost:8082/api/messages`)
console.log('')
