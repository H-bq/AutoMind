/*
 Navicat Premium Dump SQL

 Source Server         : 阿里云远程
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : 47.117.156.128:3307
 Source Schema         : network2

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 27/05/2026 14:01:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for a2a_message
-- ----------------------------
DROP TABLE IF EXISTS `a2a_message`;
CREATE TABLE `a2a_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `source_domain_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'FK network_domain.id',
  `target_domain_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `source_agent` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `target_agent` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `task_id` bigint NULL DEFAULT NULL COMMENT 'FK agent_task.id',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `message_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'REQ/RESP/FAIL/TIMEOUT/DEGRADED',
  `payload` json NULL COMMENT '已脱敏',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `latency_ms` int NULL DEFAULT NULL,
  `sent_at` datetime(3) NULL DEFAULT NULL,
  `received_at` datetime(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_domain_id` ASC, `target_agent` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'A2A 跨域消息审计' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_task
-- ----------------------------
DROP TABLE IF EXISTS `agent_task`;
CREATE TABLE `agent_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户ID',
  `intent_id` int NULL DEFAULT NULL COMMENT '关联 intent_request.id',
  `parent_task_id` bigint NULL DEFAULT NULL COMMENT '父任务ID（自关联）',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '全链路 TraceId',
  `agent_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SUPERVISOR/PLANNER/EXECUTOR/MONITOR',
  `task_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务子类型',
  `pev_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PLAN/EXECUTE/VERIFY/RECOVER/COMPLETED/FAILED',
  `confidence` decimal(4, 3) NULL DEFAULT NULL COMMENT '0~1，置信度',
  `input_payload` json NULL,
  `output_payload` json NULL,
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT 0,
  `started_at` datetime(3) NULL DEFAULT NULL,
  `finished_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_intent_id`(`intent_id` ASC) USING BTREE,
  INDEX `idx_tenant_state`(`tenant_id` ASC, `pev_state` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent 任务记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for approval_record
-- ----------------------------
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `intent_id` int NULL DEFAULT NULL,
  `agent_task_id` bigint NULL DEFAULT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approver_id` int NULL DEFAULT NULL COMMENT 'FK user.id',
  `approval_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'HIGH_RISK_MCP/PLAN_DECISION/CROSS_DOMAIN/SUPPLEMENT',
  `risk_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LOW/MEDIUM/HIGH',
  `ai_recommendation` json NULL,
  `submitted_payload` json NULL,
  `modified_payload` json NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/AUTO_TIMEOUT',
  `decision_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `submitted_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `decided_at` datetime(3) NULL DEFAULT NULL,
  `expire_at` datetime(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_agent_task`(`agent_task_id` ASC) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_intent_type_status`(`intent_id` ASC, `approval_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `actor_id` int NULL DEFAULT NULL COMMENT '用户id/操作员id',
  `actor_role` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人角色/职位',
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作类型',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for healing_record
-- ----------------------------
DROP TABLE IF EXISTS `healing_record`;
CREATE TABLE `healing_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `intent_id` int NULL DEFAULT NULL,
  `trigger_agent_task_id` bigint NULL DEFAULT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `trigger_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MONITOR/MCP_ERROR/A2A_FAIL',
  `root_cause` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attempt_no` int NOT NULL DEFAULT 1,
  `max_attempts` int NOT NULL DEFAULT 3,
  `strategy` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `result` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SUCCESS/FAILED/HANDOFF_HUMAN',
  `recovered_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_intent`(`intent_id` ASC) USING BTREE,
  INDEX `idx_trigger_task`(`trigger_agent_task_id` ASC) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自愈记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for intent_request
-- ----------------------------
DROP TABLE IF EXISTS `intent_request`;
CREATE TABLE `intent_request`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `user_id` int NULL DEFAULT NULL,
  `input_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '原始自然语言输入',
  `parsed_intent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '解析结果',
  `priority` int NULL DEFAULT NULL COMMENT '优先级',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '意图状态',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '全链路 TraceId',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mcp_call_log
-- ----------------------------
DROP TABLE IF EXISTS `mcp_call_log`;
CREATE TABLE `mcp_call_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `agent_task_id` bigint NULL DEFAULT NULL COMMENT 'FK agent_task.id',
  `intent_id` int NULL DEFAULT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tool_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tool_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_params` json NULL,
  `response_code` int NULL DEFAULT NULL,
  `response_body` json NULL,
  `error_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'FORMAT/PARAM/TOOL_NOT_FOUND/RES_NOT_FOUND/RES_CONFLICT/PERMISSION/TIMEOUT/CAPACITY',
  `latency_ms` int NULL DEFAULT NULL,
  `attempt_no` int NOT NULL DEFAULT 1,
  `is_high_risk` tinyint(1) NOT NULL DEFAULT 0,
  `called_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_agent_task`(`agent_task_id` ASC) USING BTREE,
  INDEX `idx_tool_name`(`tool_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'MCP 调用日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for network_domain
-- ----------------------------
DROP TABLE IF EXISTS `network_domain`;
CREATE TABLE `network_domain`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT (uuid()) COMMENT '网络域唯一标识',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户标识，用于多租户隔离',
  `domain_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '网络域编码',
  `domain_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '网络域名称',
  `controller_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '控制器类型，如 RYU 或 ONOS',
  `controller_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模拟 SDN 控制器访问地址',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '网络域状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '网络域与控制器域信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for network_link
-- ----------------------------
DROP TABLE IF EXISTS `network_link`;
CREATE TABLE `network_link`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT (uuid()) COMMENT '网络链路唯一标识',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户标识，用于多租户隔离',
  `link_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路编码',
  `link_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路名称',
  `source_node_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '源节点标识',
  `target_node_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标节点标识',
  `link_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路类型，如主链路或备用链路',
  `total_bandwidth_mbps` decimal(10, 2) NULL DEFAULT NULL COMMENT '链路总带宽，单位 Mbps',
  `reserved_bandwidth_mbps` decimal(10, 2) NULL DEFAULT NULL COMMENT '已预留带宽，单位 Mbps',
  `max_latency_ms` decimal(8, 2) NULL DEFAULT NULL COMMENT '链路目标最大时延，单位毫秒',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路运行状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '网络链路拓扑与基础容量信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for network_link_resource
-- ----------------------------
DROP TABLE IF EXISTS `network_link_resource`;
CREATE TABLE `network_link_resource`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT (uuid()) COMMENT '链路资源状态唯一标识',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户标识，用于多租户隔离',
  `link_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联链路标识',
  `qos_profile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'QoS 策略档位',
  `priority_queue` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '优先级队列',
  `available_bandwidth_mbps` decimal(10, 2) NULL DEFAULT NULL COMMENT '当前可用带宽，单位 Mbps',
  `current_latency_ms` decimal(8, 2) NULL DEFAULT NULL COMMENT '当前时延，单位毫秒',
  `current_packet_loss_rate` decimal(5, 4) NULL DEFAULT NULL COMMENT '当前丢包率',
  `current_jitter_ms` decimal(8, 2) NULL DEFAULT NULL COMMENT '当前抖动，单位毫秒',
  `is_anomaly` tinyint(1) NULL DEFAULT NULL COMMENT '是否处于异常状态',
  `sampled_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '资源状态采样时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '链路当前资源与 QoS 状态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for network_node
-- ----------------------------
DROP TABLE IF EXISTS `network_node`;
CREATE TABLE `network_node`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT (uuid()) COMMENT '网络节点唯一标识',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户标识，用于多租户隔离',
  `domain_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属网络域标识',
  `node_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '网络节点编码',
  `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '网络节点名称',
  `node_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '网络节点类型，如 SWITCH',
  `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点所在位置',
  `management_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点管理 IP',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点运行状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '网络节点信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for network_resource_reservation
-- ----------------------------
DROP TABLE IF EXISTS `network_resource_reservation`;
CREATE TABLE `network_resource_reservation`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT (uuid()) COMMENT '资源预留唯一标识',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户标识，用于多租户隔离',
  `intent_request_id` int NULL DEFAULT NULL COMMENT '关联意图请求标识',
  `link_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联链路标识',
  `reserved_bandwidth_mbps` decimal(10, 2) NULL DEFAULT NULL COMMENT '预留带宽，单位 Mbps',
  `target_latency_ms` decimal(8, 2) NULL DEFAULT NULL COMMENT '目标时延，单位毫秒',
  `target_packet_loss_rate` decimal(5, 4) NULL DEFAULT NULL COMMENT '目标丢包率',
  `priority` int NULL DEFAULT NULL COMMENT '资源预留优先级，数值越小优先级越高',
  `reservation_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源预留状态',
  `effective_start` datetime NULL DEFAULT NULL COMMENT '预留生效开始时间',
  `effective_end` datetime NULL DEFAULT NULL COMMENT '预留生效结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '网络资源预留记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for person
-- ----------------------------
DROP TABLE IF EXISTS `person`;
CREATE TABLE `person`  (
  `user_id` int NULL DEFAULT NULL,
  `position` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `sex` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sla_record
-- ----------------------------
DROP TABLE IF EXISTS `sla_record`;
CREATE TABLE `sla_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `intent_id` int NULL DEFAULT NULL,
  `link_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'FK network_link.id',
  `metric_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LATENCY/JITTER/LOSS/BANDWIDTH',
  `target_value` decimal(12, 4) NULL DEFAULT NULL,
  `observed_value` decimal(12, 4) NULL DEFAULT NULL,
  `breach_flag` tinyint(1) NOT NULL DEFAULT 0,
  `evaluation_window` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '如 5m/1h',
  `evaluated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_link_metric`(`link_id` ASC, `metric_type` ASC) USING BTREE,
  INDEX `idx_tenant_breach`(`tenant_id` ASC, `breach_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SLA 评估记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for telemetry_data
-- ----------------------------
DROP TABLE IF EXISTS `telemetry_data`;
CREATE TABLE `telemetry_data`  (
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '遥测采集时间',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户标识，用于多租户隔离',
  `link_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联链路标识',
  `link_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路编码冗余字段，便于查询展示',
  `latency_ms` decimal(8, 2) NULL DEFAULT NULL COMMENT '链路时延，单位毫秒',
  `packet_loss_rate` decimal(5, 4) NULL DEFAULT NULL COMMENT '链路丢包率',
  `bandwidth_usage` decimal(5, 2) NULL DEFAULT NULL COMMENT '链路带宽使用率，单位百分比',
  `jitter_ms` decimal(8, 2) NULL DEFAULT NULL COMMENT '链路抖动，单位毫秒',
  `is_anomaly` tinyint(1) NULL DEFAULT NULL COMMENT '是否为异常遥测点'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '网络链路遥测数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `identity` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10008 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
