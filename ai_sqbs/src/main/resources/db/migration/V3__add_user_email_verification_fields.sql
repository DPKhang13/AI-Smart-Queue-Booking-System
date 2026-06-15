ALTER TABLE users
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN email_verified_at TIMESTAMPTZ,
ADD COLUMN email_verification_otp_hash CHAR(64),
ADD COLUMN email_verification_otp_expires_at TIMESTAMPTZ,
ADD COLUMN email_verification_otp_sent_at TIMESTAMPTZ,
ADD COLUMN email_verification_attempt_count INT NOT NULL DEFAULT 0;

ALTER TABLE users
ADD CONSTRAINT chk_users_email_verification_attempt_count
CHECK (email_verification_attempt_count >= 0);

ALTER TABLE users
ADD CONSTRAINT chk_users_email_verified_at
CHECK (
    email_verified = FALSE
    OR (email_verified = TRUE AND email_verified_at IS NOT NULL)
);

CREATE INDEX idx_users_email_verified
ON users(email_verified);

CREATE INDEX idx_users_email_verification_otp_expires_at
ON users(email_verification_otp_expires_at);
