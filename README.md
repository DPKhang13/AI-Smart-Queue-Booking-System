# AI Smart Queue & Booking System

## Overview

**AI Smart Queue & Booking System** is a backend-focused personal project built with **Java Spring Boot**.  
The system helps users book service appointments, receive queue numbers, track queue progress, and get smart recommendations for better booking times or less crowded branches.

This project is designed to demonstrate backend engineering skills such as RESTful API design, authentication, role-based access control, queue management, database design, caching, and AI-based prediction logic.

---

## Project Purpose

Many service businesses such as cafés, clinics, salons, vehicle service centers, and customer support offices face the same problem: customers often wait too long because booking and queue systems are not optimized.

This project solves that problem by providing:

- Online booking management
- Queue ticket generation
- Real-time queue status tracking
- Branch capacity management
- Admin analytics dashboard
- AI-based wait-time and peak-hour prediction

---

## Main Features

### Authentication & Authorization

- User registration
- User login
- JWT authentication
- Role-based access control

Roles:

- `USER`
- `STAFF`
- `ADMIN`

---

### Branch Management

Admin can manage service branches, including:

- Branch name
- Branch location
- Opening time
- Closing time
- Booking capacity
- Active/inactive status

---

### Booking Management

Users can:

- Create a booking
- Cancel a booking
- View booking history
- Check booking status

Booking validation includes:

- Preventing duplicate bookings
- Checking branch availability
- Checking operating hours
- Checking queue capacity

---

### Queue Management

The system supports:

- Queue ticket generation
- Queue number assignment
- Queue status tracking
- Staff queue processing
- Estimated waiting time calculation

Queue status examples:

- `WAITING`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

---

### Admin Dashboard & Analytics

Admin can view:

- Total bookings
- Total cancelled bookings
- Average waiting time
- Peak booking hours
- Branch performance
- Queue completion rate

---

### AI Prediction Module

The AI module can provide:

- Estimated waiting time prediction
- Peak-hour prediction
- Less crowded branch recommendation
- Suggested booking time

The first version can use rule-based prediction or simple historical data analysis.  
Later versions can be upgraded with a Python AI microservice.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT
- RESTful API

### Database

- PostgreSQL

### Cache

- Redis

### Tools

- Docker
- Docker Compose
- Swagger / OpenAPI
- Postman
- GitHub

### Optional Advanced Tools

- RabbitMQ or Kafka
- GitHub Actions
- JUnit
- Mockito
- Testcontainers
- Flyway

---

## Source Code Structure

This project follows a layered Spring Boot structure similar to common enterprise backend projects.

```text
src/main/java/com/khang/smartqueue/
│
├── base
│   ├── BaseEntity.java
│   ├── BaseResponse.java
│   └── BaseController.java
│
├── config
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── RedisConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
│
├── constants
│   ├── RoleConstants.java
│   ├── BookingStatus.java
│   ├── QueueStatus.java
│   └── MessageConstants.java
│
├── controller
│   ├── AuthController.java
│   ├── UserController.java
│   ├── BranchController.java
│   ├── BookingController.java
│   ├── QueueController.java
│   ├── NotificationController.java
│   ├── AnalyticsController.java
│   └── PredictionController.java
│
├── dto
│   ├── request
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── CreateBookingRequest.java
│   │   ├── CancelBookingRequest.java
│   │   ├── CreateBranchRequest.java
│   │   └── UpdateBranchRequest.java
│   │
│   └── response
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── BranchResponse.java
│       ├── BookingResponse.java
│       ├── QueueTicketResponse.java
│       ├── AnalyticsResponse.java
│       └── PredictionResponse.java
│
├── entity
│   ├── User.java
│   ├── Branch.java
│   ├── Booking.java
│   ├── QueueTicket.java
│   ├── Notification.java
│   └── QueuePrediction.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── AppException.java
│   ├── ErrorCode.java
│   └── ErrorResponse.java
│
├── initializer
│   ├── DataInitializer.java
│   └── RoleInitializer.java
│
├── mapper
│   ├── UserMapper.java
│   ├── BranchMapper.java
│   ├── BookingMapper.java
│   ├── QueueMapper.java
│   └── NotificationMapper.java
│
├── repository
│   ├── UserRepository.java
│   ├── BranchRepository.java
│   ├── BookingRepository.java
│   ├── QueueTicketRepository.java
│   ├── NotificationRepository.java
│   └── QueuePredictionRepository.java
│
├── service
│   ├── AuthService.java
│   ├── UserService.java
│   ├── BranchService.java
│   ├── BookingService.java
│   ├── QueueService.java
│   ├── NotificationService.java
│   ├── AnalyticsService.java
│   └── PredictionService.java
│
├── serviceImpl
│   ├── AuthServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── BranchServiceImpl.java
│   ├── BookingServiceImpl.java
│   ├── QueueServiceImpl.java
│   ├── NotificationServiceImpl.java
│   ├── AnalyticsServiceImpl.java
│   └── PredictionServiceImpl.java
│
├── specification
│   ├── BookingSpecification.java
│   ├── BranchSpecification.java
│   └── UserSpecification.java
│
└── SmartQueueApplication.java
```

---

## Folder Explanation

### `base`

Contains reusable base classes such as base entity, base response, and shared controller logic.

### `config`

Contains project configuration such as security, JWT filter, Redis, Swagger, and CORS settings.

### `constants`

Contains enums and constant values used across the system.

### `controller`

Handles incoming API requests and returns responses to the client.

### `dto`

Contains request and response objects.  
DTOs help separate API data from database entities.

### `entity`

Contains JPA entity classes mapped to database tables.

### `exception`

Handles application errors and global exception responses.

### `initializer`

Contains default data setup such as default admin account, sample branches, and roles.

### `mapper`

Converts entities to DTOs and DTOs to entities.

### `repository`

Handles database access using Spring Data JPA.

### `service`

Contains service interfaces that define business logic contracts.

### `serviceImpl`

Contains service implementation classes where the main business logic is written.

### `specification`

Contains dynamic query filters for searching, filtering, and sorting data.

---

## Example API Endpoints

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

### User

```http
GET /api/users/profile
PUT /api/users/profile
```

### Branch

```http
POST /api/branches
GET /api/branches
GET /api/branches/{id}
PUT /api/branches/{id}
DELETE /api/branches/{id}
```

### Booking

```http
POST /api/bookings
GET /api/bookings/my-bookings
GET /api/bookings/{id}
PUT /api/bookings/{id}/cancel
```

### Queue

```http
GET /api/queues/branch/{branchId}
PUT /api/queues/{ticketId}/start
PUT /api/queues/{ticketId}/complete
GET /api/queues/{ticketId}/position
```

### Analytics

```http
GET /api/admin/analytics/overview
GET /api/admin/analytics/peak-hours
GET /api/admin/analytics/branch-performance
```

### AI Prediction

```http
GET /api/predictions/wait-time?branchId=1
GET /api/predictions/recommend-branch
GET /api/predictions/recommend-time
```

---

## Database Tables

Main tables:

- `users`
- `branches`
- `bookings`
- `queue_tickets`
- `notifications`
- `queue_predictions`

---

## System Workflow

```text
1. User registers or logs in
2. User selects a branch
3. User creates a booking
4. System validates booking rules
5. System generates a queue ticket
6. User tracks queue position
7. Staff processes queue tickets
8. Admin views analytics dashboard
9. AI module recommends better booking times or branches
```

---

## Future Improvements

- WebSocket real-time queue updates
- Kafka or RabbitMQ event-driven notification
- Email and SMS notification
- AI microservice using Python
- Admin frontend dashboard
- CI/CD using GitHub Actions
- Unit and integration testing
- Deployment to Render, Railway, AWS, or VPS

## Author

**Dang Phuc Khang**  
Software Engineering Student  
Backend Developer Intern Candidate
