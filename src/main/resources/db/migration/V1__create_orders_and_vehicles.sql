CREATE TABLE delivery_orders (
    order_id       VARCHAR(64)      NOT NULL PRIMARY KEY,
    latitude       DOUBLE PRECISION NOT NULL,
    longitude      DOUBLE PRECISION NOT NULL,
    address        VARCHAR(255)     NOT NULL,
    package_weight DOUBLE PRECISION NOT NULL,
    priority       VARCHAR(16)      NOT NULL
);

CREATE INDEX idx_delivery_orders_priority ON delivery_orders (priority);

CREATE TABLE vehicles (
    vehicle_id        VARCHAR(64)      NOT NULL PRIMARY KEY,
    capacity          DOUBLE PRECISION NOT NULL,
    current_latitude  DOUBLE PRECISION NOT NULL,
    current_longitude DOUBLE PRECISION NOT NULL,
    current_address   VARCHAR(255)     NOT NULL
);
