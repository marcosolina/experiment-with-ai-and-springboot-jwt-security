# POC Security - Implementation Plan

## Overview

Three applications + integration tests demonstrating custom JWT security:

| App | Type | Role | Port |
|-----|------|------|------|
| **supervisor** | Spring Boot 4 + Java 25 + Maven | Authorization Server + Resource Server | 8081 |
| **messages** | Spring Boot 4 + Java 25 + Maven | Resource Server (dual-token validation) | 8082 |
| **frontend** | React + Vite + TypeScript + Chakra UI | SPA UI | 5173 |
| **e2e-tests** | Playwright + TypeScript | Integration tests | - |

Plus a **token-generator script** (shell + node) to create JWT tokens signed with the shared secret key.

---

## Architecture

```
                         ┌──────────────┐
                         │   Frontend   │
                         │ (React/Vite) │
                         │  TypeScript  │
                         │  Chakra UI   │
                         └──┬───────┬───┘
                            │       │
                   Login &  │       │  Access protected
                   get JWT  │       │  endpoints (JWT)
                            │       │
                   ┌────────▼──┐ ┌──▼──────────┐
                   │ Supervisor │ │   Messages   │
                   │ (Auth+Res) │ │ (Resource)   │
                   └────────┬──┘ └──┬───────────┘
                            │       │
                            │  JWKS │ (startup + cached)
                            └───────┘

    Messages also accepts tokens signed with a shared HMAC secret (fallback).
    If Supervisor is down, Messages rejects RSA tokens but accepts HMAC tokens.
```

---

## Step-by-Step Implementation Plan

### Phase 1: Project Scaffolding

#### Step 1.1 - Create Supervisor Spring Boot app
- Initialize Maven project with Spring Boot 4 parent (`spring-boot-starter-parent` 4.x)
- Set Java 25 in `pom.xml` (`<java.version>25</java.version>`)
- Dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-security`
  - `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.x)
  - `org.bouncycastle:bcprov-jdk18on` (for RSA key generation if needed)
- Package structure:
  ```
  supervisor/
  └── src/main/java/com/microdevice/supervisor/
      ├── SupervisorApplication.java
      ├── config/
      │   ├── SecurityConfig.java
      │   ├── CorsConfig.java
      │   └── RsaKeyConfig.java
      ├── controller/
      │   ├── AuthController.java
      │   └── UserController.java
      ├── dto/
      │   ├── LoginRequest.java
      │   ├── LoginResponse.java
      │   ├── RegisterRequest.java
      │   └── JwksResponse.java
      ├── model/
      │   └── User.java
      ├── service/
      │   ├── JwtService.java
      │   └── UserService.java
      ├── filter/
      │   └── JwtAuthenticationFilter.java
      └── repository/
          └── UserRepository.java (in-memory)
  ```

#### Step 1.2 - Create Messages Spring Boot app
- Same Maven/Spring Boot 4/Java 25 setup
- Dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-security`
  - `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- Package structure:
  ```
  messages/
  └── src/main/java/com/microdevice/messages/
      ├── MessagesApplication.java
      ├── config/
      │   ├── SecurityConfig.java
      │   ├── CorsConfig.java
      │   └── JwtConfig.java
      ├── controller/
      │   └── MessageController.java
      ├── dto/
      │   └── MessageResponse.java
      ├── service/
      │   └── JwtValidationService.java
      ├── filter/
      │   └── JwtAuthenticationFilter.java
      └── model/
          └── Message.java
  ```

#### Step 1.3 - Create Frontend React + Vite + TypeScript app
- `npm create vite@latest frontend -- --template react-ts`
- Install dependencies: `axios`, `react-router-dom`, `@chakra-ui/react`, `@emotion/react`
- All components use **TypeScript only** (`.tsx` / `.ts`)
- All interactive components include `data-testid` attributes for Playwright tests
- Structure:
  ```
  frontend/
  ├── package.json
  ├── tsconfig.json
  ├── vite.config.ts
  └── src/
      ├── App.tsx
      ├── main.tsx
      ├── api/
      │   └── axiosConfig.ts        (interceptors for JWT + refresh)
      ├── context/
      │   └── AuthContext.tsx        (auth state management)
      ├── pages/
      │   ├── LoginPage.tsx
      │   ├── DashboardPage.tsx
      │   └── MessagesPage.tsx
      └── components/
          ├── ProtectedRoute.tsx
          └── Navbar.tsx
  ```

#### Step 1.4 - Create Playwright E2E test project
- Initialize Playwright with TypeScript: `npm init playwright@latest`
- Structure:
  ```
  e2e-tests/
  ├── package.json
  ├── playwright.config.ts
  └── tests/
      ├── login.spec.ts
      ├── dashboard.spec.ts
      ├── messages.spec.ts
      └── logout.spec.ts
  ```

---

### Phase 2: Supervisor App (Authorization Server + Resource Server)

#### Step 2.1 - RSA Key Pair Management
- Generate RSA 2048-bit key pair at startup (or load from files)
- Store keys in `RsaKeyConfig` as a Spring `@Configuration` bean
- Expose a **JWKS endpoint** (`GET /api/auth/.well-known/jwks.json`) returning the public key in JWK format
  - Include `kid` (key ID) in the JWK so Messages can identify which key to use

#### Step 2.2 - User Model & Repository
- Simple in-memory user store (HashMap-based)
- `User` record/class: `id`, `username`, `password` (BCrypt hashed), `roles`
- Pre-seed a default user: `admin / admin123`

#### Step 2.3 - JWT Token Service
- **Access token**: signed with RSA private key (RS256 algorithm)
  - Claims: `sub` (username), `roles`, `iat`, `exp` (15 min), `iss` ("supervisor")
  - Header includes `kid` matching the JWKS endpoint
- **Refresh token**: signed with RSA private key (RS256)
  - Longer expiry (24h), minimal claims
  - Stored in-memory for revocation tracking

#### Step 2.4 - Auth Controller
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/login` | POST | Public | Authenticate user, return access + refresh tokens |
| `/api/auth/register` | POST | Public | Register new user |
| `/api/auth/refresh` | POST | Public | Exchange refresh token for new access token |
| `/api/auth/logout` | POST | Authenticated | Invalidate refresh token |
| `/api/auth/.well-known/jwks.json` | GET | Public | Expose RSA public key in JWKS format |

#### Step 2.5 - Resource Endpoints (Protected)
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/users/me` | GET | Bearer JWT | Return current user profile |
| `/api/users` | GET | Bearer JWT (ADMIN) | List all users |

#### Step 2.6 - Security Configuration
- `SecurityFilterChain` with:
  - Public paths: `/api/auth/**`
  - All other paths require authentication
  - Stateless session management
  - CORS configured for frontend origin
- Custom `JwtAuthenticationFilter` that:
  - Extracts Bearer token from `Authorization` header
  - Validates the RSA-signed JWT
  - Sets `SecurityContextHolder` with authenticated principal

---

### Phase 3: Messages App (Resource Server with Fallback)

#### Step 3.1 - Dual-Token JWT Configuration
- **Config properties** (`application.yml`):
  ```yaml
  jwt:
    supervisor:
      jwks-url: http://localhost:8081/api/auth/.well-known/jwks.json
      issuer: supervisor
    shared-secret:
      key: "base64-encoded-256-bit-secret"
      issuer: shared-secret
  ```

#### Step 3.2 - JWT Validation Service (Core Logic)
This is the critical component implementing the fallback mechanism:

```
Token arrives -> Check "iss" claim
  |-- issuer == "supervisor"
  |     |-- Supervisor public key cached? -> Validate with RSA public key
  |     |-- Not cached? -> Try fetching JWKS from Supervisor
  |     |     |-- Success -> Cache key, validate token
  |     |     +-- Failure (Supervisor down) -> REJECT token
  |     +-- Cached key but validation fails -> REJECT token
  |
  +-- issuer == "shared-secret"
        +-- Validate with HMAC shared secret key (HS256)
              |-- Valid -> Accept
              +-- Invalid -> Reject
```

- On startup, attempt to fetch JWKS from Supervisor
  - **Success**: cache the public key, set `supervisorAvailable = true`
  - **Failure**: log warning, set `supervisorAvailable = false`, continue starting
- Periodic background task (every 60s) tries to refresh the JWKS if supervisor was previously down
- The key insight: **Messages app starts successfully regardless of Supervisor availability**

#### Step 3.3 - JWT Authentication Filter
- Extract Bearer token from `Authorization` header
- Decode JWT header to read `alg` and claims to read `iss`
- Route to appropriate validation:
  - `RS256` + issuer `supervisor` -> validate with cached RSA public key
  - `HS256` + issuer `shared-secret` -> validate with shared secret
- On successful validation, set `SecurityContextHolder`

#### Step 3.4 - Message Endpoints (Protected)
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/messages` | GET | Bearer JWT | List all messages |
| `/api/messages/{id}` | GET | Bearer JWT | Get message by ID |
| `/api/messages` | POST | Bearer JWT | Create new message |
| `/api/messages/{id}` | DELETE | Bearer JWT | Delete message |
| `/api/messages/health` | GET | Public | Health check + supervisor connectivity status |

- In-memory message store (ArrayList/HashMap) with some seed data

#### Step 3.5 - Security Configuration
- Similar to Supervisor: stateless, CORS, JWT filter
- Public paths: `/api/messages/health`
- All other `/api/messages/**` paths require authentication

---

### Phase 4: Frontend (React + Vite + TypeScript + Chakra UI)

#### Step 4.1 - Auth Context & Token Management
- `AuthContext` provides: `user`, `token`, `login()`, `logout()`, `isAuthenticated`
- Store access token in memory (state) and refresh token in `localStorage`
- On app load, try to refresh the token using stored refresh token

#### Step 4.2 - Axios Configuration
- Create Axios instance with base URL
- **Request interceptor**: attach `Authorization: Bearer <token>` header
- **Response interceptor**: on 401 response, attempt token refresh
  - If refresh succeeds, retry original request
  - If refresh fails, redirect to login

#### Step 4.3 - Login Page
- Chakra UI `Card` with `Input` fields for username and password
- Chakra UI `Button` for submit
- `data-testid` attributes: `login-username`, `login-password`, `login-submit`, `login-error`
- POST to Supervisor `/api/auth/login`
- On success: store tokens, redirect to dashboard
- On error: show Chakra UI `Alert` error message

#### Step 4.4 - Dashboard Page
- Chakra UI `Card` showing current user info (from `/api/users/me` on Supervisor)
- Navigation links to Messages page
- `data-testid` attributes: `dashboard-username`, `dashboard-roles`, `nav-messages`

#### Step 4.5 - Messages Page
- Chakra UI `Table` to display messages from Messages app (`/api/messages`)
- Chakra UI `Input` + `Button` to create new messages
- Chakra UI `IconButton` for delete on each row
- `data-testid` attributes: `messages-list`, `message-item-{id}`, `message-input`, `message-submit`, `message-delete-{id}`

#### Step 4.6 - Navbar & Logout
- Chakra UI `Flex` navbar with app title and user info
- Chakra UI `Button` for logout
- `data-testid` attributes: `navbar-username`, `navbar-logout`
- Logout flow:
  - Call `POST /api/auth/logout` on Supervisor (invalidate refresh token)
  - Clear tokens from memory/localStorage
  - Redirect to login

#### Step 4.7 - Vite Proxy Config
- Configure `vite.config.ts` with proxy rules:
  - `/api/auth/**` and `/api/users/**` -> `http://localhost:8081`
  - `/api/messages/**` -> `http://localhost:8082`

---

### Phase 5: Token Generator Script

#### Step 5.1 - Node.js Script (`scripts/generate-token.js`)
- Uses `jsonwebtoken` npm package
- Accepts CLI parameters: `--username`, `--roles`, `--expiry`
- Generates a JWT signed with HS256 using the **same shared secret** configured in Messages app
- Token claims:
  - `iss`: "shared-secret"
  - `sub`: provided username (default: "service-account")
  - `roles`: provided roles (default: ["USER"])
  - `iat`: current timestamp
  - `exp`: current + expiry (default: 1 hour)
- Outputs the token to stdout

#### Step 5.2 - Shell Wrapper (`scripts/generate-token.sh`)
- Thin wrapper that calls `node generate-token.js` with forwarded arguments
- Includes usage/help text

---

### Phase 6: Configuration & CORS

#### Step 6.1 - Application Configuration Files

**supervisor/src/main/resources/application.yml**:
```yaml
server:
  port: 8081

spring:
  application:
    name: supervisor

jwt:
  access-token-expiration: 900000      # 15 minutes
  refresh-token-expiration: 86400000   # 24 hours
```

**messages/src/main/resources/application.yml**:
```yaml
server:
  port: 8082

spring:
  application:
    name: messages

jwt:
  supervisor:
    jwks-url: http://localhost:8081/api/auth/.well-known/jwks.json
    issuer: supervisor
    jwks-refresh-interval: 60000  # 60 seconds
  shared-secret:
    key: "bXktc3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTI1Ni1iaXRz"
    issuer: shared-secret
```

#### Step 6.2 - CORS
- Both backends allow origin: `http://localhost:5173`
- Allow headers: `Authorization`, `Content-Type`
- Allow methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

---

### Phase 7: Playwright Integration Tests

#### Step 7.1 - Test Configuration (`playwright.config.ts`)
- Base URL: `http://localhost:5173`
- Web server command to start frontend (or expect it to be running)
- Browsers: Chromium (default), optionally Firefox
- Screenshot on failure

#### Step 7.2 - Login Tests (`tests/login.spec.ts`)
- **Test: successful login** - Fill `[data-testid="login-username"]` and `[data-testid="login-password"]`, click `[data-testid="login-submit"]`, verify redirect to dashboard
- **Test: failed login with wrong credentials** - Verify `[data-testid="login-error"]` is displayed
- **Test: empty form submission** - Verify validation messages

#### Step 7.3 - Dashboard Tests (`tests/dashboard.spec.ts`)
- **Test: displays user info** - Login first, verify `[data-testid="dashboard-username"]` shows correct username
- **Test: unauthenticated redirect** - Navigate to dashboard without login, verify redirect to login page

#### Step 7.4 - Messages Tests (`tests/messages.spec.ts`)
- **Test: list messages** - Login, navigate to messages, verify `[data-testid="messages-list"]` has items
- **Test: create message** - Fill `[data-testid="message-input"]`, click `[data-testid="message-submit"]`, verify new message appears
- **Test: delete message** - Click `[data-testid="message-delete-{id}"]`, verify message removed

#### Step 7.5 - Logout Tests (`tests/logout.spec.ts`)
- **Test: logout clears session** - Login, click `[data-testid="navbar-logout"]`, verify redirect to login
- **Test: cannot access protected page after logout** - After logout, navigate to dashboard, verify redirect to login

#### Step 7.6 - Auth Helper
- Create a shared test helper/fixture that performs login via API (not UI) for tests that need authentication as a precondition, to speed up tests

---

### Phase 8: Testing Scenarios (Manual)

#### Test 1 - Normal Flow (both apps running)
1. Start Supervisor (port 8081)
2. Start Messages (port 8082) - fetches JWKS from Supervisor
3. Start Frontend (port 5173)
4. Login via UI -> get JWT -> access both Supervisor and Messages endpoints

#### Test 2 - Fallback Flow (Supervisor down)
1. Start Messages WITHOUT Supervisor running
2. Messages starts successfully (logs warning about Supervisor being unavailable)
3. Use `generate-token.sh` to create a shared-secret JWT
4. Call Messages endpoints with the shared-secret JWT -> succeeds
5. Try using a Supervisor-issued JWT -> rejected

#### Test 3 - Supervisor Recovery
1. Start with Supervisor down (Messages uses fallback)
2. Start Supervisor
3. Messages periodic refresh picks up the JWKS
4. Supervisor-issued JWTs now accepted alongside shared-secret JWTs

#### Test 4 - Token Refresh
1. Login via UI
2. Wait for access token to expire (or set short expiry for testing)
3. Frontend automatically refreshes the token
4. User continues without interruption

---

## File Tree (Final)

```
poc-security/
├── PLAN.md
├── supervisor/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/microdevice/supervisor/
│           │   ├── SupervisorApplication.java
│           │   ├── config/
│           │   ├── controller/
│           │   ├── dto/
│           │   ├── filter/
│           │   ├── model/
│           │   ├── repository/
│           │   └── service/
│           └── resources/
│               └── application.yml
├── messages/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/microdevice/messages/
│           │   ├── MessagesApplication.java
│           │   ├── config/
│           │   ├── controller/
│           │   ├── dto/
│           │   ├── filter/
│           │   ├── model/
│           │   └── service/
│           └── resources/
│               └── application.yml
├── frontend/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── api/
│       │   └── axiosConfig.ts
│       ├── context/
│       │   └── AuthContext.tsx
│       ├── pages/
│       │   ├── LoginPage.tsx
│       │   ├── DashboardPage.tsx
│       │   └── MessagesPage.tsx
│       └── components/
│           ├── ProtectedRoute.tsx
│           └── Navbar.tsx
├── e2e-tests/
│   ├── package.json
│   ├── playwright.config.ts
│   └── tests/
│       ├── login.spec.ts
│       ├── dashboard.spec.ts
│       ├── messages.spec.ts
│       └── logout.spec.ts
└── scripts/
    ├── generate-token.sh
    ├── generate-token.js
    └── package.json
```

---

## Key Design Decisions

1. **RSA (RS256) for Supervisor-issued tokens** - Asymmetric keys allow Messages to validate tokens without knowing the private key. Only the public key is shared via JWKS.

2. **HMAC (HS256) for shared-secret tokens** - Symmetric key is simpler for service-to-service fallback. Both the token generator script and Messages app share the same secret.

3. **Issuer-based routing** - The `iss` claim in the JWT determines which validation strategy to use. This keeps the logic clean and extensible.

4. **Graceful degradation** - Messages app never fails to start due to Supervisor unavailability. It simply operates in degraded mode, accepting only shared-secret tokens.

5. **In-memory storage** - Both apps use in-memory data stores (no database) to keep the POC simple and focused on the security mechanism.

6. **Token refresh** - Only Supervisor-issued tokens support refresh. Shared-secret tokens are meant for service/script use and are generated as needed.

7. **TypeScript throughout frontend** - All frontend code is TypeScript (`.tsx`/`.ts`) for type safety and better developer experience.

8. **Chakra UI** - Pre-built accessible components eliminate the need for custom UI components, keeping focus on the security logic.

9. **data-testid attributes** - Every interactive UI element has a `data-testid` attribute, making Playwright selectors stable and decoupled from styling/structure changes.

10. **Playwright with API login helper** - Tests that need auth as a precondition use API-based login (faster) rather than going through the UI login flow every time.
