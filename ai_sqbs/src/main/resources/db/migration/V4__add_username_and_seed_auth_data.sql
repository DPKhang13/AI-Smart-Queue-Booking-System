ALTER TABLE users
ADD COLUMN username VARCHAR(50);

CREATE UNIQUE INDEX uq_users_username_lower
ON users(LOWER(username))
WHERE username IS NOT NULL;

INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'System administrator'),
    ('STAFF', 'Staff user'),
    ('USER', 'Customer user')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (
    role_id,
    full_name,
    email,
    username,
    phone,
    password_hash,
    is_active,
    is_deleted,
    email_verified,
    email_verified_at
)
SELECT
    r.role_id,
    'Seed Admin',
    'admin@smartqueue.local',
    'admin',
    NULL,
    '$2a$10$/QgmrLHf1vgbHtYmchkx5.CoKr4AG4i5ifKm2tdGfaW8jQ72hdsYe',
    TRUE,
    FALSE,
    TRUE,
    NOW()
FROM roles r
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM users u
      WHERE LOWER(u.email) = 'admin@smartqueue.local'
         OR LOWER(u.username) = 'admin'
  );

INSERT INTO users (
    role_id,
    full_name,
    email,
    username,
    phone,
    password_hash,
    is_active,
    is_deleted,
    email_verified,
    email_verified_at
)
SELECT
    r.role_id,
    'Seed Staff',
    'staff@smartqueue.local',
    'staff',
    NULL,
    '$2a$10$/QgmrLHf1vgbHtYmchkx5.CoKr4AG4i5ifKm2tdGfaW8jQ72hdsYe',
    TRUE,
    FALSE,
    TRUE,
    NOW()
FROM roles r
WHERE r.name = 'STAFF'
  AND NOT EXISTS (
      SELECT 1 FROM users u
      WHERE LOWER(u.email) = 'staff@smartqueue.local'
         OR LOWER(u.username) = 'staff'
  );

INSERT INTO users (
    role_id,
    full_name,
    email,
    username,
    phone,
    password_hash,
    is_active,
    is_deleted,
    email_verified,
    email_verified_at
)
SELECT
    r.role_id,
    'Seed User',
    'user@smartqueue.local',
    'user',
    NULL,
    '$2a$10$/QgmrLHf1vgbHtYmchkx5.CoKr4AG4i5ifKm2tdGfaW8jQ72hdsYe',
    TRUE,
    FALSE,
    TRUE,
    NOW()
FROM roles r
WHERE r.name = 'USER'
  AND NOT EXISTS (
      SELECT 1 FROM users u
      WHERE LOWER(u.email) = 'user@smartqueue.local'
         OR LOWER(u.username) = 'user'
  );
