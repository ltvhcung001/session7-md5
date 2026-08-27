# QuickBite Technical Implementation Plan

**Plan Version:** 1.0.0  
**Status:** In Progress / Baseline Executed  
**Date:** 2026-08-27  

---

## 1. Architectural Milestones & Phases

```mermaid
gantt
    title QuickBite Implementation Roadmap
    dateFormat  YYYY-MM-DD
    section Phase 1: Core Foundation
    Multi-module Maven Setup           :done,    p1_1, 2026-08-27, 1d
    Common Library (DTOs, Events, Exceptions) :done, p1_2, 2026-08-27, 1d
    Eureka Discovery Server Setup      :done,    p1_3, 2026-08-27, 1d
    API Gateway & JWT Filter Setup     :done,    p1_4, 2026-08-27, 1d
    section Phase 2: Domain Services
    Auth & User Service Implementation :done,    p2_1, 2026-08-27, 1d
    Restaurant & Menu Catalog Service  :done,    p2_2, 2026-08-27, 1d
    Order Processing & Feign Client    :done,    p2_3, 2026-08-27, 1d
    Payment Service & Ledger           :done,    p2_4, 2026-08-27, 1d
    section Phase 3: Logistics & Events
    Kafka Event Choreography           :done,    p3_1, 2026-08-27, 1d
    Delivery Service & Driver Matching :done,    p3_2, 2026-08-27, 1d
    Notification Service & Consumers   :done,    p3_3, 2026-08-27, 1d
    section Phase 4: Containerization & Docs
    Docker Compose Orchestration       :done,    p4_1, 2026-08-27, 1d
    OpenAPI Documentation & Verification:done,   p4_2, 2026-08-27, 1d
```

---

## 2. Microservice Dependency Matrix

| Service | Upstream Dependencies (Sync / Feign) | Event Subscriptions (Kafka) | Event Publications (Kafka) |
| :--- | :--- | :--- | :--- |
| **`discovery-service`** | None | None | None |
| **`api-gateway`** | Discovery (`discovery-service`) | None | None |
| **`auth-user-service`** | Discovery | None | None |
| **`restaurant-service`**| Discovery | None | None |
| **`order-service`** | `restaurant-service`, `auth-user-service` | `payment-processed-topic`, `delivery-status-topic` | `order-placed-topic`, `order-confirmed-topic`, `order-prepared-topic` |
| **`payment-service`** | Discovery | `order-placed-topic` | `payment-processed-topic` |
| **`delivery-service`** | Discovery | `order-prepared-topic` | `delivery-status-topic` |
| **`notification-service`**| Discovery | `order-placed-topic`, `payment-processed-topic`, `order-confirmed-topic`, `delivery-status-topic` | None |

---

## 3. Database Initialization Strategy

- **Database-per-service pattern** is implemented on PostgreSQL 16.
- A centralized SQL script `docker/postgres/init-databases.sql` automatically provisions separate catalogs on container startup:
  1. `quickbite_user_db`
  2. `quickbite_restaurant_db`
  3. `quickbite_order_db`
  4. `quickbite_payment_db`
  5. `quickbite_delivery_db`
  6. `quickbite_notification_db`
- Hibernate `ddl-auto: update` manages schema evolution per microservice seamlessly in development.

---

## 4. Container Orchestration & Networking

- **Network:** `quickbite-network` (bridge network enabling DNS-based service discovery).
- **Service Dependency Ordering:**
  - `postgres` (healthcheck ensures database readiness before services start).
  - `zookeeper` -> `kafka`.
  - `discovery-service` starts before all dependent business microservices.
  - `api-gateway` binds to port 8080 as the external frontend proxy.

---

## 5. Verification and Quality Assurance Plan

1. **Compilation Validation:** Run `mvn clean test-compile` across the entire multi-module reactor.
2. **Container Build Validation:** Build Docker images for each service using multi-stage builds.
3. **End-to-End Test Suite:**
   - User Registration & JWT Token generation.
   - Restaurant creation & Menu item cataloging.
   - Order placement with Feign pricing validation.
   - Kafka event trigger from `order-service` -> `payment-service` -> `order-service` state progression.
   - Order preparation -> `delivery-service` driver assignment.
   - Verification of `notification-service` inbox logs.
