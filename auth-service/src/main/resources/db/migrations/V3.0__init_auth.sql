CREATE TABLE tb_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);
-- Create tb_roles table
CREATE TABLE tb_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Create user_roles junction table
CREATE TABLE tb_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE
);
-- Create refresh_tokens table
CREATE TABLE tb_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);
-- Create password_reset_tokens table
CREATE TABLE tb_password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_used BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);
-- Create email_verification_tokens table
CREATE TABLE tb_email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_used BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);
-- Create login_attempts table for rate limiting
CREATE TABLE tb_login_attempts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT false
);
-- Create indexes for performance
CREATE INDEX idx_users_email ON tb_users(email);
CREATE INDEX idx_users_active ON tb_users(is_active);
CREATE INDEX idx_users_created_at ON tb_users(created_at);
CREATE INDEX idx_refresh_tokens_user_id ON tb_refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON tb_refresh_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_token ON tb_password_reset_tokens(token);
CREATE INDEX idx_email_verification_tokens_token ON tb_email_verification_tokens(token);
CREATE INDEX idx_login_attempts_email ON tb_login_attempts(email);
CREATE INDEX idx_login_attempts_ip_time ON tb_login_attempts(ip_address, attempt_time);

INSERT INTO tb_roles (name, description) VALUES
    ('USER', 'Regular user with basic permissions'),
    ('ADMIN', 'Administrator with full permissions'),
    ('MODERATOR', 'Moderator with limited admin permissions');

-- Create trigger to update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON tb_users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();