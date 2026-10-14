-- AutoMind realistic seed data for PEV / MCP / A2A / SLA integration tests.
-- Execute after clearing these tables:
-- network_domain, network_node, network_link, network_link_resource,
-- network_resource_reservation, telemetry_data, sla_record.

INSERT INTO `network_domain`
(`id`, `tenant_id`, `domain_code`, `domain_name`, `controller_type`, `controller_endpoint`, `status`, `created_at`, `updated_at`)
VALUES
('10000000-0000-0000-0000-000000000101', 'tenant-001', 'BJ-CAMPUS-DOMAIN', '北京总部办公域', 'RYU', 'http://localhost:9090', 'ACTIVE', NOW(), NOW()),
('10000000-0000-0000-0000-000000000102', 'tenant-001', 'SH-POP-DOMAIN', '上海会议接入域', 'ONOS', 'http://localhost:9090', 'ACTIVE', NOW(), NOW()),
('10000000-0000-0000-0000-000000000103', 'tenant-001', 'HZ-DR-DOMAIN', '杭州灾备中转域', 'RYU', 'http://localhost:9090', 'ACTIVE', NOW(), NOW()),
('10000000-0000-0000-0000-000000000201', 'tenant-002', 'SH-FINANCE-DOMAIN', '上海金融生产域', 'ONOS', 'http://localhost:9090', 'ACTIVE', NOW(), NOW()),
('10000000-0000-0000-0000-000000000202', 'tenant-002', 'GZ-FINANCE-DR-DOMAIN', '广州金融灾备域', 'RYU', 'http://localhost:9090', 'DEGRADED', NOW(), NOW()),
('10000000-0000-0000-0000-000000000301', 'tenant-003', 'BJ-EDGE-DOMAIN', '北京边缘接入域', 'RYU', 'http://localhost:9090', 'ACTIVE', NOW(), NOW()),
('10000000-0000-0000-0000-000000000302', 'tenant-003', 'XA-REMOTE-DOMAIN', '西安远端故障演练域', 'ONOS', 'http://192.0.2.10:9090', 'DEGRADED', NOW(), NOW()),
('8e84bcbb-5025-11f1-8b97-00163e254a5b', 'admin', 'ADMIN-DOMAIN', '管理员全局视图', NULL, NULL, 'ACTIVE', NOW(), NOW());

INSERT INTO `network_node`
(`id`, `tenant_id`, `domain_id`, `node_code`, `node_name`, `node_type`, `location`, `management_ip`, `status`, `created_at`, `updated_at`)
VALUES
('20000000-0000-0000-0000-000000000101', 'tenant-001', '10000000-0000-0000-0000-000000000101', 'BJ-CORE-01', '北京总部核心交换机 01', 'SWITCH', 'Beijing', '10.10.1.11', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000102', 'tenant-001', '10000000-0000-0000-0000-000000000101', 'BJ-MEETING-EDGE-01', '北京视频会议接入交换机 01', 'SWITCH', 'Beijing', '10.10.1.21', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000103', 'tenant-001', '10000000-0000-0000-0000-000000000102', 'SH-CORE-01', '上海会议核心交换机 01', 'SWITCH', 'Shanghai', '10.20.1.11', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000104', 'tenant-001', '10000000-0000-0000-0000-000000000102', 'SH-MEETING-EDGE-01', '上海视频会议接入交换机 01', 'SWITCH', 'Shanghai', '10.20.1.21', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000105', 'tenant-001', '10000000-0000-0000-0000-000000000103', 'HZ-DR-CORE-01', '杭州灾备中转交换机 01', 'SWITCH', 'Hangzhou', '10.30.1.11', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000201', 'tenant-002', '10000000-0000-0000-0000-000000000201', 'SH-FIN-CORE-01', '上海金融核心交换机 01', 'SWITCH', 'Shanghai', '10.40.1.11', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000202', 'tenant-002', '10000000-0000-0000-0000-000000000202', 'GZ-FIN-DR-01', '广州金融灾备交换机 01', 'SWITCH', 'Guangzhou', '10.50.1.11', 'WARN', NOW(), NOW()),
('20000000-0000-0000-0000-000000000301', 'tenant-003', '10000000-0000-0000-0000-000000000301', 'BJ-EDGE-01', '北京边缘接入交换机 01', 'SWITCH', 'Beijing', '10.60.1.11', 'UP', NOW(), NOW()),
('20000000-0000-0000-0000-000000000302', 'tenant-003', '10000000-0000-0000-0000-000000000302', 'XA-REMOTE-01', '西安远端故障演练交换机 01', 'SWITCH', 'Xian', '10.70.1.11', 'WARN', NOW(), NOW());

INSERT INTO `network_link`
(`id`, `tenant_id`, `link_code`, `link_name`, `source_node_id`, `target_node_id`, `link_type`, `total_bandwidth_mbps`, `reserved_bandwidth_mbps`, `max_latency_ms`, `status`, `created_at`, `updated_at`)
VALUES
('30000000-0000-0000-0000-000000000101', 'tenant-001', 'bj-sh-video-primary', '北京-上海视频会议主用专线', '20000000-0000-0000-0000-000000000102', '20000000-0000-0000-0000-000000000104', 'WAN_PRIMARY', 1000.00, 260.00, 40.00, 'UP', NOW(), NOW()),
('30000000-0000-0000-0000-000000000102', 'tenant-001', 'bj-sh-video-backup', '北京-上海视频会议备用专线', '20000000-0000-0000-0000-000000000101', '20000000-0000-0000-0000-000000000103', 'WAN_BACKUP', 800.00, 180.00, 55.00, 'UP', NOW(), NOW()),
('30000000-0000-0000-0000-000000000103', 'tenant-001', 'bj-hz-dr-relay', '北京-杭州灾备中转链路', '20000000-0000-0000-0000-000000000101', '20000000-0000-0000-0000-000000000105', 'WAN_BACKUP', 600.00, 490.00, 80.00, 'UP', NOW(), NOW()),
('30000000-0000-0000-0000-000000000104', 'tenant-001', 'hz-sh-dr-relay', '杭州-上海灾备中转链路', '20000000-0000-0000-0000-000000000105', '20000000-0000-0000-0000-000000000103', 'WAN_BACKUP', 600.00, 450.00, 80.00, 'UP', NOW(), NOW()),
('30000000-0000-0000-0000-000000000105', 'tenant-001', 'bj-sh-congested-test', '北京-上海拥塞演练链路', '20000000-0000-0000-0000-000000000102', '20000000-0000-0000-0000-000000000103', 'WAN_PRIMARY', 300.00, 220.00, 40.00, 'DEGRADED', NOW(), NOW()),
('30000000-0000-0000-0000-000000000201', 'tenant-002', 'sh-gz-finance-primary', '上海-广州金融生产专线', '20000000-0000-0000-0000-000000000201', '20000000-0000-0000-0000-000000000202', 'WAN_PRIMARY', 500.00, 420.00, 45.00, 'DEGRADED', NOW(), NOW()),
('30000000-0000-0000-0000-000000000301', 'tenant-003', 'bj-xa-a2a-failover', '北京-西安 A2A 降级演练链路', '20000000-0000-0000-0000-000000000301', '20000000-0000-0000-0000-000000000302', 'WAN_PRIMARY', 400.00, 100.00, 50.00, 'UP', NOW(), NOW());

INSERT INTO `network_link_resource`
(`id`, `tenant_id`, `link_id`, `qos_profile`, `priority_queue`, `available_bandwidth_mbps`, `current_latency_ms`, `current_packet_loss_rate`, `current_jitter_ms`, `is_anomaly`, `sampled_at`)
VALUES
('40000000-0000-0000-0000-000000000101', 'tenant-001', '30000000-0000-0000-0000-000000000101', 'VIDEO_LOW_LATENCY', 'HIGH', 740.00, 28.60, 0.0012, 3.10, 0, NOW()),
('40000000-0000-0000-0000-000000000102', 'tenant-001', '30000000-0000-0000-0000-000000000102', 'VIDEO_BACKUP', 'MEDIUM', 620.00, 42.40, 0.0018, 4.20, 0, NOW()),
('40000000-0000-0000-0000-000000000103', 'tenant-001', '30000000-0000-0000-0000-000000000103', 'DR_RELAY', 'MEDIUM', 110.00, 74.00, 0.0070, 8.50, 0, NOW()),
('40000000-0000-0000-0000-000000000104', 'tenant-001', '30000000-0000-0000-0000-000000000104', 'DR_RELAY', 'MEDIUM', 150.00, 69.50, 0.0065, 7.90, 0, NOW()),
('40000000-0000-0000-0000-000000000105', 'tenant-001', '30000000-0000-0000-0000-000000000105', 'BEST_EFFORT', 'LOW', 80.00, 67.20, 0.0250, 15.40, 1, NOW()),
('40000000-0000-0000-0000-000000000201', 'tenant-002', '30000000-0000-0000-0000-000000000201', 'FINANCE_SETTLEMENT', 'HIGH', 80.00, 58.40, 0.0120, 11.60, 1, NOW()),
('40000000-0000-0000-0000-000000000301', 'tenant-003', '30000000-0000-0000-0000-000000000301', 'CROSS_DOMAIN_TEST', 'MEDIUM', 280.00, 34.50, 0.0020, 5.20, 0, NOW());

INSERT INTO `network_resource_reservation`
(`id`, `tenant_id`, `intent_request_id`, `link_id`, `reserved_bandwidth_mbps`, `target_latency_ms`, `target_packet_loss_rate`, `priority`, `reservation_status`, `effective_start`, `effective_end`, `created_at`, `updated_at`)
VALUES
('50000000-0000-0000-0000-000000000101', 'tenant-001', NULL, '30000000-0000-0000-0000-000000000101', 180.00, 40.00, 0.0050, 2, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 6 HOUR), NOW(), NOW()),
('50000000-0000-0000-0000-000000000102', 'tenant-001', NULL, '30000000-0000-0000-0000-000000000102', 100.00, 55.00, 0.0050, 3, 'PLANNED', DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), NOW(), NOW()),
('50000000-0000-0000-0000-000000000103', 'tenant-001', NULL, '30000000-0000-0000-0000-000000000105', 160.00, 40.00, 0.0050, 1, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR), NOW(), NOW()),
('50000000-0000-0000-0000-000000000201', 'tenant-002', NULL, '30000000-0000-0000-0000-000000000201', 420.00, 45.00, 0.0050, 1, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), NOW(), NOW());

INSERT INTO `telemetry_data`
(`time`, `tenant_id`, `link_id`, `link_code`, `latency_ms`, `packet_loss_rate`, `bandwidth_usage`, `jitter_ms`, `is_anomaly`)
VALUES
(DATE_SUB(NOW(), INTERVAL 4 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000101', 'bj-sh-video-primary', 28.90, 0.0012, 31.20, 3.00, 0),
(DATE_SUB(NOW(), INTERVAL 3 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000101', 'bj-sh-video-primary', 28.40, 0.0011, 32.00, 3.20, 0),
(DATE_SUB(NOW(), INTERVAL 2 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000101', 'bj-sh-video-primary', 29.10, 0.0013, 32.80, 3.30, 0),
(DATE_SUB(NOW(), INTERVAL 1 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000101', 'bj-sh-video-primary', 28.60, 0.0012, 33.10, 3.10, 0),
(DATE_SUB(NOW(), INTERVAL 4 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000102', 'bj-sh-video-backup', 42.10, 0.0018, 22.70, 4.10, 0),
(DATE_SUB(NOW(), INTERVAL 2 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000102', 'bj-sh-video-backup', 42.70, 0.0019, 23.20, 4.30, 0),
(DATE_SUB(NOW(), INTERVAL 3 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000105', 'bj-sh-congested-test', 66.80, 0.0240, 84.60, 15.00, 1),
(DATE_SUB(NOW(), INTERVAL 1 MINUTE), 'tenant-001', '30000000-0000-0000-0000-000000000105', 'bj-sh-congested-test', 68.10, 0.0260, 86.20, 15.80, 1),
(DATE_SUB(NOW(), INTERVAL 4 MINUTE), 'tenant-002', '30000000-0000-0000-0000-000000000201', 'sh-gz-finance-primary', 57.90, 0.0110, 88.40, 11.20, 1),
(DATE_SUB(NOW(), INTERVAL 2 MINUTE), 'tenant-002', '30000000-0000-0000-0000-000000000201', 'sh-gz-finance-primary', 58.80, 0.0130, 89.10, 12.00, 1),
(DATE_SUB(NOW(), INTERVAL 3 MINUTE), 'tenant-003', '30000000-0000-0000-0000-000000000301', 'bj-xa-a2a-failover', 34.20, 0.0020, 30.00, 5.00, 0),
(DATE_SUB(NOW(), INTERVAL 1 MINUTE), 'tenant-003', '30000000-0000-0000-0000-000000000301', 'bj-xa-a2a-failover', 34.80, 0.0021, 31.40, 5.30, 0);

INSERT INTO `sla_record`
(`tenant_id`, `intent_id`, `link_id`, `metric_type`, `target_value`, `observed_value`, `breach_flag`, `evaluation_window`, `evaluated_at`)
VALUES
('tenant-001', NULL, '30000000-0000-0000-0000-000000000101', 'LATENCY', 40.0000, 28.7500, 0, '5m', NOW(3)),
('tenant-001', NULL, '30000000-0000-0000-0000-000000000101', 'LOSS', 0.0050, 0.0012, 0, '5m', NOW(3)),
('tenant-001', NULL, '30000000-0000-0000-0000-000000000102', 'LATENCY', 55.0000, 42.4000, 0, '5m', NOW(3)),
('tenant-001', NULL, '30000000-0000-0000-0000-000000000105', 'LATENCY', 40.0000, 67.4500, 1, '5m', NOW(3)),
('tenant-001', NULL, '30000000-0000-0000-0000-000000000105', 'LOSS', 0.0050, 0.0250, 1, '5m', NOW(3)),
('tenant-002', NULL, '30000000-0000-0000-0000-000000000201', 'LATENCY', 45.0000, 58.3500, 1, '5m', NOW(3)),
('tenant-002', NULL, '30000000-0000-0000-0000-000000000201', 'LOSS', 0.0050, 0.0120, 1, '5m', NOW(3)),
('tenant-003', NULL, '30000000-0000-0000-0000-000000000301', 'LATENCY', 50.0000, 34.5000, 0, '5m', NOW(3));
