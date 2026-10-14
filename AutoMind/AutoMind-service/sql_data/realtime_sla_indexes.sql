-- Run once before enabling AutoMind-simulator on a constrained database.
-- These indexes keep 5-minute realtime queries and retention cleanup bounded.

CREATE INDEX idx_telemetry_time ON telemetry_data (`time`);
CREATE INDEX idx_telemetry_tenant_link_time ON telemetry_data (tenant_id, link_id, `time`);

CREATE INDEX idx_sla_evaluated_at ON sla_record (evaluated_at);
CREATE INDEX idx_sla_tenant_link_time ON sla_record (tenant_id, link_id, evaluated_at);
