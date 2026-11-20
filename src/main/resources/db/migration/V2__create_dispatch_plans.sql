CREATE TABLE dispatch_plans (
    plan_id                   VARCHAR(36)              NOT NULL PRIMARY KEY,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    strategy                  VARCHAR(64)              NOT NULL,
    total_orders              INTEGER                  NOT NULL,
    assigned_orders           INTEGER                  NOT NULL,
    unassigned_orders         INTEGER                  NOT NULL,
    total_distance_km         DOUBLE PRECISION         NOT NULL,
    fleet_utilization_percent DOUBLE PRECISION         NOT NULL,
    computation_millis        BIGINT                   NOT NULL,
    plan_json                 TEXT                     NOT NULL
);

CREATE INDEX idx_dispatch_plans_created_at ON dispatch_plans (created_at);
