DROP TABLE IF EXISTS a2a_message;
DROP TABLE IF EXISTS agent_task;
DROP TABLE IF EXISTS approval_record;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS healing_record;
DROP TABLE IF EXISTS intent_request;
DROP TABLE IF EXISTS mcp_call_log;
DROP TABLE IF EXISTS network_domain;
DROP TABLE IF EXISTS network_link;
DROP TABLE IF EXISTS network_link_resource;
DROP TABLE IF EXISTS network_node;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS sla_record;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(32),
  password VARCHAR(64),
  status VARCHAR(10),
  identity VARCHAR(16)
);

CREATE TABLE person (
  user_id INT,
  position VARCHAR(128),
  tenant_id VARCHAR(64),
  name VARCHAR(64),
  sex VARCHAR(5),
  introduce VARCHAR(128),
  label VARCHAR(64)
);

CREATE TABLE intent_request (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64),
  user_id INT,
  input_text CLOB,
  parsed_intent CLOB,
  priority INT,
  status VARCHAR(32) DEFAULT 'SUBMITTED',
  trace_id VARCHAR(64),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_intent_trace_id ON intent_request(trace_id);

CREATE TABLE audit_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64),
  actor_id INT,
  actor_role VARCHAR(128),
  action VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE agent_task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64) NOT NULL,
  intent_id INT,
  parent_task_id BIGINT,
  trace_id VARCHAR(64) NOT NULL,
  agent_role VARCHAR(32) NOT NULL,
  task_type VARCHAR(64),
  pev_state VARCHAR(16) NOT NULL,
  confidence DECIMAL(4, 3),
  input_payload CLOB,
  output_payload CLOB,
  error_code VARCHAR(64),
  retry_count INT NOT NULL DEFAULT 0,
  started_at TIMESTAMP,
  finished_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_agent_task_trace ON agent_task(trace_id);
CREATE INDEX idx_agent_task_intent ON agent_task(intent_id);

CREATE TABLE approval_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64) NOT NULL,
  intent_id INT,
  agent_task_id BIGINT,
  trace_id VARCHAR(64),
  approver_id INT,
  approval_type VARCHAR(32) NOT NULL,
  risk_level VARCHAR(16),
  ai_recommendation CLOB,
  submitted_payload CLOB,
  modified_payload CLOB,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  decision_reason VARCHAR(512),
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  decided_at TIMESTAMP,
  expire_at TIMESTAMP
);
CREATE INDEX idx_approval_trace ON approval_record(trace_id);
CREATE INDEX idx_approval_intent_type_status ON approval_record(intent_id, approval_type, status);

CREATE TABLE mcp_call_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64) NOT NULL,
  agent_task_id BIGINT,
  intent_id INT,
  trace_id VARCHAR(64) NOT NULL,
  tool_name VARCHAR(128) NOT NULL,
  tool_version VARCHAR(32),
  request_params CLOB,
  response_code INT,
  response_body CLOB,
  error_type VARCHAR(32),
  latency_ms INT,
  attempt_no INT NOT NULL DEFAULT 1,
  is_high_risk BOOLEAN NOT NULL DEFAULT FALSE,
  called_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE a2a_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64) NOT NULL,
  source_domain_id VARCHAR(64),
  target_domain_id VARCHAR(64),
  source_agent VARCHAR(64),
  target_agent VARCHAR(64),
  task_id BIGINT,
  trace_id VARCHAR(64) NOT NULL,
  message_type VARCHAR(16) NOT NULL,
  payload CLOB,
  status VARCHAR(16),
  latency_ms INT,
  sent_at TIMESTAMP,
  received_at TIMESTAMP
);

CREATE TABLE sla_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64) NOT NULL,
  intent_id INT,
  link_id VARCHAR(64),
  metric_type VARCHAR(32) NOT NULL,
  target_value DECIMAL(12, 4),
  observed_value DECIMAL(12, 4),
  breach_flag BOOLEAN NOT NULL DEFAULT FALSE,
  evaluation_window VARCHAR(32),
  evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE healing_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id VARCHAR(64) NOT NULL,
  intent_id INT,
  trigger_agent_task_id BIGINT,
  trace_id VARCHAR(64),
  trigger_source VARCHAR(32) NOT NULL,
  root_cause VARCHAR(512),
  attempt_no INT NOT NULL DEFAULT 1,
  max_attempts INT NOT NULL DEFAULT 3,
  strategy VARCHAR(64),
  result VARCHAR(32),
  recovered_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE network_link_resource (
  id VARCHAR(36) PRIMARY KEY,
  tenant_id VARCHAR(64),
  link_id VARCHAR(36),
  qos_profile VARCHAR(32),
  priority_queue VARCHAR(16),
  available_bandwidth_mbps DECIMAL(10, 2),
  current_latency_ms DECIMAL(8, 2),
  current_packet_loss_rate DECIMAL(5, 4),
  current_jitter_ms DECIMAL(8, 2),
  is_anomaly BOOLEAN,
  sampled_at TIMESTAMP
);

CREATE TABLE network_domain (
  id VARCHAR(36) PRIMARY KEY,
  tenant_id VARCHAR(64),
  domain_code VARCHAR(64),
  domain_name VARCHAR(128),
  controller_type VARCHAR(32),
  controller_endpoint VARCHAR(255),
  status VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE network_node (
  id VARCHAR(36) PRIMARY KEY,
  tenant_id VARCHAR(64),
  domain_id VARCHAR(36),
  node_code VARCHAR(64),
  node_name VARCHAR(128),
  node_type VARCHAR(32),
  location VARCHAR(128),
  management_ip VARCHAR(64),
  status VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE network_link (
  id VARCHAR(36) PRIMARY KEY,
  tenant_id VARCHAR(64),
  link_code VARCHAR(64),
  link_name VARCHAR(128),
  source_node_id VARCHAR(36),
  target_node_id VARCHAR(36),
  link_type VARCHAR(32),
  total_bandwidth_mbps DECIMAL(10, 2),
  reserved_bandwidth_mbps DECIMAL(10, 2),
  max_latency_ms DECIMAL(8, 2),
  status VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user(user_id, username, password, status, identity) VALUES
  (1, 'admin', 'x', 'ACTIVE', 'ADMIN'),
  (10001, 'operator', 'x', 'ACTIVE', 'OPERATOR');

INSERT INTO person(user_id, position, tenant_id, name, label) VALUES
  (1, 'super admin', 'admin', 'admin', 'admin'),
  (10001, 'tenant operator', 'tenant-001', 'operator', 'operator');

INSERT INTO network_link_resource(id, tenant_id, link_id, qos_profile, priority_queue, available_bandwidth_mbps, current_latency_ms, current_packet_loss_rate, current_jitter_ms, is_anomaly, sampled_at)
VALUES ('res-1', 'tenant-001', 'link-1', 'DEFAULT', 'P3', 1000.00, 10.00, 0.0010, 1.00, FALSE, CURRENT_TIMESTAMP);

INSERT INTO network_domain(id, tenant_id, domain_code, domain_name, controller_type, controller_endpoint, status)
VALUES ('domain-remote', 'tenant-001', 'remote', 'remote domain', 'MOCK', 'http://localhost:0', 'ACTIVE');

INSERT INTO network_node(id, tenant_id, domain_id, node_code, node_name, node_type, location, management_ip, status)
VALUES
  ('node-a', 'tenant-001', 'domain-remote', 'A', 'node-a', 'SWITCH', 'lab', '10.0.0.1', 'ACTIVE'),
  ('node-b', 'tenant-001', 'domain-remote', 'B', 'node-b', 'SWITCH', 'lab', '10.0.0.2', 'ACTIVE');

INSERT INTO network_link(id, tenant_id, link_code, link_name, source_node_id, target_node_id, link_type, total_bandwidth_mbps, reserved_bandwidth_mbps, max_latency_ms, status)
VALUES ('link-1', 'tenant-001', 'L1', 'link-1', 'node-a', 'node-b', 'PRIMARY', 1000.00, 0.00, 20.00, 'ACTIVE');
