-- Drop composite PK
ALTER TABLE tb_user_roles DROP CONSTRAINT tb_user_roles_pkey;

-- Add id column
ALTER TABLE tb_user_roles
    ADD COLUMN id BIGSERIAL PRIMARY KEY;

-- Add unique constraint
ALTER TABLE tb_user_roles
    ADD CONSTRAINT uq_user_role UNIQUE (user_id, role_id);
