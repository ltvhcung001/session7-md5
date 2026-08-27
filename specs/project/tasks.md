# QuickBite Implementation Tasks (SpecKit)

**Specification Reference:** [`specs/project/spec.md`](file:///home/cungh/Documents/rikkei/md5/ss7/specs/project/spec.md)  
**Implementation Plan:** [`specs/project/plan.md`](file:///home/cungh/Documents/rikkei/md5/ss7/specs/project/plan.md)  
**Constitution:** [`specify/memory/constitution.md`](file:///home/cungh/Documents/rikkei/md5/ss7/specify/memory/constitution.md)  

---

## 📋 Task Matrix

### Phase 1: Foundation & Shared Infrastructure

- [x] **`TASK-001` - Root Multi-Module Maven Setup**
  - **Description:** Initialize parent `pom.xml` managing Spring Boot 3.3.2, Spring Cloud 2023.0.3, Lombok, JJWT, and SpringDoc dependencies.
  - **Acceptance Criteria:** `mvn validate` runs successfully on root project.
  - **Status:** `COMPLETED`

- [x] **`TASK-002` - Common Library Module**
  - **Description:** Implement `common-library` with shared DTOs (`ApiResponse`, `ErrorResponse`, `PageResponse`), domain Enums, Kafka Event payloads, Global Exception Handler, and `JwtUtil`.
  - **Acceptance Criteria:** `common-library` compiles into a reusable JAR.
  - **Status:** `COMPLETED`

- [x] **`TASK-003` - Netflix Eureka Discovery Service**
  - **Description:** Create `discovery-service` with `@EnableEurekaServer` running on port 8761 with actuator endpoints.
  - **Acceptance Criteria:** Eureka dashboard renders at `http://localhost:8761`.
  - **Status:** `COMPLETED`

- [x] **`TASK-004` - API Gateway & JWT Security Filter**
  - **Description:** Implement Spring Cloud Gateway on port 8080 with dynamic Eureka routing, `AuthenticationFilter` for JWT token verification, claim extraction (`X-User-Id`), and global CORS filter.
  - **Acceptance Criteria:** Gateway intercepts unsecured endpoints, validates Bearer tokens, and routes traffic.
  - **Status:** `COMPLETED`

---

### Phase 2: Core Domain Services

- [x] **`TASK-005` - Auth & User Service (`auth-user-service`)**
  - **Description:** Create user registration, login, refresh token, user profile, and delivery address management with PostgreSQL datastore (`quickbite_user_db`) on port 8081.
  - **Acceptance Criteria:** Registration returns BCrypt-hashed user; login issues valid JWTs; address book CRUD functional.
  - **Status:** `COMPLETED`

- [x] **`TASK-006` - Restaurant Catalog Service (`restaurant-service`)**
  - **Description:** Implement restaurant profiles, categories, and menu items on port 8082 with search and filtering capabilities.
  - **Acceptance Criteria:** Owner can create restaurants, add categories & dishes; customers can search restaurants and query menus.
  - **Status:** `COMPLETED`

- [x] **`TASK-007` - Order Processing Service (`order-service`)**
  - **Description:** Implement order placement, item calculation, OpenFeign integration with `restaurant-service`, Kafka event producer (`OrderPlacedEvent`), and order lifecycle management on port 8083.
  - **Acceptance Criteria:** Validates menu prices synchronously; persists order in `PENDING_PAYMENT`; emits Kafka events.
  - **Status:** `COMPLETED`

---

### Phase 3: Event-Driven Ecosystem & Logistics

- [x] **`TASK-008` - Payment Service & Kafka Consumer (`payment-service`)**
  - **Description:** Implement transaction ledger on port 8084 with Kafka listener for `order-placed-topic` and producer for `payment-processed-topic`.
  - **Acceptance Criteria:** Consumes order event; generates transaction ID; emits payment result event.
  - **Status:** `COMPLETED`

- [x] **`TASK-009` - Delivery & Driver Tracking Service (`delivery-service`)**
  - **Description:** Implement driver matching, GPS coordinate tracking, and status transitions (`PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`) on port 8086.
  - **Acceptance Criteria:** Consumes `order-prepared-topic`; auto-assigns driver; emits status updates to Kafka.
  - **Status:** `COMPLETED`

- [x] **`TASK-010` - Multi-Channel Notification Service (`notification-service`)**
  - **Description:** Implement event consumer for all Kafka topics (`order-placed-topic`, `payment-processed-topic`, `order-confirmed-topic`, `delivery-status-topic`) on port 8085.
  - **Acceptance Criteria:** Logs alerts for SMS, Email, and Push channels; saves notifications to user inbox.
  - **Status:** `COMPLETED`

---

### Phase 4: Containerization, Testing & Documentation

- [x] **`TASK-011` - Docker Compose & Multi-Database Orchestration**
  - **Description:** Create `docker-compose.yml`, multi-stage `Dockerfile` per microservice, and PostgreSQL database initialization script `init-databases.sql`.
  - **Acceptance Criteria:** `docker compose up --build` launches PostgreSQL, Kafka, Zookeeper, Eureka, Gateway, and all 6 microservices in one command.
  - **Status:** `COMPLETED`

- [x] **`TASK-012` - Compilation Verification & End-to-End Documentation**
  - **Description:** Verify complete multi-module compilation (`mvn clean test-compile`) and provide API testing walkthrough with example JSON payloads in `README.md`.
  - **Acceptance Criteria:** 10/10 modules build with `BUILD SUCCESS`.
  - **Status:** `COMPLETED`

---

### Phase 5: Future Enhancements (Backlog)

- [ ] **`TASK-013` - Redis Distributed Caching & Rate Limiting**
  - **Description:** Introduce Redis for caching active restaurant menus and implementing sliding-window rate limiting on API Gateway.
  - **Dependencies:** `TASK-004`, `TASK-006`
  - **Status:** `BACKLOG`

- [ ] **`TASK-014` - WebSocket Real-Time Driver Tracking**
  - **Description:** Implement Spring WebSocket / STOMP endpoint in `delivery-service` to broadcast live driver GPS coordinates to frontend clients.
  - **Dependencies:** `TASK-009`
  - **Status:** `BACKLOG`

- [ ] **`TASK-015` - VNPay / MoMo Sandbox Payment Gateway Integration**
  - **Description:** Integrate real payment webhooks and cryptographic signature verification for VNPay and MoMo e-wallets in `payment-service`.
  - **Dependencies:** `TASK-008`
  - **Status:** `BACKLOG`
