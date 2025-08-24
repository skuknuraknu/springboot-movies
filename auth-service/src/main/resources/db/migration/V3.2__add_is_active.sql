ALTER TABLE tb_roles
ADD is_active BOOLEAN DEFAULT true;

ALTER TABLE tb_user_roles
ADD is_active BOOLEAN DEFAULT true;

ALTER TABLE tb_refresh_tokens
ADD is_active BOOLEAN DEFAULT true;

ALTER TABLE tb_password_reset_tokens
ADD is_active BOOLEAN DEFAULT true;

ALTER TABLE tb_email_verification_tokens
ADD is_active BOOLEAN DEFAULT true;

ALTER TABLE tb_login_attempts
ADD is_active BOOLEAN DEFAULT true;
