CREATE TABLE app_users (
    id       UUID         PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL
);

CREATE TABLE orders (
    id            UUID           PRIMARY KEY,
    customer_name VARCHAR(255)   NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(255)   NOT NULL,
    created_at    TIMESTAMP      NOT NULL
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);
