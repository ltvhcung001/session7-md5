# QuickBite Engineering Constitution

**Document Version:** 1.0.0  
**Project:** QuickBite Microservices Food Ordering Platform  
**Target Runtime:** Java 17+, Spring Boot 3.3+, Spring Cloud 2023.0+, Apache Kafka, PostgreSQL, Docker  

---

## 1. Core Architectural Invariants

### 1.1 Microservices Boundary & Autonomy
- **Database-per-Service:** Each microservice possesses exclusive ownership of its datastore (`quickbite_user_db`, `quickbite_restaurant_db`, `quickbite_order_db`, etc.). Direct cross-service database access is strictly prohibited.
- **API Gateway as Single Entrypoint:** External client requests must transit through `api-gateway` on port `8080`. Internal services are routed dynamically via Eureka service discovery (`lb://service-name`).
- **Stateless Services:** All application services must remain stateless to allow horizontal scaling behind the gateway load balancer. Session state must reside in client JWT tokens or distributed stores (Redis).

### 1.2 Communication Protocol Standards
- **Synchronous Communication (Queries):** Use declarative Spring Cloud OpenFeign clients for point-to-point queries where real-time retrieval is strictly required (e.g., pricing validation before order creation).
- **Asynchronous Communication (Events & State Transitions):** All mutating side-effects (Order placed, Payment processed, Driver assigned, Notification dispatched) MUST be published as strongly-typed events to Apache Kafka topics.
- **Saga Choreography:** Distributed transactions must adhere to event-driven choreography. Every service consuming a transition event must handle idempotency and publish corresponding success/failure events.

---

## 2. API Design & Payload Standards

### 2.1 Uniform Response Envelope
All REST API responses MUST be wrapped in the standard `ApiResponse<T>` envelope:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-08-27T08:30:00"
}
```

### 2.2 Uniform Error Representation
Errors must yield standardized HTTP status codes and map to `ErrorResponse`:
```json
{
  "success": false,
  "status": 404,
  "error": "Not Found",
  "message": "Restaurant not found with id: '99'",
  "path": "/api/v1/restaurants/99",
  "details": [],
  "timestamp": "2026-08-27T08:30:00"
}
```

### 2.3 Semantic URL Naming
- Resource collections: Plural nouns (`/api/v1/restaurants`, `/api/v1/orders`).
- Sub-resources: `/api/v1/restaurants/{id}/categories`, `/api/v1/users/me/addresses`.
- Actions on resources: Use standard HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).

---

## 3. Security & Authentication Rules

### 3.1 Token Validation & Downstream Context Propagation
- Client authenticates via `auth-user-service` and receives HMAC-SHA256 JWT access & refresh tokens.
- `api-gateway` validates JWT tokens at the boundary, extracts claims (`userId`, `email`, `role`), and populates downstream HTTP headers:
  - `X-User-Id`
  - `X-User-Email`
  - `X-User-Role`
- Microservices consume these headers to enforce authorization without re-parsing raw signatures.

### 3.2 Role-Based Access Control (RBAC)
Supported security roles:
- `ROLE_CUSTOMER`: Browsing, ordering, tracking, user profile management.
- `ROLE_RESTAURANT_OWNER`: Restaurant creation, menu maintenance, order preparation status updates.
- `ROLE_DRIVER`: Claiming delivery batches, updating real-time geo-coordinates, delivery status lifecycle.
- `ROLE_ADMIN`: Platform governance, restaurant approval, audit logs.

---

## 4. Event Schema & Kafka Topic Governance

- Event definitions MUST reside in `common-library/src/main/java/com/quickbite/common/event/`.
- Every event payload MUST include:
  - `eventId` (UUID)
  - `timestamp` (ISO-8601 LocalDateTime)
  - Correlation identifier (e.g. `orderId`, `customerId`)
- Standard Topics:
  - `order-placed-topic`
  - `payment-processed-topic`
  - `order-confirmed-topic`
  - `order-prepared-topic`
  - `delivery-status-topic`

---

## 5. Code Quality & Engineering Conventions

### 5.1 Project Layout & Module Responsibility
- `common-library`: Cross-cutting DTOs, Enums, Kafka Event payloads, Exceptions, Security Utilities.
- `discovery-service`: Eureka Server only.
- `api-gateway`: Route configuration, CORS, JWT verification filter.
- Business microservices: Structured strictly into `entity`, `dto`, `repository`, `service`, `controller`, and `kafka` packages.

### 5.2 Clean Code & Lombok Conventions
- Entities use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Relationships mapped with `FetchType.LAZY` and `@JsonIgnore` on parent back-references to avoid circular serialization.
- Services use constructor injection via Lombok `@RequiredArgsConstructor`.
- Input validation enforced at controller boundary via `@Valid` and Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@DecimalMin`).

---

## 6. Definition of Done (DoD)

A task or feature is considered complete only when:
1. All domain entities, DTOs, and controllers match the specification contract.
2. Inter-service events (if any) are properly published and consumed with Kafka serialization.
3. Code compiles with zero errors under `mvn clean test-compile`.
4. Multi-stage `Dockerfile` and `docker-compose.yml` entries are functional.
5. OpenAPI / Swagger endpoints are accessible and documented.
