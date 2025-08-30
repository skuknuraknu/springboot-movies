CREATE TABLE tb_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    price DECIMAL(10, 0) NOT NULL,
    max_request INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED','EXPIRED')),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan FOREIGN KEY (plan_id) REFERENCES tb_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_subscriptions_user_id ON tb_subscriptions(user_id);
CREATE INDEX idx_subscriptions_plan_id ON tb_subscriptions(plan_id);

INSERT INTO tb_plans ( name, description, price, max_request ) VALUES
    ('Free', 'Basic plan with limited features', 0, 50),
    ('Pro', 'Advanced plan with more features', 1000, 100),
    ('Enterprise', 'Full-featured plan for businesses', 2000, 250);
