# AI Smart Queue & Booking System

<div align="center">

## Production-Style Backend Portfolio Project

Backend-focused smart booking and queue management system built with Java Spring Boot, PostgreSQL, Redis, JWT, and Docker.

![Java](https://img.shields.io/badge/Java-25-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-9.4-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-orange)

</div>

---

# Project Overview

AI Smart Queue & Booking System is a backend-focused portfolio project designed to simulate a production-style booking and queue management platform for service-based businesses such as:

- Cafés
- Clinics
- Salons
- EV service centers
- Customer support offices

The system allows users to:

- Create bookings
- Receive queue numbers
- Track queue progress
- View estimated waiting time
- Get AI-based booking recommendations

This project was intentionally designed to demonstrate real-world backend engineering concepts instead of a traditional university CRUD assignment.

---

# Why This Project Matters

Many businesses struggle with:

- Long customer waiting times
- Manual queue handling
- Poor booking optimization
- No analytics visibility
- Inefficient customer flow

This project solves those problems by combining:

- Booking management
- Queue tracking
- Branch capacity control
- Analytics dashboards
- AI-based prediction features

The system architecture was designed to simulate modern backend systems commonly used in startups, SaaS platforms, and fintech-style services.

---

# Main Features

## Authentication & Authorization

- User registration
- User login
- JWT authentication
- Password encryption
- Role-based authorization

Roles:

- USER
- STAFF
- ADMIN

---

## Booking Management

Users can:

- Create bookings
- Cancel bookings
- View booking history
- Check booking status

Validation logic includes:

- Duplicate booking prevention
- Capacity validation
- Operating-hours validation
- Queue overflow prevention

---

## Queue Management

The system supports:

- Queue ticket generation
- Queue number assignment
- Queue position tracking
- Estimated wait time calculation
- Staff queue processing
- Queue completion tracking

Queue statuses:

- WAITING
- IN_PROGRESS
- COMPLETED
- CANCELLED

---

## Branch Management

Admin can manage:

- Branch operating hours
- Booking capacity
- Active/inactive branches
- Branch performance

---

## Analytics Dashboard

Admin analytics include:

- Total bookings
- Cancellation rate
- Peak booking hours
- Average waiting time
- Queue completion rate
- Branch performance

---

## AI Prediction Module

AI features include:

- Estimated wait-time prediction
- Peak-hour prediction
- Smart booking recommendations
- Less crowded branch suggestions

Initial implementation:

- Rule-based prediction
- Historical data analysis

Future implementation:

- Python AI microservice
- ML prediction models

---

# Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT
- RESTful APIs

## Database

- PostgreSQL

## Cache

- Redis

## DevOps & Tools

- Docker
- Docker Compose
- Swagger/OpenAPI
- Postman
- GitHub

## Optional Advanced Features

- Kafka / RabbitMQ
- GitHub Actions CI/CD
- WebSocket / SSE
- Flyway
- JUnit & Mockito
- Testcontainers

---

# System Architecture

## High-Level Architecture

```text
Client Applications
        ↓
Spring Boot REST API
        ↓
-------------------------------------------------
| Auth Module                                   |
| Booking Module                                |
| Queue Module                                  |
| Analytics Module                              |
| Prediction Module                             |
| Notification Module                           |
-------------------------------------------------
        ↓
PostgreSQL + Redis
```

---

# Engineering Decisions

## Why JWT Authentication?

JWT provides stateless authentication, making the system scalable and suitable for distributed backend architectures.

---

## Why Redis?

Redis is used for:

- Queue caching
- Fast queue lookup
- Realtime queue tracking
- Reducing database load

---

## Why Layered Architecture?

The project uses a layered Spring Boot architecture to improve:

- Maintainability
- Scalability
- Code separation
- Team collaboration
- Testing capability

---

## Why DTO Pattern?

DTOs separate API responses from database entities and improve:

- Security
- API consistency
- Maintainability

---

# Source Code Structure

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

# Main Database Tables

| Table             | Purpose                 |
| ----------------- | ----------------------- |
| users             | User accounts and roles |
| branches          | Branch information      |
| bookings          | Booking records         |
| queue_tickets     | Queue management        |
| notifications     | Notification storage    |
| queue_predictions | AI prediction data      |

---

# Example API Endpoints

## Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

---

## Booking APIs

```http
POST /api/bookings
GET /api/bookings/my-bookings
PUT /api/bookings/{id}/cancel
```

---

## Queue APIs

```http
GET /api/queues/{ticketId}/position
PUT /api/queues/{ticketId}/start
PUT /api/queues/{ticketId}/complete
```

---

# Example API Response

```json
{
  "bookingId": 15,
  "queueNumber": "A-102",
  "estimatedWaitTime": 18,
  "status": "WAITING"
}
```

---

# System Workflow

```text
1. User registers or logs in
2. User selects a branch
3. User creates a booking
4. System validates booking rules
5. System generates queue ticket
6. User tracks queue position
7. Staff processes queue tickets
8. Admin monitors analytics dashboard
9. AI module recommends better booking times
```

---

# Scalability Considerations

The project was designed with scalability in mind:

- Modular backend structure
- Stateless JWT authentication
- Redis caching for queue optimization
- Separated business domains
- Async-ready architecture
- Future microservice compatibility

Planned scalability improvements:

- Kafka/RabbitMQ event-driven workflows
- WebSocket realtime updates
- AI microservices
- CI/CD automation
- Cloud deployment

---

# Current Project Status

| Feature                | Status         |
| ---------------------- | -------------- |
| Authentication System  | ✅ Completed   |
| Booking APIs           | ✅ Completed   |
| Queue Management       | ✅ Completed   |
| PostgreSQL Integration | ✅ Completed   |
| Redis Integration      | 🚧 In Progress |
| Analytics APIs         | 🚧 In Progress |
| AI Prediction Module   | 🚧 In Progress |
| Notification System    | 📌 Planned     |
| Kafka Integration      | 📌 Planned     |
| Deployment             | 📌 Planned     |

---

# Future Improvements

Planned improvements include:

- WebSocket realtime queue updates
- Kafka or RabbitMQ event processing
- Email/SMS notifications
- AI microservice integration
- Admin frontend dashboard
- CI/CD with GitHub Actions
- Unit & integration testing
- Cloud deployment

---

# How To Run The Project

## Clone Repository

```bash
git clone https://github.com/your-username/ai-smart-queue-system.git
cd ai-smart-queue-system
```

---

## Start PostgreSQL & Redis

```bash
docker-compose up -d
```

---

## Run Spring Boot Application

```bash
./mvnw spring-boot:run
```

For Windows:

```bash
mvnw.cmd spring-boot:run
```

---

# Planned Repository Structure

```text
smart-queue-system/
│
├── docs/
│   ├── architecture/
│   ├── erd/
│   ├── api-flow/
│   └── planning/
│
├── postman/
├── src/
├── docker-compose.yml
├── README.md
└── pom.xml
```

---

# Documentation Included

The repository will include:

- Architecture diagrams
- ERD diagrams
- API flow diagrams
- Postman collection
- Swagger documentation
- Docker configuration
- System planning files

---

# Recruiter-Focused Goals

This project was designed to demonstrate:

- Production-style backend architecture
- Real-world business logic implementation
- RESTful API design
- Scalable backend structure
- Database design knowledge
- Redis caching concepts
- AI integration concepts
- Docker deployment workflow
- Professional documentation quality

The goal is for this project to resemble a junior backend engineer portfolio project rather than a university assignment.

---

# Author

## Dang Phuc Khang

Software Engineering Student  
Backend Developer Intern Candidate
