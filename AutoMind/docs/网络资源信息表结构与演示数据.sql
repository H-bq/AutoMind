-- 网络资源信息存储表结构与演示数据
-- 依据：项目文档.md、开发者文档.md
-- 范围：跨域拓扑、网络节点、链路资源、资源预留、遥测数据。

-- network_domain：用于项目中的跨域拓扑管理、A2A 跨域协同和 MCP 控制器域路由，标识每个模拟 SDN 控制器管理的网络域。
CREATE TABLE network_domain (
                                id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()) COMMENT '网络域唯一标识',
                                tenant_id VARCHAR(64) COMMENT '租户标识，用于多租户隔离',
                                domain_code VARCHAR(64) COMMENT '网络域编码',
                                domain_name VARCHAR(128) COMMENT '网络域名称',
                                controller_type VARCHAR(32) COMMENT '控制器类型，如 RYU 或 ONOS',
                                controller_endpoint VARCHAR(255) COMMENT '模拟 SDN 控制器访问地址',
                                status VARCHAR(32) COMMENT '网络域状态',
                                created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='网络域与控制器域信息表';

INSERT INTO network_domain (
    id, tenant_id, domain_code, domain_name, controller_type, controller_endpoint, status, created_at, updated_at
) VALUES
('10000000-0000-0000-0000-000000000001', 'tenant-001', 'BJ-DOMAIN', '北京办公域', 'RYU', 'http://ryu-bj-sim:8080', 'ACTIVE', '2026-05-08 09:00:00', '2026-05-08 09:00:00'),
('10000000-0000-0000-0000-000000000002', 'tenant-001', 'SH-DOMAIN', '上海办公域', 'ONOS', 'http://onos-sh-sim:8181', 'ACTIVE', '2026-05-08 09:00:00', '2026-05-08 09:00:00'),
('10000000-0000-0000-0000-000000000003', 'tenant-001', 'GZ-DOMAIN', '广州办公域', 'RYU', 'http://ryu-gz-sim:8080', 'DEGRADED', '2026-05-08 09:00:00', '2026-05-08 09:00:00');



-- network_node：用于项目中的 B 端全局拓扑展示和 Planner Agent 路径规划，存储各网络域内的交换机等网络节点。
CREATE TABLE network_node (
                              id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()) COMMENT '网络节点唯一标识',
                              tenant_id VARCHAR(64) COMMENT '租户标识，用于多租户隔离',
                              domain_id VARCHAR(36) COMMENT '所属网络域标识',
                              node_code VARCHAR(64) COMMENT '网络节点编码',
                              node_name VARCHAR(128) COMMENT '网络节点名称',
                              node_type VARCHAR(32) COMMENT '网络节点类型，如 SWITCH',
                              location VARCHAR(128) COMMENT '节点所在位置',
                              management_ip VARCHAR(64) COMMENT '节点管理 IP',
                              status VARCHAR(32) COMMENT '节点运行状态',
                              created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='网络节点信息表';

INSERT INTO network_node (
    id, tenant_id, domain_id, node_code, node_name, node_type, location, management_ip, status, created_at, updated_at
) VALUES
('20000000-0000-0000-0000-000000000001', 'tenant-001', '10000000-0000-0000-0000-000000000001', 'BJ-SW-CORE-01', '北京核心交换机 01', 'SWITCH', 'Beijing', '10.10.1.11', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00'),
('20000000-0000-0000-0000-000000000002', 'tenant-001', '10000000-0000-0000-0000-000000000002', 'SH-SW-CORE-01', '上海核心交换机 01', 'SWITCH', 'Shanghai', '10.20.1.11', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00'),
('20000000-0000-0000-0000-000000000003', 'tenant-001', '10000000-0000-0000-0000-000000000003', 'GZ-SW-EDGE-01', '广州边缘交换机 01', 'SWITCH', 'Guangzhou', '10.30.1.21', 'WARN', '2026-05-08 09:05:00', '2026-05-08 09:05:00');

-- network_link：用于项目中的拓扑链路展示、SLA 校验、路径规划和 MCP 策略下发，存储节点之间的链路容量与状态。
CREATE TABLE network_link (
                              id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()) COMMENT '网络链路唯一标识',
                              tenant_id VARCHAR(64) COMMENT '租户标识，用于多租户隔离',
                              link_code VARCHAR(64) COMMENT '链路编码',
                              link_name VARCHAR(128) COMMENT '链路名称',
                              source_node_id VARCHAR(36) COMMENT '源节点标识',
                              target_node_id VARCHAR(36) COMMENT '目标节点标识',
                              link_type VARCHAR(32) COMMENT '链路类型，如主链路或备用链路',
                              total_bandwidth_mbps DECIMAL(10,2) COMMENT '链路总带宽，单位 Mbps',
                              reserved_bandwidth_mbps DECIMAL(10,2) COMMENT '已预留带宽，单位 Mbps',
                              max_latency_ms DECIMAL(8,2) COMMENT '链路目标最大时延，单位毫秒',
                              status VARCHAR(32) COMMENT '链路运行状态',
                              created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='网络链路拓扑与基础容量信息表';

INSERT INTO network_link (
    id, tenant_id, link_code, link_name, source_node_id, target_node_id, link_type, total_bandwidth_mbps, reserved_bandwidth_mbps, max_latency_ms, status, created_at, updated_at
) VALUES
('30000000-0000-0000-0000-000000000001', 'tenant-001', 'bj-sh-link-01', '北京-上海主链路', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'WAN_PRIMARY', 1000.00, 320.00, 50.00, 'UP', '2026-05-08 09:10:00', '2026-05-08 09:10:00'),
('30000000-0000-0000-0000-000000000002', 'tenant-001', 'bj-sh-link-02', '北京-上海备用链路', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'WAN_BACKUP', 500.00, 80.00, 80.00, 'UP', '2026-05-08 09:10:00', '2026-05-08 09:10:00'),
('30000000-0000-0000-0000-000000000003', 'tenant-001', 'gz-bj-link-01', '广州-北京跨域链路', '20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', 'WAN_PRIMARY', 800.00, 640.00, 90.00, 'DEGRADED', '2026-05-08 09:10:00', '2026-05-08 09:10:00');

-- network_link_resource：用于项目中的意图校验、资源余量判断和 QoS 状态展示，存储链路当前可用资源与异常状态。
CREATE TABLE network_link_resource (
                                      id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()) COMMENT '链路资源状态唯一标识',
                                      tenant_id VARCHAR(64) COMMENT '租户标识，用于多租户隔离',
                                      link_id VARCHAR(36) COMMENT '关联链路标识',
                                      qos_profile VARCHAR(32) COMMENT 'QoS 策略档位',
                                      priority_queue VARCHAR(16) COMMENT '优先级队列',
                                      available_bandwidth_mbps DECIMAL(10,2) COMMENT '当前可用带宽，单位 Mbps',
                                      current_latency_ms DECIMAL(8,2) COMMENT '当前时延，单位毫秒',
                                      current_packet_loss_rate DECIMAL(5,4) COMMENT '当前丢包率',
                                      current_jitter_ms DECIMAL(8,2) COMMENT '当前抖动，单位毫秒',
                                      is_anomaly BOOLEAN COMMENT '是否处于异常状态',
                                      sampled_at datetime COMMENT '资源状态采样时间'
) COMMENT='链路当前资源与 QoS 状态表';

INSERT INTO network_link_resource (
    id, tenant_id, link_id, qos_profile, priority_queue, available_bandwidth_mbps, current_latency_ms, current_packet_loss_rate, current_jitter_ms, is_anomaly, sampled_at
) VALUES
('40000000-0000-0000-0000-000000000001', 'tenant-001', '30000000-0000-0000-0000-000000000001', 'VIDEO_LOW_LATENCY', 'HIGH', 680.00, 28.40, 0.0010, 3.20, FALSE, '2026-05-08 09:15:00'),
('40000000-0000-0000-0000-000000000002', 'tenant-001', '30000000-0000-0000-0000-000000000002', 'BEST_EFFORT', 'MEDIUM', 420.00, 42.60, 0.0020, 4.80, FALSE, '2026-05-08 09:15:00'),
('40000000-0000-0000-0000-000000000003', 'tenant-001', '30000000-0000-0000-0000-000000000003', 'VIDEO_LOW_LATENCY', 'HIGH', 160.00, 216.00, 0.0610, 18.50, TRUE, '2026-05-08 09:15:00');

-- network_resource_reservation：用于项目中的意图资源分配、冲突仲裁和执行追踪，记录每个意图请求占用的链路资源。
CREATE TABLE network_resource_reservation (
                                             id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()) COMMENT '资源预留唯一标识',
                                             tenant_id VARCHAR(64) COMMENT '租户标识，用于多租户隔离',
                                             intent_request_id VARCHAR(36) COMMENT '关联意图请求标识',
                                             link_id VARCHAR(36) COMMENT '关联链路标识',
                                             reserved_bandwidth_mbps DECIMAL(10,2) COMMENT '预留带宽，单位 Mbps',
                                             target_latency_ms DECIMAL(8,2) COMMENT '目标时延，单位毫秒',
                                             target_packet_loss_rate DECIMAL(5,4) COMMENT '目标丢包率',
                                             priority INT COMMENT '资源预留优先级，数值越小优先级越高',
                                             reservation_status VARCHAR(32) COMMENT '资源预留状态',
                                             effective_start datetime COMMENT '预留生效开始时间',
                                             effective_end datetime COMMENT '预留生效结束时间',
                                             created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='网络资源预留记录表';

INSERT INTO network_resource_reservation (
    id, tenant_id, intent_request_id, link_id, reserved_bandwidth_mbps, target_latency_ms, target_packet_loss_rate, priority, reservation_status, effective_start, effective_end, created_at, updated_at
) VALUES
('50000000-0000-0000-0000-000000000001', 'tenant-001', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 120.00, 50.00, 0.0050, 1, 'ACTIVE', '2026-05-08 10:00:00', '2026-05-08 11:00:00', '2026-05-08 09:20:00', '2026-05-08 09:20:00'),
('50000000-0000-0000-0000-000000000002', 'tenant-001', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 80.00, 80.00, 0.0100, 2, 'PLANNED', '2026-05-08 10:30:00', '2026-05-08 12:00:00', '2026-05-08 09:22:00', '2026-05-08 09:22:00'),
('50000000-0000-0000-0000-000000000003', 'tenant-001', '60000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', 160.00, 90.00, 0.0100, 1, 'DEGRADED', '2026-05-08 10:00:00', '2026-05-08 11:30:00', '2026-05-08 09:24:00', '2026-05-08 09:24:00');

-- telemetry_data：用于项目中的 Monitor Agent SLA 校验、异常检测和自愈触发，存储链路实时遥测指标。
CREATE TABLE telemetry_data (
                              time datetime NOT NULL COMMENT '遥测采集时间',
                              tenant_id VARCHAR(64) COMMENT '租户标识，用于多租户隔离',
                              link_id VARCHAR(36) COMMENT '关联链路标识',
                              link_code VARCHAR(64) COMMENT '链路编码冗余字段，便于查询展示',
                              latency_ms DECIMAL(8,2) COMMENT '链路时延，单位毫秒',
                              packet_loss_rate DECIMAL(5,4) COMMENT '链路丢包率',
                              bandwidth_usage DECIMAL(5,2) COMMENT '链路带宽使用率，单位百分比',
                              jitter_ms DECIMAL(8,2) COMMENT '链路抖动，单位毫秒',
                              is_anomaly BOOLEAN COMMENT '是否为异常遥测点'
) COMMENT='网络链路遥测数据表';

INSERT INTO telemetry_data (
    time, tenant_id, link_id, link_code, latency_ms, packet_loss_rate, bandwidth_usage, jitter_ms, is_anomaly
) VALUES
('2026-05-08 09:30:00+08', 'tenant-001', '30000000-0000-0000-0000-000000000001', 'bj-sh-link-01', 26.80, 0.0010, 41.50, 3.10, FALSE),
('2026-05-08 09:30:01+08', 'tenant-001', '30000000-0000-0000-0000-000000000002', 'bj-sh-link-02', 47.20, 0.0030, 28.00, 5.20, FALSE),
('2026-05-08 09:30:02+08', 'tenant-001', '30000000-0000-0000-0000-000000000003', 'gz-bj-link-01', 225.60, 0.0720, 86.40, 22.30, TRUE);


