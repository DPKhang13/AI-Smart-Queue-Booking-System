# AI Smart Queue & Booking System — MASTER CONTEXT

## 1. Project Overview

AI Smart Queue & Booking System is a backend-focused portfolio project built to strengthen Backend Intern / Java Spring Boot Intern applications.

The system simulates a production-style smart booking and queue management platform for service-based businesses such as:

* Cafés
* Clinics
* Salons
* Service centers
* EV maintenance stations
* Customer support offices

The system supports both registered users and walk-in guests. Registered users can create bookings, receive queue tickets, track queue progress, and receive notifications. Walk-in guests can take queue tickets directly without creating an account.

This project is designed to demonstrate:

* Java Spring Boot backend architecture
* RESTful API design
* JWT authentication
* Role-based authorization
* Booking workflow validation
* Queue management logic
* Walk-in queue ticket support
* Staff shift and counter assignment
* Notification retry logic
* PostgreSQL schema design
* Flyway migration
* Redis-ready queue optimization
* AI/analytics-ready database structure
* Clean JPA entity mapping
* Dockerized backend setup
* Swagger/OpenAPI documentation
* Professional GitHub documentation

The project should look like a junior backend engineer portfolio project, not a simple CRUD assignment.

---

## 2. Current Tech Stack

### Backend

* Java 25
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* RESTful APIs
* Bean Validation

### Database

* PostgreSQL
* Flyway Migration
* JSONB support

### Cache / Future Optimization

* Redis

### Documentation / Tools

* Swagger / OpenAPI
* Postman
* dbdiagram.io
* GitHub

### DevOps

* Docker
* Docker Compose

### Testing

* JUnit
* Mockito
* Testcontainers optional

---

## 3. Current MVP Scope

### Included In Current MVP

The current MVP includes:

* Auth / Role / User
* Branch management
* Branch schedule
* Branch holiday
* Service type
* Service capacity slot
* Booking
* Queue ticket
* Walk-in queue support
* Counter
* Staff shift
* Counter assignment
* Queue event history
* Notification retry
* Queue prediction
* Prediction log
* No-show prediction
* Customer feedback
* Audit log

### Excluded From Current MVP

Do not build these yet:

* Payment
* Queue display screen
* Multi-tenant system
* Subscription / pricing system
* Complex frontend UI

These can be added in later phases only.

---

## 4. Main Business Flow

### Registered User Booking Flow

```text
User registers / logs in
        ↓
User selects branch
        ↓
User selects service type
        ↓
User creates booking
        ↓
System validates branch schedule, holiday, capacity slot, and duplicate active booking
        ↓
Booking is created with status PENDING or CONFIRMED
        ↓
Queue ticket can be generated from booking
        ↓
User tracks queue progress
        ↓
Staff starts service
        ↓
Queue ticket becomes IN_PROGRESS
        ↓
Staff completes / skips / cancels ticket
        ↓
Queue event is recorded
        ↓
Notification is created / sent
        ↓
Prediction, feedback, and analytics data are updated
```

### Walk-in Guest Flow

```text
Guest arrives at branch
        ↓
Staff or kiosk creates walk-in queue ticket
        ↓
Queue ticket has no booking_id
        ↓
Queue ticket may have no customer_id
        ↓
Guest is identified by guest_name or guest_phone
        ↓
Ticket enters WAITING queue
        ↓
Staff assigns counter / staff
        ↓
Service starts
        ↓
Service completes
        ↓
Queue event is recorded
```

### Staff Queue Flow

```text
Staff has shift
        ↓
Staff is assigned to branch
        ↓
Staff is assigned to counter
        ↓
Staff views waiting queue tickets
        ↓
Staff starts ticket
        ↓
Ticket status becomes IN_PROGRESS
        ↓
Staff completes / skips / cancels ticket
        ↓
QueueEvent records the state transition
```

---

## 5. Roles And Permissions

### USER

Can:

* Register
* Login
* View profile
* Update own profile
* Create booking
* Cancel own booking
* View own bookings
* View own queue tickets
* Submit feedback

### STAFF

Can:

* View assigned branch queue
* Create walk-in ticket
* Start queue ticket
* Complete queue ticket
* Skip queue ticket
* Cancel queue ticket
* Assign counter if allowed
* View queue event history

### ADMIN

Can:

* Manage users
* Manage branches
* Manage schedules
* Manage holidays
* Manage service types
* Manage capacity slots
* Manage counters
* Manage staff shifts
* View analytics
* View audit logs

---

## 6. Current Core Modules

### Auth Module

Responsibilities:

* Register
* Login
* Password hashing
* JWT token generation
* JWT validation
* Role-based authorization

Entities involved:

* User
* Role

---

### User Module

Responsibilities:

* View profile
* Update profile
* Soft delete user
* View booking history
* View queue history

Important rule:

* User uses soft delete.
* Password field must be `password_hash`, not `password`.

---

### Branch Module

Responsibilities:

* Create branch
* Update branch
* Soft delete branch
* Manage default operating hours
* Manage default queue capacity

Branch uses:

* `default_opening_time`
* `default_closing_time`
* `max_queue_capacity`
* `average_service_duration`

---

### Branch Schedule Module

Responsibilities:

* Manage weekly schedule per branch
* Support closed days
* Support opening / closing time per weekday

Important rule:

```text
If is_closed = true:
    opening_time and closing_time may be null

If is_closed = false:
    opening_time and closing_time must exist
```

---

### Branch Holiday Module

Responsibilities:

* Manage special holidays
* Override normal branch schedule
* Support full-day closing
* Support special opening hours

---

### Service Type Module

Responsibilities:

* Manage service types per branch
* Store estimated service duration
* Support active / deleted service type

Examples:

* Haircut
* Consultation
* EV battery check
* Coffee pickup
* Customer support

---

### Service Capacity Slot Module

Responsibilities:

* Define booking capacity by time slot
* Support recurring weekly slot
* Support specific date slot
* Support max bookings
* Support max walk-in queue tickets

Important rule:

```text
Either day_of_week or specific_date must be set, not both.
```

---

### Booking Module

Responsibilities:

* Create booking
* Cancel booking
* Confirm booking
* Complete booking
* Mark no-show booking
* Track booking status
* Prevent duplicate active booking
* Archive old bookings

Booking statuses:

```text
PENDING
CONFIRMED
CANCELLED
COMPLETED
NO_SHOW
```

Important design:

* Do not use soft delete for bookings.
* Use `archived_at` for old transactional booking records.
* Booking data must remain available for analytics and AI training.

Important DB rule:

```sql
CREATE UNIQUE INDEX uq_active_booking_slot
    ON bookings(user_id, branch_id, booking_date, booking_time)
    WHERE status IN ('PENDING', 'CONFIRMED');
```

Reason:

* Prevent duplicate active booking.
* Allow user to book the same slot again after cancellation.

---

### Counter Module

Responsibilities:

* Manage counters per branch

Examples:

* Counter 1
* Counter 2
* Consultation Desk A

---

### Staff Shift Module

Responsibilities:

* Store staff working schedule
* Link staff to branch
* Track shift date and shift time
* Track shift status

Shift statuses:

```text
SCHEDULED
ACTIVE
COMPLETED
CANCELLED
```

---

### Counter Assignment Module

Responsibilities:

* Assign staff to counter
* Link counter assignment to staff shift
* Prevent one counter from having multiple active staff at the same time

Important DB rule:

```sql
CREATE UNIQUE INDEX uq_active_counter_assignment
    ON counter_assignments(counter_id)
    WHERE unassigned_at IS NULL;
```

---

### Queue Ticket Module

Responsibilities:

* Generate queue ticket
* Support booking-based ticket
* Support walk-in ticket
* Assign customer or guest info
* Assign staff
* Assign counter
* Track queue status
* Track service timestamps
* Use optimistic locking

Queue statuses:

```text
WAITING
IN_PROGRESS
COMPLETED
CANCELLED
SKIPPED
```

Important design:

* Do not store fixed `queue_position`.
* Queue order must be calculated dynamically.

Queue order:

```sql
ORDER BY check_in_time ASC NULLS LAST, ticket_id ASC
```

Reason:

* Avoid mass update when one ticket is cancelled or skipped.
* Avoid database lock problems.
* Reduce race condition risk.

Walk-in rule:

```text
queue_tickets.booking_id may be null
queue_tickets.customer_id may be null
guest_name or guest_phone must exist for walk-in tickets
```

---

### Queue Event Module

Responsibilities:

* Record queue status history
* Track who performed the action
* Support auditability
* Support analytics

Event types:

```text
TICKET_CREATED
SERVICE_STARTED
SERVICE_COMPLETED
TICKET_CANCELLED
TICKET_SKIPPED
STAFF_ASSIGNED
COUNTER_ASSIGNED
TICKET_UPDATED
```

---

### Notification Module

Responsibilities:

* Store notifications
* Support booking notifications
* Support queue notifications
* Support registered users
* Support walk-in guests
* Support retry logic

Notification types:

```text
BOOKING_CONFIRMATION
BOOKING_CANCELLED
QUEUE_REMINDER
QUEUE_UPDATED
NO_SHOW_WARNING
```

Notification statuses:

```text
PENDING
SENT
FAILED
```

Retry fields:

```text
retry_count
error_message
last_retry_at
```

Important design:

* `user_id` can be nullable because walk-in guests may not have accounts.
* `recipient_target` must be required.

---

### Queue Prediction Module

Responsibilities:

* Store predicted wait time
* Store predicted queue length
* Support different prediction methods

Prediction methods:

```text
RULE_BASED
HISTORICAL_AVERAGE
AI_MODEL
```

Important rule:

```text
predicted_wait_minutes should be between 0 and 1440.
```

---

### Prediction Log Module

Responsibilities:

* Store AI or rule-based prediction input
* Store prediction output
* Store confidence score
* Store model version
* Use jsonb for input_data and output_data

---

### No-show Prediction Module

Responsibilities:

* Predict whether a booking may become no-show
* Store probability
* Store risk level
* Store model input/output

Risk levels:

```text
LOW
MEDIUM
HIGH
```

---

### Customer Feedback Module

Responsibilities:

* Store rating
* Store comment
* Link feedback to user
* Link feedback to booking or queue ticket
* Link feedback to branch and service type
* Support analytics and AI training

Rating rule:

```text
rating must be between 1 and 5
```

---

### Audit Log Module

Responsibilities:

* Track important system actions
* Support nullable performed_by_id for system-generated actions
* Store old_value and new_value as jsonb

Examples:

```text
ADMIN_CREATE_BRANCH
STAFF_COMPLETE_QUEUE
USER_CANCEL_BOOKING
SYSTEM_GENERATE_PREDICTION
```

---

## 7. Current Database Tables

Current schema includes:

```text
roles
users

branches
branch_schedules
branch_holidays
service_types
service_capacity_slots

bookings

counters
staff_shifts
counter_assignments

queue_tickets
queue_events

notifications

queue_predictions
prediction_logs
no_show_predictions

customer_feedbacks

audit_logs
```

Excluded from current scope:

```text
payments
queue_displays
```

---

## 8. Database Design Rules

### Naming Convention

Use prefix_id naming style:

```text
roles.role_id
users.user_id
branches.branch_id
branch_schedules.schedule_id
branch_holidays.holiday_id
service_types.service_type_id
service_capacity_slots.capacity_slot_id
bookings.booking_id
counters.counter_id
staff_shifts.shift_id
counter_assignments.assignment_id
queue_tickets.ticket_id
queue_events.event_id
notifications.notification_id
queue_predictions.prediction_id
prediction_logs.prediction_log_id
no_show_predictions.no_show_prediction_id
customer_feedbacks.feedback_id
audit_logs.audit_id
```

Avoid inconsistent names:

```text
id
service_id
nof_id
predict_id
pre_log_id
```

---

### Password Rule

Use:

```text
password_hash
```

Do not use:

```text
password
```

Reason:

* Stored password must be hashed, not plaintext.

---

### Timezone Rule

Use `timestamptz` for real event timestamps:

```text
created_at
updated_at
deleted_at
archived_at
cancelled_at
check_in_time
start_service_time
completed_time
sent_at
last_retry_at
assigned_at
unassigned_at
```

Use `date` and `time` for business date/time:

```text
booking_date
booking_time
opening_time
closing_time
prediction_date
prediction_time
shift_date
start_time
end_time
```

---

### Soft Delete Rule

Use soft delete only for master data:

```text
users
branches
service_types
```

Soft delete fields:

```text
is_active
is_deleted
deleted_at
```

Do not soft delete transactional tables:

```text
bookings
queue_tickets
queue_events
notifications
prediction_logs
audit_logs
```

Bookings use:

```text
archived_at
```

---

### Optimistic Locking Rule

Use `version` field for:

```text
bookings
queue_tickets
```

Reason:

* Prevent race conditions when two staff or background jobs update the same booking or ticket.

In JPA:

```java
@Version
private Integer version;
```

---

### JSONB Rule

Use `jsonb`, not `json`, for:

```text
prediction_logs.input_data
prediction_logs.output_data
no_show_predictions.input_data
no_show_predictions.output_data
audit_logs.old_value
audit_logs.new_value
```

---

### Source Of Truth Rule

Flyway SQL is the database source of truth.

Entity annotations should map to the database, but advanced constraints should be defined in Flyway.

Flyway should contain:

* Tables
* Foreign keys
* Indexes
* Partial unique indexes
* CHECK constraints
* jsonb columns
* Optimistic locking columns

DBML is only for ERD visualization.

---

## 9. JPA Entity Clean Code Rules

Use:

```text
BaseEntity
SoftDeleteEntity
```

### BaseEntity

Should contain:

```text
createdAt
updatedAt
@PrePersist
@PreUpdate
```

### SoftDeleteEntity

Should extend BaseEntity and contain:

```text
isActive
isDeleted
deletedAt
```

Only these entities should extend SoftDeleteEntity:

```text
User
Branch
ServiceType
```

Other transactional entities should extend BaseEntity only.

---

## 10. Relationship Design Rules

Use:

```text
@ManyToOne(fetch = FetchType.LAZY)
@OneToOne(fetch = FetchType.LAZY)
```

Avoid unnecessary bidirectional `@OneToMany` collections.

Reason:

* Avoid JSON recursion
* Avoid heavy entity graph
* Avoid accidental N+1 query
* Keep entities maintainable

Recommended:

* Keep important direct relationships.
* Use repository queries instead of loading huge reverse collections.

---

## 11. Planned API Groups

### Auth APIs

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token
```

### User APIs

```text
GET /api/users/me
PUT /api/users/me
GET /api/users/{userId}
GET /api/users
PATCH /api/users/{userId}/status
```

### Branch APIs

```text
POST /api/branches
GET /api/branches
GET /api/branches/{branchId}
PUT /api/branches/{branchId}
DELETE /api/branches/{branchId}
```

### Branch Schedule APIs

```text
POST /api/branches/{branchId}/schedules
GET /api/branches/{branchId}/schedules
PUT /api/branch-schedules/{scheduleId}
DELETE /api/branch-schedules/{scheduleId}
```

### Branch Holiday APIs

```text
POST /api/branches/{branchId}/holidays
GET /api/branches/{branchId}/holidays
PUT /api/branch-holidays/{holidayId}
DELETE /api/branch-holidays/{holidayId}
```

### Service Type APIs

```text
POST /api/branches/{branchId}/service-types
GET /api/branches/{branchId}/service-types
GET /api/service-types/{serviceTypeId}
PUT /api/service-types/{serviceTypeId}
DELETE /api/service-types/{serviceTypeId}
```

### Service Capacity Slot APIs

```text
POST /api/service-capacity-slots
GET /api/service-capacity-slots
GET /api/service-capacity-slots/{capacitySlotId}
PUT /api/service-capacity-slots/{capacitySlotId}
DELETE /api/service-capacity-slots/{capacitySlotId}
```

### Booking APIs

```text
POST /api/bookings
GET /api/bookings/my
GET /api/bookings/{bookingId}
PATCH /api/bookings/{bookingId}/cancel
PATCH /api/bookings/{bookingId}/confirm
PATCH /api/bookings/{bookingId}/complete
PATCH /api/bookings/{bookingId}/no-show
```

### Counter APIs

```text
POST /api/branches/{branchId}/counters
GET /api/branches/{branchId}/counters
GET /api/counters/{counterId}
PUT /api/counters/{counterId}
PATCH /api/counters/{counterId}/status
```

### Staff Shift APIs

```text
POST /api/staff-shifts
GET /api/staff-shifts
GET /api/staff-shifts/{shiftId}
PUT /api/staff-shifts/{shiftId}
PATCH /api/staff-shifts/{shiftId}/status
```

### Counter Assignment APIs

```text
POST /api/counter-assignments
GET /api/counter-assignments
PATCH /api/counter-assignments/{assignmentId}/unassign
```

### Queue Ticket APIs

```text
POST /api/queue-tickets/from-booking/{bookingId}
POST /api/queue-tickets/walk-in
GET /api/queue-tickets/{ticketId}
GET /api/branches/{branchId}/queue-tickets
PATCH /api/queue-tickets/{ticketId}/assign-staff
PATCH /api/queue-tickets/{ticketId}/assign-counter
PATCH /api/queue-tickets/{ticketId}/start
PATCH /api/queue-tickets/{ticketId}/complete
PATCH /api/queue-tickets/{ticketId}/skip
PATCH /api/queue-tickets/{ticketId}/cancel
```

### Queue Event APIs

```text
GET /api/queue-tickets/{ticketId}/events
GET /api/queue-events
```

### Notification APIs

```text
GET /api/notifications/my
GET /api/notifications
PATCH /api/notifications/{notificationId}/retry
PATCH /api/notifications/{notificationId}/mark-sent
```

### Queue Prediction APIs

```text
POST /api/predictions/queue
GET /api/predictions/queue
GET /api/branches/{branchId}/predictions
```

### Prediction Log APIs

```text
GET /api/prediction-logs
GET /api/prediction-logs/{predictionLogId}
```

### No-show Prediction APIs

```text
POST /api/predictions/no-show/{bookingId}
GET /api/predictions/no-show/{bookingId}
GET /api/predictions/no-show
```

### Customer Feedback APIs

```text
POST /api/feedbacks
GET /api/feedbacks/my
GET /api/feedbacks
GET /api/feedbacks/{feedbackId}
```

### Audit Log APIs

```text
GET /api/audit-logs
GET /api/audit-logs/{auditId}
```

---

## 12. Recommended Project Structure

```text
smart-queue-system/
├── docs/
│   ├── architecture/
│   ├── erd/
│   ├── api-flow/
│   ├── planning/
│   └── screenshots/
│
├── postman/
│   └── smart-queue-system.postman_collection.json
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/khang/smartqueue/
│   │   │       ├── base/
│   │   │       ├── config/
│   │   │       ├── constants/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── initializer/
│   │   │       ├── mapper/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       ├── serviceImpl/
│   │   │       ├── specification/
│   │   │       └── SmartQueueApplication.java
│   │   │
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   │
│   └── test/
│
├── docker-compose.yml
├── Dockerfile
├── README.md
└── pom.xml
```

---

## 13. Java Package Structure

Use:

```text
com.khang.smartqueue
```

Recommended folders:

```text
base
config
constants
controller
dto
entity
exception
initializer
mapper
repository
service
serviceImpl
specification
```

---

## 14. Implementation Order

### Phase 1 — Foundation

Build first:

```text
Project setup
Pom dependencies
Application config
Flyway migration
Role/User/Auth
JWT security
Swagger config
Global exception handler
BaseEntity
SoftDeleteEntity
```

### Phase 2 — Master Data

Build next:

```text
Branch
BranchSchedule
BranchHoliday
ServiceType
ServiceCapacitySlot
Counter
```

### Phase 3 — Booking Core

Build:

```text
Booking create
Booking cancel
Booking confirm
Booking capacity validation
Duplicate active booking validation
```

### Phase 4 — Queue Core

Build:

```text
Queue ticket from booking
Walk-in queue ticket
Queue status transition
Assign staff
Assign counter
Queue event history
Optimistic locking
```

### Phase 5 — Staff Operations

Build:

```text
Staff shift
Counter assignment
Active counter assignment validation
Staff queue dashboard APIs
```

### Phase 6 — Notification

Build:

```text
Notification creation
Notification retry
Notification status update
```

### Phase 7 — AI / Analytics

Build after core works:

```text
QueuePrediction
PredictionLog
NoShowPrediction
CustomerFeedback
AuditLog
```

### Phase 8 — Polish

Build last:

```text
Unit tests
Integration tests
Docker
Postman collection
README
Swagger screenshots
GitHub cleanup
```

---

## 15. Things AI Must Not Do

Do not:

```text
Generate Payment module yet
Generate Queue Display module yet
Generate multi-tenant system yet
Use queue_position column
Store plaintext password
Use password instead of password_hash
Use timestamp instead of timestamptz for real event timestamps
Soft delete transactional tables
Use composite unique booking slot directly in DBML as final DB rule
Ignore Flyway as database source of truth
Create too many unnecessary bidirectional @OneToMany relationships
Expose entity directly in API response
Skip DTO layer
Skip validation layer
Skip service layer
Put business logic inside controller
Generate frontend before backend core is stable
```

---

## 16. DTO / API Rules

Use DTOs for all API requests and responses.

Do not return JPA entities directly from controllers.

Controller responsibilities:

```text
Receive request
Validate request DTO
Call service
Return response DTO
```

Service responsibilities:

```text
Business logic
Validation logic
Transaction handling
Status transition handling
Repository orchestration
```

Repository responsibilities:

```text
Database access only
Custom query methods
Specification support
```

---

## 17. Security Rules

Use JWT authentication.

Use role-based authorization.

Passwords must be hashed before saving.

Recommended access rules:

```text
/api/auth/** = public
Swagger = public in dev only
User profile = authenticated
Booking create/cancel = USER
Queue operations = STAFF or ADMIN
Branch/service management = ADMIN
Prediction/admin analytics = ADMIN
Audit logs = ADMIN
```

---

## 18. Final Recruiter Goal

This project should show:

* Production-style backend architecture
* Real-world queue and booking workflow
* Strong database design
* Clean JPA entity mapping
* Business rule validation at database and service level
* Concurrency handling with optimistic locking
* Notification retry logic
* AI/analytics-ready schema
* Professional GitHub documentation
* Clear README and API documentation

The final result should look like a junior backend engineer project, not a student CRUD assignment.
