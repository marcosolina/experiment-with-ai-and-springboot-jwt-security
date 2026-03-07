# POC Security - JWT Authentication with Spring Boot & React

A proof of concept demonstrating custom JWT security across multiple microservices with a React frontend.

## Architecture

```
                         +----------------+
                         |    Frontend    |
                         | React + Vite   |
                         | TypeScript     |
                         | Chakra UI      |
                         +---+--------+---+
                             |        |
                    Login &  |        |  Access protected
                    get JWT  |        |  endpoints (JWT)
                             |        |
                    +--------v--+ +---v-----------+
                    | Supervisor | |   Messages    |
                    | (Auth+Res) | | (Resource)    |
                    +--------+--+ +---+-----------+
                             |        |
                             |  JWKS  | (startup + cached)
                             +--------+
```

| Service | Description | Port |
|---------|-------------|------|
| **Supervisor** | Authorization Server + Resource Server (Spring Boot 4 / Java 25) | 8081 |
| **Messages** | Resource Server with dual-token validation (Spring Boot 4 / Java 25) | 8082 |
| **Frontend** | SPA with login, dashboard, and messaging (React + Vite + TypeScript + Chakra UI) | 5173 |
| **E2E Tests** | Integration tests (Playwright + TypeScript) | 9323 (report) |

## Quick Start

The only prerequisites are **Git** and **Docker**. No Java, Node.js, or Maven needed.

### Windows (PowerShell)

```powershell
irm https://raw.githubusercontent.com/marcosolina/experiment-with-ai-and-springboot-jwt-security/main/install.ps1 | iex
```

### macOS / Linux (bash)

```bash
curl -fsSL https://raw.githubusercontent.com/marcosolina/experiment-with-ai-and-springboot-jwt-security/main/install.sh | bash
```

### Manual setup

```bash
git clone https://github.com/marcosolina/experiment-with-ai-and-springboot-jwt-security.git
cd experiment-with-ai-and-springboot-jwt-security
docker compose up --build
```

Once everything is running:

| URL | Description |
|-----|-------------|
| http://localhost:5173 | Frontend UI |
| http://localhost:9323 | Playwright test report |
| http://localhost:8081 | Supervisor API |
| http://localhost:8082 | Messages API |

**Default credentials:** `admin` / `admin123`

## Key Features

### Dual-Token Validation (Messages Service)

The Messages service accepts JWT tokens from two sources:

- **RS256 tokens** issued by the Supervisor (validated via JWKS public key)
- **HS256 tokens** signed with a shared secret key (fallback mechanism)

### Graceful Degradation

The Messages service starts successfully even if the Supervisor is unavailable. In this mode:

- Supervisor-issued tokens are **rejected** (no public key available)
- Shared-secret tokens are **accepted** (validated with the local HMAC key)
- A background task periodically attempts to fetch the Supervisor's JWKS
- Once the Supervisor becomes available, both token types are accepted

### Token Refresh

The frontend handles token expiration transparently using Axios interceptors. When an access token expires, the refresh token is used to obtain a new one without interrupting the user session.

## Project Structure

```
poc-security/
|-- supervisor/          Spring Boot 4 auth server
|-- messages/            Spring Boot 4 resource server
|-- frontend/            React + Vite + TypeScript + Chakra UI
|-- e2e-tests/           Playwright integration tests
|-- scripts/             Token generator (shared-secret JWT)
|-- docker-compose.yml   Orchestrates all services
|-- install.ps1          Windows quick start script
|-- install.sh           macOS/Linux quick start script
```

## API Endpoints

### Supervisor (port 8081)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | Public | Authenticate and get JWT tokens |
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | Bearer | Invalidate refresh token |
| GET | `/api/auth/.well-known/jwks.json` | Public | RSA public key (JWKS format) |
| GET | `/api/users/me` | Bearer | Current user profile |
| GET | `/api/users` | Bearer (ADMIN) | List all users |

### Messages (port 8082)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/messages` | Bearer | List all messages |
| GET | `/api/messages/{id}` | Bearer | Get message by ID |
| POST | `/api/messages` | Bearer | Create a message |
| DELETE | `/api/messages/{id}` | Bearer | Delete a message |
| GET | `/api/messages/health` | Public | Health check + supervisor status |

## Token Generator Script

Generate shared-secret JWT tokens for testing the fallback mechanism:

```bash
cd scripts
npm install
node generate-token.js
```

With options:

```bash
node generate-token.js --username myuser --roles USER,ADMIN --expiry 2
```

Test with curl:

```bash
TOKEN=$(node generate-token.js 2>/dev/null | head -4 | tail -1)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/messages
```

## E2E Tests

The test suite includes 21 tests:

- **Login tests** - form display, successful login, invalid credentials
- **Dashboard tests** - user profile display, unauthenticated redirect, navigation
- **Messages tests** - list, create, delete messages, health status
- **Logout tests** - session clearing, post-logout protection
- **Messages API tests** - shared-secret token CRUD, token rejection (no token, invalid secret, expired, unknown issuer), health endpoint

Test results are saved as JSON files in `e2e-tests/test-results/api-reports/` with full request/response details including headers, URLs, and payloads.

## Local Development (without Docker)

Requirements: Java 25, Maven, Node.js

```bash
# Terminal 1 - Supervisor
cd supervisor && mvn spring-boot:run

# Terminal 2 - Messages
cd messages && mvn spring-boot:run

# Terminal 3 - Frontend
cd frontend && npm install && npm run dev

# Terminal 4 - E2E Tests
cd e2e-tests && npm install && npx playwright install chromium && npx playwright test
```

## Stopping

```bash
docker compose down
```

## Tech Stack

- **Backend:** Spring Boot 4.0, Spring Security 7.0, Java 25, Maven
- **Auth:** JWT (RS256 + HS256), JWKS, BCrypt
- **Frontend:** React 18, Vite 5, TypeScript, Chakra UI v2, Axios
- **Testing:** Playwright, TypeScript
- **Infrastructure:** Docker, Docker Compose, nginx
