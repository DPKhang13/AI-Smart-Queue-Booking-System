# MASTER_CONTEXT.md

# AI Smart Queue & Booking System

## 1. Project Purpose

AI Smart Queue & Booking System is a personal backend-focused portfolio project designed to strengthen the CV for Backend Intern and Java Spring Boot internship positions.

The goal is to create a production-style backend system that demonstrates:

- Backend architecture design
- RESTful API development
- Authentication and authorization
- Queue management logic
- Booking workflow validation
- PostgreSQL database design
- Redis caching
- Analytics APIs
- AI-based prediction features
- Dockerized deployment
- Professional GitHub project structure
- Production-style database and ERD thinking

This project should look closer to a real startup/product backend system rather than a simple university CRUD assignment.

---

## 2. Current Important Updates

The previous context should be updated because it is missing the latest ERD and production database decisions.

Important updates:

- Add `Role`, `ServiceType`, `QueueEvent`, `PredictionLog`, and `AuditLog` entities.
- Use consistent prefix_id naming convention.
- Use `password_hash` instead of `password`.
- Remove `queue_position` from `queue_tickets`.
- Support walk-in queue tickets.
- Allow `queue_tickets.booking_id` to be nullable.
- Allow `queue_tickets.customer_id` to be nullable.
- Add `guest_name` and `guest_phone` to `queue_tickets`.
- Add notification retry fields: `retry_count`, `error_message`, `last_retry_at`.
- Use `is_active`, `is_deleted`, and `deleted_at` for soft deletion.
- Keep ticket number uniqueness by `(branch_id, queue_date, ticket_number)`.
- Generate ticket numbers safely using Redis atomic increment or PostgreSQL row-level locking.
- Add `PredictionLog` with `branch_id` and `service_type_id`.
- Add nullable `AuditLog.performed_by_id` for system actions.

---

## 3. Main Project Idea

The system simulates a smart booking and queue platform for:

- Cafés
- Clinics
- Salons
- Service centers
- EV maintenance stations
- Customer support offices

Registered users can:

- Create bookings
- Receive queue numbers
- Track queue progress
- Get estimated wait times
- Receive AI-based booking recommendations

Walk-in guests can:

- Take a queue ticket directly without registering
- Be identified by optional `guest_name` and `guest_phone`

Admins and staff can:

- Manage queue flow
- View analytics
- Monitor booking performance
- Manage branches
- Manage service types
- Assign staff to queue tickets

---

## 4. Target Tech Stack

### Backend

- Java 25
- Spring Boot
- Spring Security
- JWT
- RESTful APIs
- Spring Data JPA

### Database

- PostgreSQL

### Cache

- Redis

### Documentation and Tools

- Swagger / OpenAPI
- Postman
- GitHub
- Docker
- Docker Compose
- dbdiagram.io

### Optional Advanced Features

- Kafka or RabbitMQ
- GitHub Actions CI/CD
- JUnit and Mockito
- Flyway
- WebSocket / SSE
- Python AI microservice

---

## 5. Main System Modules

### Authentication Module

Features:

- Register
- Login
- JWT token generation
- Role-based authorization
- Password hashing

Roles:

- USER
- STAFF
- ADMIN

### User Module

Features:

- View profile
- Update profile
- Booking history
- Queue history
- Soft deletion support

### Branch Module

Features:

- Create branch
- Update branch
- Branch operating hours
- Branch booking capacity
- Active/inactive branch
- Soft deletion support

### Service Type Module

Features:

- Create service type
- Update service type
- Estimated service duration
- Service type per branch
- Active/inactive service type
- Soft deletion support

### Booking Module

Features:

- Create booking
- Cancel booking
- Booking validation
- Prevent overlapping bookings
- Capacity validation
- Booking status tracking

Booking statuses:

- PENDING
- CONFIRMED
- CANCELLED
- COMPLETED
- NO_SHOW

### Queue Module

Features:

- Queue ticket generation
- Walk-in queue ticket support
- Queue order tracking
- Estimated wait time
- Staff queue processing
- Queue completion tracking
- Queue event history

Queue statuses:

- WAITING
- IN_PROGRESS
- COMPLETED
- CANCELLED
- SKIPPED

Important queue rule:

- Do not store fixed `queue_position`.
- Calculate queue order dynamically using `ORDER BY check_in_time ASC, ticket_id ASC`.

### Notification Module

Features:

- Booking confirmation
- Booking cancellation
- Queue reminder
- Queue updated notification
- Retry failed notifications

Notification statuses:

- PENDING
- SENT
- FAILED

Production retry fields:

- retry_count
- error_message
- last_retry_at

### Analytics Module

Features:

- Peak booking hours
- Cancellation rate
- Average waiting time
- Branch performance
- Queue completion rate
- Daily and weekly booking reports

### AI Prediction Module

Features:

- Estimated waiting time prediction
- Peak-hour prediction
- Less crowded branch recommendation
- Suggested booking time

Initial implementation:

- Rule-based prediction
- Historical data analysis

Future implementation:

- Python AI microservice
- ML prediction models

### Audit Module

Features:

- Track important system actions
- Store old and new values as JSONB
- Allow nullable `performed_by_id` for system-generated actions

---

## 6. Current Feature Priority

### Priority 1 — Must Have

- JWT Authentication
- Role-based access
- Booking creation
- Booking cancellation
- Queue ticket generation
- Walk-in queue ticket support
- Queue tracking
- PostgreSQL schema
- Swagger API docs
- Docker Compose

### Priority 2 — Important Features

- Redis caching
- Booking conflict validation
- Notification processing
- Notification retry mechanism
- Admin analytics
- Branch management
- Service type management
- Queue event history
- Unit testing

### Priority 3 — Wow Factor

- AI prediction
- Recommendation system
- Kafka/RabbitMQ
- WebSocket realtime queue
- CI/CD pipeline
- Cloud deployment
- Python AI microservice

---

## 7. Final Folder Structure

```text
src/main/java/com/khang/smartqueue/
│
├── base
├── config
├── constants
├── controller
├── dto
├── entity
├── exception
├── initializer
├── mapper
├── repository
├── service
├── serviceImpl
├── specification
│
└── SmartQueueApplication.java
```

---

## 8. Entities

Production entities:

- Role
- User
- Branch
- ServiceType
- Booking
- QueueTicket
- QueueEvent
- Notification
- QueuePrediction
- PredictionLog
- AuditLog

---

## 9. Database Naming Convention

Use prefix_id naming style consistently:

- roles.role_id
- users.user_id
- branches.branch_id
- service_types.service_type_id
- bookings.booking_id
- queue_tickets.ticket_id
- queue_events.event_id
- notifications.notification_id
- queue_predictions.prediction_id
- prediction_logs.prediction_log_id
- audit_logs.audit_id

Avoid inconsistent names such as:

- id
- service_id
- nof_id
- predict_id
- pre_log_id

---

## 10. Current ERD Relationships

```text
Role 1 ─── N User

User 1 ─── N Booking
User 1 ─── N Notification
User 1 ─── N AuditLog nullable
User 1 ─── N QueueTicket as customer
User 1 ─── N QueueTicket as assignedStaff

Branch 1 ─── N ServiceType
Branch 1 ─── N Booking
Branch 1 ─── N QueueTicket
Branch 1 ─── N QueuePrediction
Branch 1 ─── N PredictionLog

ServiceType 1 ─── N Booking
ServiceType 1 ─── N QueuePrediction
ServiceType 1 ─── N PredictionLog

Booking 1 ─── 0..1 QueueTicket
Booking 1 ─── N Notification

QueueTicket 1 ─── N QueueEvent
```

Important:

- Booking to QueueTicket is 1 to 0..1 because walk-in tickets can exist without booking.
- `queue_tickets.booking_id` is nullable.
- `queue_tickets.customer_id` is nullable for walk-in guests.
- `queue_tickets.assigned_staff_id` is nullable until assigned.

---

## 11. Current DBML For dbdiagram.io

```dbml
Table roles {
  role_id bigint [pk, increment]
  name varchar(50) [unique, not null]
  description text
  created_at timestamp
}

Table users {
  user_id bigint [pk, increment]
  role_id bigint [not null]
  full_name varchar(150) [not null]
  email varchar(150) [unique, not null]
  phone varchar(30) [unique]
  password_hash varchar(255) [not null]
  is_active boolean [default: true]
  is_deleted boolean [default: false]
  deleted_at timestamp
  created_at timestamp
  updated_at timestamp
}

Table branches {
  branch_id bigint [pk, increment]
  name varchar(150)
  address text
  phone varchar(30)
  opening_time time
  closing_time time
  max_queue_capacity int
  average_service_duration int
  is_active boolean [default: true]
  is_deleted boolean [default: false]
  deleted_at timestamp
  created_at timestamp
  updated_at timestamp
}

Table service_types {
  service_type_id bigint [pk, increment]
  branch_id bigint [not null]
  name varchar(150)
  description text
  estimated_duration_minutes int
  is_active boolean [default: true]
  is_deleted boolean [default: false]
  deleted_at timestamp
  created_at timestamp
  updated_at timestamp
}

Table bookings {
  booking_id bigint [pk, increment]
  user_id bigint [not null]
  branch_id bigint [not null]
  service_type_id bigint [not null]
  booking_code varchar(50) [unique]
  booking_date date
  booking_time time
  status varchar(30)
  note text
  cancelled_at timestamp
  cancellation_reason text
  created_at timestamp
  updated_at timestamp

  indexes {
    (user_id, branch_id, booking_date, booking_time) [unique]
  }
}

Table queue_tickets {
  ticket_id bigint [pk, increment]
  booking_id bigint
  branch_id bigint [not null]
  customer_id bigint
  assigned_staff_id bigint
  guest_name varchar(150)
  guest_phone varchar(30)
  counter_name varchar(50)
  ticket_number varchar(30)
  queue_date date
  status varchar(30)
  check_in_time timestamp
  start_service_time timestamp
  completed_time timestamp
  estimated_wait_minutes int
  actual_wait_minutes int
  created_at timestamp
  updated_at timestamp

  indexes {
    (branch_id, queue_date, ticket_number) [unique]
  }
}

Table queue_events {
  event_id bigint [pk, increment]
  queue_ticket_id bigint [not null]
  performed_by_id bigint
  old_status varchar(30)
  new_status varchar(30)
  event_type varchar(50)
  note text
  created_at timestamp
}

Table notifications {
  notification_id bigint [pk, increment]
  user_id bigint [not null]
  booking_id bigint
  type varchar(50)
  status varchar(30)
  title varchar(200)
  message text
  recipient_target varchar(150)
  retry_count int [default: 0]
  error_message text
  last_retry_at timestamp
  sent_at timestamp
  created_at timestamp
}

Table queue_predictions {
  prediction_id bigint [pk, increment]
  branch_id bigint [not null]
  service_type_id bigint [not null]
  prediction_date date
  prediction_time time
  predicted_wait_minutes int
  predicted_queue_length int
  confidence_score decimal(5,2)
  method varchar(50)
  created_at timestamp
}

Table prediction_logs {
  prediction_log_id bigint [pk, increment]
  branch_id bigint [not null]
  service_type_id bigint [not null]
  input_data jsonb
  output_data jsonb
  confidence_score decimal(5,2)
  model_version varchar(50)
  created_at timestamp
}

Table audit_logs {
  audit_id bigint [pk, increment]
  performed_by_id bigint
  action varchar(100)
  entity_name varchar(100)
  entity_id bigint
  old_value jsonb
  new_value jsonb
  created_at timestamp
}

Ref: users.role_id > roles.role_id
Ref: bookings.user_id > users.user_id
Ref: bookings.branch_id > branches.branch_id
Ref: bookings.service_type_id > service_types.service_type_id
Ref: service_types.branch_id > branches.branch_id
Ref: queue_tickets.booking_id > bookings.booking_id
Ref: queue_tickets.branch_id > branches.branch_id
Ref: queue_tickets.customer_id > users.user_id
Ref: queue_tickets.assigned_staff_id > users.user_id
Ref: queue_events.queue_ticket_id > queue_tickets.ticket_id
Ref: queue_events.performed_by_id > users.user_id
Ref: notifications.user_id > users.user_id
Ref: notifications.booking_id > bookings.booking_id
Ref: queue_predictions.branch_id > branches.branch_id
Ref: queue_predictions.service_type_id > service_types.service_type_id
Ref: prediction_logs.branch_id > branches.branch_id
Ref: prediction_logs.service_type_id > service_types.service_type_id
Ref: audit_logs.performed_by_id > users.user_id
```

---

## 12. Planned API Groups

### Auth APIs

```http
POST /api/auth/register
POST /api/auth/login
```

### User APIs

```http
GET /api/users/profile
PUT /api/users/profile
```

### Branch APIs

```http
POST /api/branches
GET /api/branches
GET /api/branches/{id}
PUT /api/branches/{id}
DELETE /api/branches/{id}
```

### Service Type APIs

```http
POST /api/service-types
GET /api/service-types
GET /api/service-types/{id}
PUT /api/service-types/{id}
DELETE /api/service-types/{id}
```

### Booking APIs

```http
POST /api/bookings
GET /api/bookings/my-bookings
GET /api/bookings/{id}
PUT /api/bookings/{id}/cancel
```

### Queue APIs

```http
POST /api/queues/walk-in
GET /api/queues/branch/{branchId}
PUT /api/queues/{ticketId}/assign-staff
PUT /api/queues/{ticketId}/start
PUT /api/queues/{ticketId}/complete
PUT /api/queues/{ticketId}/skip
GET /api/queues/{ticketId}/position
```

### Analytics APIs

```http
GET /api/admin/analytics/overview
GET /api/admin/analytics/peak-hours
GET /api/admin/analytics/branch-performance
```

### AI Prediction APIs

```http
GET /api/predictions/wait-time
GET /api/predictions/recommend-branch
GET /api/predictions/recommend-time
```

---

## 13. GitHub Repository Structure

```text
smart-queue-system/
│
├── docs/
│   ├── architecture/
│   ├── erd/
│   ├── api-flow/
│   ├── planning/
│   │   ├── MASTER_CONTEXT.md
│   │   ├── ROADMAP.md
│   │   ├── FEATURE_PRIORITY.md
│   │   └── SYSTEM_NOTES.md
│   └── screenshots/
│
├── postman/
├── src/
├── docker-compose.yml
├── README.md
└── pom.xml
```

---

## 14. Future AI Assistant Instructions

When helping with this project, always:

- Read this MASTER_CONTEXT.md first.
- Follow the final folder structure.
- Use Java Spring Boot backend style.
- Use PostgreSQL and Redis.
- Use JWT authentication.
- Use prefix_id database naming convention.
- Avoid queue_position in the database.
- Support walk-in queue tickets.
- Use password_hash, never plaintext password.
- Include notification retry logic.
- Include auditability where useful.
- Prioritize backend quality over frontend UI.
