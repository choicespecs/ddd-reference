-- Schema is owned by Flyway. Never edit an applied migration; add V3__..., V4__... instead.
-- One table group per bounded context, prefixed with the context name.

-- ===== Catalog context =====
CREATE TABLE catalog_product (
    id              UUID            PRIMARY KEY,
    sku             VARCHAR(32)     NOT NULL UNIQUE,
    name            VARCHAR(200)    NOT NULL,
    description     VARCHAR(2000),
    price_amount    NUMERIC(19, 2)  NOT NULL,
    price_currency  VARCHAR(3)      NOT NULL,
    stock_on_hand   INTEGER         NOT NULL CHECK (stock_on_hand >= 0),
    active          BOOLEAN         NOT NULL,
    version         BIGINT          NOT NULL
);

-- ===== Ordering context =====
CREATE TABLE ordering_order (
    id              UUID            PRIMARY KEY,
    customer_email  VARCHAR(320)    NOT NULL,
    status          VARCHAR(16)     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    placed_at       TIMESTAMP WITH TIME ZONE,
    version         BIGINT          NOT NULL
);

CREATE TABLE ordering_order_line (
    id                   UUID            PRIMARY KEY,
    order_id             UUID            NOT NULL REFERENCES ordering_order (id) ON DELETE CASCADE,
    -- product_id is a reference BY ID to another context: deliberately NO foreign key to catalog_product.
    product_id           UUID            NOT NULL,
    product_name         VARCHAR(200)    NOT NULL,
    unit_price_amount    NUMERIC(19, 2)  NOT NULL,
    unit_price_currency  VARCHAR(3)      NOT NULL,
    quantity             INTEGER         NOT NULL CHECK (quantity BETWEEN 1 AND 99)
);

CREATE INDEX idx_order_line_order ON ordering_order_line (order_id);
CREATE INDEX idx_order_created_at ON ordering_order (created_at DESC);
