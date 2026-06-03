-- ============================================================
-- AI SMART QUEUE & BOOKING SYSTEM
-- Flyway Migration: V2__init_schema.sql
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
-- 2. BRANCHES
-- ============================================================

CREATE TABLE branches (
    branch_id BIGSERIAL PRIMARY KEY,

    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(30),

    default_opening_time TIME NOT NULL,
    default_closing_time TIME NOT NULL,

    max_queue_capacity INT NOT NULL,
    average_service_duration INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

-- ============================================================
-- 3. USERS
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
        REFERENCES roles(role_id)
);

-- ============================================================
-- 4. BRANCH SCHEDULES
-- ============================================================

CREATE TABLE branch_schedules (
    schedule_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,

    day_of_week INT NOT NULL,

    opening_time TIME,
    closing_time TIME,

    is_closed BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_branch_schedules_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT uq_branch_schedules_branch_day
        UNIQUE (branch_id, day_of_week)
);

-- ============================================================
-- 5. BRANCH HOLIDAYS
-- ============================================================

CREATE TABLE branch_holidays (
    holiday_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,

    holiday_date DATE NOT NULL,
    reason VARCHAR(200),
    is_closed BOOLEAN NOT NULL DEFAULT TRUE,

    special_opening_time TIME,
    special_closing_time TIME,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_branch_holidays_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT uq_branch_holidays_branch_date
        UNIQUE (branch_id, holiday_date)
);

-- ============================================================
-- 6. SERVICE TYPES
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
        REFERENCES branches(branch_id)
);

-- ============================================================
-- 7. SERVICE CAPACITY SLOTS
-- ============================================================

CREATE TABLE service_capacity_slots (
    capacity_slot_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,

    day_of_week INT,
    specific_date DATE,

    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    max_bookings INT NOT NULL,
    max_queue_tickets INT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_service_capacity_slots_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT fk_service_capacity_slots_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id)
);

-- ============================================================
-- 8. BOOKINGS
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
        REFERENCES service_types(service_type_id)
);

-- ============================================================
-- 9. COUNTERS
-- ============================================================

CREATE TABLE counters (
    counter_id BIGSERIAL PRIMARY KEY,

    branch_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_counters_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT uq_counters_branch_name
        UNIQUE (branch_id, name)
);

-- ============================================================
-- 10. STAFF SHIFTS
-- ============================================================

CREATE TABLE staff_shifts (
    shift_id BIGSERIAL PRIMARY KEY,

    staff_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,

    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_staff_shifts_staff
        FOREIGN KEY (staff_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_staff_shifts_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id)
);

-- ============================================================
-- 11. COUNTER ASSIGNMENTS
-- ============================================================

CREATE TABLE counter_assignments (
    assignment_id BIGSERIAL PRIMARY KEY,

    counter_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    shift_id BIGINT,

    assigned_at TIMESTAMPTZ NOT NULL,
    unassigned_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_counter_assignments_counter
        FOREIGN KEY (counter_id)
        REFERENCES counters(counter_id),

    CONSTRAINT fk_counter_assignments_staff
        FOREIGN KEY (staff_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_counter_assignments_shift
        FOREIGN KEY (shift_id)
        REFERENCES staff_shifts(shift_id)
);

-- ============================================================
-- 12. QUEUE TICKETS
-- ============================================================

CREATE TABLE queue_tickets (
    ticket_id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT UNIQUE,
    branch_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,

    customer_id BIGINT,
    assigned_staff_id BIGINT,
    counter_id BIGINT,

    guest_name VARCHAR(150),
    guest_phone VARCHAR(30),

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

    CONSTRAINT fk_queue_tickets_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id),

    CONSTRAINT fk_queue_tickets_customer
        FOREIGN KEY (customer_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_queue_tickets_assigned_staff
        FOREIGN KEY (assigned_staff_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_queue_tickets_counter
        FOREIGN KEY (counter_id)
        REFERENCES counters(counter_id),

    CONSTRAINT uq_queue_ticket_per_branch_date
        UNIQUE (branch_id, queue_date, ticket_number)
);

-- ============================================================
-- 13. QUEUE EVENTS
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
        REFERENCES users(user_id)
);

-- ============================================================
-- 14. NO-SHOW PREDICTIONS
-- ============================================================

CREATE TABLE no_show_predictions (
    no_show_prediction_id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT UNIQUE NOT NULL,

    probability NUMERIC(5, 2) NOT NULL,
    risk_level VARCHAR(30) NOT NULL,

    model_version VARCHAR(50),
    input_data JSONB,
    output_data JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_no_show_predictions_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id)
);

-- ============================================================
-- 15. NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,

    user_id BIGINT,
    booking_id BIGINT,
    queue_ticket_id BIGINT,

    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,

    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,

    recipient_target VARCHAR(150) NOT NULL,

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

    CONSTRAINT fk_notifications_queue_ticket
        FOREIGN KEY (queue_ticket_id)
        REFERENCES queue_tickets(ticket_id)
);

-- ============================================================
-- 16. QUEUE PREDICTIONS
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
        REFERENCES service_types(service_type_id)
);

-- ============================================================
-- 17. PREDICTION LOGS
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
        REFERENCES service_types(service_type_id)
);

-- ============================================================
-- 18. CUSTOMER FEEDBACKS
-- ============================================================

CREATE TABLE customer_feedbacks (
    feedback_id BIGSERIAL PRIMARY KEY,

    user_id BIGINT,
    queue_ticket_id BIGINT UNIQUE,
    booking_id BIGINT,

    branch_id BIGINT NOT NULL,
    service_type_id BIGINT,

    rating INT NOT NULL,
    comment TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_customer_feedbacks_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_customer_feedbacks_queue_ticket
        FOREIGN KEY (queue_ticket_id)
        REFERENCES queue_tickets(ticket_id),

    CONSTRAINT fk_customer_feedbacks_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id),

    CONSTRAINT fk_customer_feedbacks_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id),

    CONSTRAINT fk_customer_feedbacks_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id)
);

-- ============================================================
-- 19. AUDIT LOGS
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
-- 20. INDEXES
-- ============================================================

-- USERS
CREATE INDEX idx_users_role_id
ON users(role_id);

CREATE INDEX idx_users_active_deleted
ON users(is_active, is_deleted);

-- BRANCHES
CREATE INDEX idx_branches_active_deleted
ON branches(is_active, is_deleted);

-- BRANCH SCHEDULES
CREATE INDEX idx_branch_schedules_branch_id
ON branch_schedules(branch_id);

-- BRANCH HOLIDAYS
CREATE INDEX idx_branch_holidays_branch_id
ON branch_holidays(branch_id);

-- SERVICE TYPES
CREATE INDEX idx_service_types_branch_id
ON service_types(branch_id);

CREATE INDEX idx_service_types_active_deleted
ON service_types(is_active, is_deleted);

-- SERVICE CAPACITY SLOTS
CREATE INDEX idx_service_capacity_slots_branch_id
ON service_capacity_slots(branch_id);

CREATE INDEX idx_service_capacity_slots_service_type_id
ON service_capacity_slots(service_type_id);

CREATE INDEX idx_service_capacity_slots_weekly
ON service_capacity_slots(branch_id, service_type_id, day_of_week, start_time);

CREATE INDEX idx_service_capacity_slots_specific_date
ON service_capacity_slots(branch_id, service_type_id, specific_date, start_time);

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

CREATE INDEX idx_bookings_user_branch_datetime
ON bookings(user_id, branch_id, booking_date, booking_time);

-- Optional stronger protection against race conditions for active booking slots.
-- Enable if you want the database to reject duplicate active bookings.
-- CREATE UNIQUE INDEX uq_active_booking_slot
-- ON bookings(user_id, branch_id, booking_date, booking_time)
-- WHERE status IN ('PENDING', 'CONFIRMED');

-- COUNTERS
CREATE INDEX idx_counters_branch_id
ON counters(branch_id);

-- STAFF SHIFTS
CREATE INDEX idx_staff_shifts_staff_id
ON staff_shifts(staff_id);

CREATE INDEX idx_staff_shifts_branch_id
ON staff_shifts(branch_id);

CREATE INDEX idx_staff_shifts_staff_date
ON staff_shifts(staff_id, shift_date);

CREATE INDEX idx_staff_shifts_branch_date
ON staff_shifts(branch_id, shift_date);

-- COUNTER ASSIGNMENTS
CREATE INDEX idx_counter_assignments_counter_id
ON counter_assignments(counter_id);

CREATE INDEX idx_counter_assignments_staff_id
ON counter_assignments(staff_id);

CREATE INDEX idx_counter_assignments_shift_id
ON counter_assignments(shift_id);

CREATE INDEX idx_counter_assignments_counter_assigned_at
ON counter_assignments(counter_id, assigned_at);

-- QUEUE TICKETS
CREATE INDEX idx_queue_tickets_branch_id
ON queue_tickets(branch_id);

CREATE INDEX idx_queue_tickets_service_type_id
ON queue_tickets(service_type_id);

CREATE INDEX idx_queue_tickets_customer_id
ON queue_tickets(customer_id);

CREATE INDEX idx_queue_tickets_assigned_staff_id
ON queue_tickets(assigned_staff_id);

CREATE INDEX idx_queue_tickets_counter_id
ON queue_tickets(counter_id);

CREATE INDEX idx_queue_tickets_status
ON queue_tickets(status);

CREATE INDEX idx_queue_tickets_branch_date
ON queue_tickets(branch_id, queue_date);

CREATE INDEX idx_queue_tickets_queue_order
ON queue_tickets(branch_id, queue_date, status, check_in_time, ticket_id);

-- QUEUE EVENTS
CREATE INDEX idx_queue_events_ticket_id
ON queue_events(queue_ticket_id);

CREATE INDEX idx_queue_events_performed_by_id
ON queue_events(performed_by_id);

CREATE INDEX idx_queue_events_created_at
ON queue_events(created_at);

-- NO-SHOW PREDICTIONS
CREATE INDEX idx_no_show_predictions_risk_level
ON no_show_predictions(risk_level);

CREATE INDEX idx_no_show_predictions_created_at
ON no_show_predictions(created_at);

-- NOTIFICATIONS
CREATE INDEX idx_notifications_user_id
ON notifications(user_id);

CREATE INDEX idx_notifications_booking_id
ON notifications(booking_id);

CREATE INDEX idx_notifications_queue_ticket_id
ON notifications(queue_ticket_id);

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

CREATE INDEX idx_queue_predictions_lookup
ON queue_predictions(branch_id, service_type_id, prediction_date, prediction_time);

-- PREDICTION LOGS
CREATE INDEX idx_prediction_logs_branch_id
ON prediction_logs(branch_id);

CREATE INDEX idx_prediction_logs_service_type_id
ON prediction_logs(service_type_id);

CREATE INDEX idx_prediction_logs_created_at
ON prediction_logs(created_at);

-- CUSTOMER FEEDBACKS
CREATE INDEX idx_customer_feedbacks_user_id
ON customer_feedbacks(user_id);

CREATE INDEX idx_customer_feedbacks_booking_id
ON customer_feedbacks(booking_id);

CREATE INDEX idx_customer_feedbacks_branch_id
ON customer_feedbacks(branch_id);

CREATE INDEX idx_customer_feedbacks_service_type_id
ON customer_feedbacks(service_type_id);

CREATE INDEX idx_customer_feedbacks_rating
ON customer_feedbacks(rating);

CREATE INDEX idx_customer_feedbacks_created_at
ON customer_feedbacks(created_at);

-- AUDIT LOGS
CREATE INDEX idx_audit_logs_performed_by_id
ON audit_logs(performed_by_id);

CREATE INDEX idx_audit_logs_entity
ON audit_logs(entity_name, entity_id);

CREATE INDEX idx_audit_logs_created_at
ON audit_logs(created_at);

-- Optional JSONB GIN indexes.
-- Enable later if JSONB query performance becomes important.
-- CREATE INDEX idx_no_show_predictions_input_data_gin ON no_show_predictions USING GIN (input_data);
-- CREATE INDEX idx_no_show_predictions_output_data_gin ON no_show_predictions USING GIN (output_data);
-- CREATE INDEX idx_prediction_logs_input_data_gin ON prediction_logs USING GIN (input_data);
-- CREATE INDEX idx_prediction_logs_output_data_gin ON prediction_logs USING GIN (output_data);
-- CREATE INDEX idx_audit_logs_old_value_gin ON audit_logs USING GIN (old_value);
-- CREATE INDEX idx_audit_logs_new_value_gin ON audit_logs USING GIN (new_value);

-- ============================================================
-- BUSINESS CONSTRAINTS
-- ============================================================

-- Active booking slot protection.
CREATE UNIQUE INDEX uq_active_booking_slot
ON bookings(user_id, branch_id, booking_date, booking_time)
WHERE status IN ('PENDING', 'CONFIRMED');

-- Booking status.
ALTER TABLE bookings
ADD CONSTRAINT chk_booking_status
CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'));

-- Queue ticket status.
ALTER TABLE queue_tickets
ADD CONSTRAINT chk_queue_ticket_status
CHECK (status IN ('WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'SKIPPED'));

-- Notification status.
ALTER TABLE notifications
ADD CONSTRAINT chk_notification_status
CHECK (status IN ('PENDING', 'SENT', 'FAILED'));

-- Soft delete consistency.
ALTER TABLE users
ADD CONSTRAINT chk_users_deleted_at
CHECK (
  (is_deleted = FALSE AND deleted_at IS NULL)
  OR
  (is_deleted = TRUE AND deleted_at IS NOT NULL)
);

ALTER TABLE branches
ADD CONSTRAINT chk_branches_deleted_at
CHECK (
  (is_deleted = FALSE AND deleted_at IS NULL)
  OR
  (is_deleted = TRUE AND deleted_at IS NOT NULL)
);

ALTER TABLE service_types
ADD CONSTRAINT chk_service_types_deleted_at
CHECK (
  (is_deleted = FALSE AND deleted_at IS NULL)
  OR
  (is_deleted = TRUE AND deleted_at IS NOT NULL)
);

-- Walk-in / customer ownership.
ALTER TABLE queue_tickets
ADD CONSTRAINT chk_queue_ticket_customer_or_guest
CHECK (
  customer_id IS NOT NULL
  OR (guest_name IS NOT NULL AND TRIM(guest_name) <> '')
  OR (guest_phone IS NOT NULL AND TRIM(guest_phone) <> '')
);

-- A counter can have only one active staff assignment.
CREATE UNIQUE INDEX uq_active_counter_assignment
ON counter_assignments(counter_id)
WHERE unassigned_at IS NULL;

-- Branch default opening hours.
ALTER TABLE branches
ADD CONSTRAINT chk_branches_default_opening_hours
CHECK (default_opening_time < default_closing_time);

-- Capacity slot must be either weekly or date-specific, not both.
ALTER TABLE service_capacity_slots
ADD CONSTRAINT chk_capacity_slots_day_or_date
CHECK (
  (day_of_week IS NOT NULL AND specific_date IS NULL)
  OR
  (day_of_week IS NULL AND specific_date IS NOT NULL)
);

-- Capacity slot time range.
ALTER TABLE service_capacity_slots
ADD CONSTRAINT chk_capacity_slots_time_range
CHECK (start_time < end_time);

-- Queue ticket time order.
ALTER TABLE queue_tickets
ADD CONSTRAINT chk_queue_ticket_time_order
CHECK (
  (start_service_time IS NULL OR check_in_time IS NULL OR start_service_time >= check_in_time)
  AND
  (completed_time IS NULL OR start_service_time IS NULL OR completed_time >= start_service_time)
);

-- Queue prediction wait time.
ALTER TABLE queue_predictions
ADD CONSTRAINT chk_prediction_wait_minutes
CHECK (predicted_wait_minutes >= 0 AND predicted_wait_minutes <= 1440);

-- Customer feedback rating.
ALTER TABLE customer_feedbacks
ADD CONSTRAINT chk_customer_feedback_rating
CHECK (rating BETWEEN 1 AND 5);
