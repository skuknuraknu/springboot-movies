ALTER TABLE tb_refresh_tokens
ADD COLUMN last_used TIMESTAMP,
ADD COLUMN rotation_count BIGINT;