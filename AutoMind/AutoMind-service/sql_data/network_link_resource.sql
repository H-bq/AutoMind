/*
 Navicat Premium Dump SQL

 Source Server         : 阿里云远程
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : 47.117.156.128:3307
 Source Schema         : network

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 29/05/2026 11:50:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- Records of network_link_resource
-- ----------------------------
INSERT INTO `network_link_resource` VALUES ('40000000-0000-0000-0000-000000000001', 'tenant-001', '30000000-0000-0000-0000-000000000001', 'VIDEO_LOW_LATENCY', 'HIGH', 680.00, 28.40, 0.0010, 3.20, 0, '2026-05-08 09:15:00');
INSERT INTO `network_link_resource` VALUES ('40000000-0000-0000-0000-000000000002', 'tenant-002', '30000000-0000-0000-0000-000000000002', 'BEST_EFFORT', 'MEDIUM', 420.00, 42.60, 0.0020, 4.80, 0, '2026-05-08 09:15:00');
INSERT INTO `network_link_resource` VALUES ('40000000-0000-0000-0000-000000000003', 'tenant-003', '30000000-0000-0000-0000-000000000003', 'VIDEO_LOW_LATENCY', 'HIGH', 160.00, 216.00, 0.0610, 18.50, 1, '2026-05-08 09:15:00');
INSERT INTO `network_link_resource` VALUES ('40000000-0000-0000-0000-000000000004', 'tenant-004', '30000000-0000-0000-0000-000000000004', 'DEDICATED_BANDWIDTH', 'HIGH', 1000.00, 36.80, 0.0015, 3.90, 0, '2026-05-08 09:15:00');
INSERT INTO `network_link_resource` VALUES ('40000000-0000-0000-0000-000000000005', 'tenant-005', '30000000-0000-0000-0000-000000000005', 'VIDEO_LOW_LATENCY', 'HIGH', 750.00, 32.50, 0.0012, 3.60, 0, '2026-05-08 09:15:00');

SET FOREIGN_KEY_CHECKS = 1;
