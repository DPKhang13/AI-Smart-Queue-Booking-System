-- ============================================================
-- AI SMART QUEUE & BOOKING SYSTEM
-- Flyway Migration: V1__init_schema.sql
-- Database: PostgreSQL
-- ============================================================

-- ============================================================
-- 1. ROLES
-- ============================================================

CREATE TABLE roles (
    role_id BIGSERIAL PRIMARY KEY,

    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

-- ============================================================
-- 2. USERS
-- ============================================================

CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,

    role_id BIGINT NOT NULL,

    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(30) UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id),

    CONSTRAINT chk_users_deleted_at
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL)
            OR
            (is_deleted = TRUE AND deleted_at IS NOT NULL)
        )
);

-- ============================================================
-- 3. BRANCHES
-- ============================================================

CREATE TABLE branches (
    branch_id BIGSERIAL PRIMARY KEY,

    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(30),

    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,

    max_queue_capacity INT NOT NULL,
    average_service_duration INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT chk_branches_capacity
        CHECK (max_queue_capacity > 0),

    CONSTRAINT chk_branches_avg_duration
        CHECK (average_service_duration > 0),

    CONSTRAINT chk_branches_opening_hours
        CHECK (opening_time < closing_time),

    CONSTRAINT chk_branches_deleted_at
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL)
            OR
            (is_deleted = TRUE AND deleted_at IS NOT NULL)
        )
);

-- ============================================================
-- 4. SERVICE TYPES
-- ============================================================

CREATE TABLE service_types (
    service_type_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,

    name VARCHAR(150) NOT NULL,
    description TEXT,

    estimated_duration_minutes INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_service_types_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT chk_service_types_duration
        CHECK (estimated_duration_minutes > 0),

    CONSTRAINT chk_service_types_deleted_at
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL)
            OR
            (is_deleted = TRUE AND deleted_at IS NOT NULL)
        )
);

-- ============================================================
-- 5. BOOKINGS
-- ============================================================

CREATE TABLE bookings (
    booking_id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,

    booking_code VARCHAR(50) UNIQUE NOT NULL,

    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,

    status VARCHAR(30) NOT NULL,

    note TEXT,

    cancelled_at TIMESTAMPTZ,
    cancellation_reason TEXT,

    archived_at TIMESTAMPTZ,

    version INT NOT NULL DEFAULT 1,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_bookings_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT fk_bookings_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id),

    CONSTRAINT chk_booking_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),

    CONSTRAINT chk_booking_version
        CHECK (version >= 1),

    CONSTRAINT chk_booking_cancelled_fields
        CHECK (
            (status <> 'CANCELLED')
            OR
            (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
        ),

    CONSTRAINT chk_booking_archived_status
        CHECK (
            archived_at IS NULL
            OR status IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')
        )
);

-- Partial unique index:
-- A user cannot have duplicate active bookings in the same branch and time slot.
-- CANCELLED / COMPLETED / NO_SHOW bookings do not block future booking attempts.
CREATE UNIQUE INDEX uq_active_booking_slot
ON bookings (user_id, branch_id, booking_date, booking_time)
WHERE status IN ('PENDING', 'CONFIRMED');

-- ============================================================
-- 6. QUEUE TICKETS
-- ============================================================

CREATE TABLE queue_tickets (
    ticket_id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT UNIQUE,
    branch_id BIGINT NOT NULL,
    customer_id BIGINT,
    assigned_staff_id BIGINT,

    guest_name VARCHAR(150),
    guest_phone VARCHAR(30),

    counter_name VARCHAR(50),

    ticket_number VARCHAR(30) NOT NULL,
    queue_date DATE NOT NULL,

    status VARCHAR(30) NOT NULL,

    check_in_time TIMESTAMPTZ,
    start_service_time TIMESTAMPTZ,
    completed_time TIMESTAMPTZ,

    estimated_wait_minutes INT,
    actual_wait_minutes INT,

    version INT NOT NULL DEFAULT 1,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_queue_tickets_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id),

    CONSTRAINT fk_queue_tickets_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT fk_queue_tickets_customer
        FOREIGN KEY (customer_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_queue_tickets_assigned_staff
        FOREIGN KEY (assigned_staff_id)
        REFERENCES users(user_id),

    CONSTRAINT uq_ticket_per_branch_date
        UNIQUE (branch_id, queue_date, ticket_number),

    CONSTRAINT chk_queue_ticket_status
        CHECK (status IN ('WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'SKIPPED')),

    CONSTRAINT chk_queue_ticket_version
        CHECK (version >= 1),

    CONSTRAINT chk_queue_ticket_wait_minutes
        CHECK (
            (estimated_wait_minutes IS NULL OR estimated_wait_minutes >= 0)
            AND
            (actual_wait_minutes IS NULL OR actual_wait_minutes >= 0)
        ),

    CONSTRAINT chk_queue_ticket_customer_or_guest
        CHECK (
            customer_id IS NOT NULL
            OR (guest_name IS NOT NULL AND TRIM(guest_name) <> '')
            OR (guest_phone IS NOT NULL AND TRIM(guest_phone) <> '')
        ),

    CONSTRAINT chk_queue_ticket_time_order
        CHECK (
            (start_service_time IS NULL OR check_in_time IS NULL OR start_service_time >= check_in_time)
            AND
            (completed_time IS NULL OR start_service_time IS NULL OR completed_time >= start_service_time)
        )
);

-- Note:
-- queue_position is intentionally not stored.
-- Queue order should be calculated using:
-- ORDER BY check_in_time ASC NULLS LAST, ticket_id ASC

-- ============================================================
-- 7. QUEUE EVENTS
-- ============================================================

CREATE TABLE queue_events (
    event_id BIGSERIAL PRIMARY KEY,

    queue_ticket_id BIGINT NOT NULL,
    performed_by_id BIGINT,

    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,

    event_type VARCHAR(50) NOT NULL,
    note TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_queue_events_ticket
        FOREIGN KEY (queue_ticket_id)
        REFERENCES queue_tickets(ticket_id),

    CONSTRAINT fk_queue_events_performed_by
        FOREIGN KEY (performed_by_id)
        REFERENCES users(user_id),

    CONSTRAINT chk_queue_event_old_status
        CHECK (
            old_status IS NULL
            OR old_status IN ('WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'SKIPPED')
        ),

    CONSTRAINT chk_queue_event_new_status
        CHECK (new_status IN ('WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'SKIPPED')),

    CONSTRAINT chk_queue_event_type
        CHECK (event_type IN (
            'TICKET_CREATED',
            'SERVICE_STARTED',
            'SERVICE_COMPLETED',
            'TICKET_CANCELLED',
            'TICKET_SKIPPED',
            'STAFF_ASSIGNED',
            'TICKET_UPDATED'
        ))
);

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    booking_id BIGINT,

    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,

    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,

    recipient_target VARCHAR(150),

    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    last_retry_at TIMESTAMPTZ,

    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_notifications_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id),

    CONSTRAINT chk_notification_type
        CHECK (type IN (
            'BOOKING_CONFIRMATION',
            'BOOKING_CANCELLED',
            'QUEUE_REMINDER',
            'QUEUE_UPDATED'
        )),

    CONSTRAINT chk_notification_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED')),

    CONSTRAINT chk_notification_retry_count
        CHECK (retry_count >= 0)
);

-- ============================================================
-- 9. QUEUE PREDICTIONS
-- ============================================================

CREATE TABLE queue_predictions (
    prediction_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,

    prediction_date DATE NOT NULL,
    prediction_time TIME NOT NULL,

    predicted_wait_minutes INT NOT NULL,
    predicted_queue_length INT,

    confidence_score NUMERIC(5, 2),

    method VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_queue_predictions_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT fk_queue_predictions_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id),

    CONSTRAINT chk_prediction_wait_minutes
        CHECK (predicted_wait_minutes >= 0 AND predicted_wait_minutes <= 1440),

    CONSTRAINT chk_prediction_queue_length
        CHECK (predicted_queue_length IS NULL OR predicted_queue_length >= 0),

    CONSTRAINT chk_prediction_confidence_score
        CHECK (
            confidence_score IS NULL
            OR
            (confidence_score >= 0 AND confidence_score <= 100)
        ),

    CONSTRAINT chk_prediction_method
        CHECK (method IN ('RULE_BASED', 'HISTORICAL_AVERAGE', 'AI_MODEL'))
);

-- ============================================================
-- 10. PREDICTION LOGS
-- ============================================================

CREATE TABLE prediction_logs (
    prediction_log_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,

    input_data JSONB NOT NULL,
    output_data JSONB NOT NULL,

    confidence_score NUMERIC(5, 2),
    model_version VARCHAR(50),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_prediction_logs_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT fk_prediction_logs_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id),

    CONSTRAINT chk_prediction_logs_confidence_score
        CHECK (
            confidence_score IS NULL
            OR
            (confidence_score >= 0 AND confidence_score <= 100)
        )
);

-- ============================================================
-- 11. AUDIT LOGS
-- ============================================================

CREATE TABLE audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,

    performed_by_id BIGINT,

    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,

    old_value JSONB,
    new_value JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_logs_performed_by
        FOREIGN KEY (performed_by_id)
        REFERENCES users(user_id)
);

-- ============================================================
-- 12. INDEXES
-- ============================================================

-- USERS
CREATE INDEX idx_users_role_id
ON users(role_id);

CREATE INDEX idx_users_active_deleted
ON users(is_active, is_deleted);

-- BRANCHES
CREATE INDEX idx_branches_active_deleted
ON branches(is_active, is_deleted);

-- SERVICE TYPES
CREATE INDEX idx_service_types_branch_id
ON service_types(branch_id);

CREATE INDEX idx_service_types_active_deleted
ON service_types(is_active, is_deleted);

-- BOOKINGS
CREATE INDEX idx_bookings_user_id
ON bookings(user_id);

CREATE INDEX idx_bookings_branch_id
ON bookings(branch_id);

CREATE INDEX idx_bookings_service_type_id
ON bookings(service_type_id);

CREATE INDEX idx_bookings_status
ON bookings(status);

CREATE INDEX idx_bookings_date_status
ON bookings(booking_date, status);

-- Supports historical booking lookup across all statuses,
-- while uq_active_booking_slot only covers active bookings.
CREATE INDEX idx_bookings_user_branch_datetime
ON bookings(user_id, branch_id, booking_date, booking_time);

-- QUEUE TICKETS
CREATE INDEX idx_queue_tickets_status
ON queue_tickets(status);

CREATE INDEX idx_queue_tickets_branch_date
ON queue_tickets(branch_id, queue_date);

CREATE INDEX idx_queue_tickets_customer_id
ON queue_tickets(customer_id);

CREATE INDEX idx_queue_tickets_assigned_staff_id
ON queue_tickets(assigned_staff_id);

CREATE INDEX idx_queue_tickets_booking_id
ON queue_tickets(booking_id);

CREATE INDEX idx_queue_tickets_queue_order
ON queue_tickets(branch_id, queue_date, status, check_in_time, ticket_id);

-- QUEUE EVENTS
CREATE INDEX idx_queue_events_ticket_id
ON queue_events(queue_ticket_id);

CREATE INDEX idx_queue_events_performed_by_id
ON queue_events(performed_by_id);

CREATE INDEX idx_queue_events_created_at
ON queue_events(created_at);

-- NOTIFICATIONS
CREATE INDEX idx_notifications_user_id
ON notifications(user_id);

CREATE INDEX idx_notifications_booking_id
ON notifications(booking_id);

CREATE INDEX idx_notifications_status
ON notifications(status);

CREATE INDEX idx_notifications_retry
ON notifications(status, retry_count, last_retry_at);

-- QUEUE PREDICTIONS
CREATE INDEX idx_queue_predictions_branch_id
ON queue_predictions(branch_id);

CREATE INDEX idx_queue_predictions_service_type_id
ON queue_predictions(service_type_id);

CREATE INDEX idx_queue_predictions_branch_date
ON queue_predictions(branch_id, prediction_date);

-- PREDICTION LOGS
CREATE INDEX idx_prediction_logs_branch_id
ON prediction_logs(branch_id);

CREATE INDEX idx_prediction_logs_service_type_id
ON prediction_logs(service_type_id);

CREATE INDEX idx_prediction_logs_created_at
ON prediction_logs(created_at);

-- AUDIT LOGS
CREATE INDEX idx_audit_logs_performed_by_id
ON audit_logs(performed_by_id);

CREATE INDEX idx_audit_logs_entity
ON audit_logs(entity_name, entity_id);

CREATE INDEX idx_audit_logs_created_at
ON audit_logs(created_at);

