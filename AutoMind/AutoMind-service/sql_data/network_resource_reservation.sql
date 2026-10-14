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

 Date: 29/05/2026 11:50:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- Records of network_resource_reservation
-- ----------------------------
INSERT INTO `network_resource_reservation` VALUES ('50000000-0000-0000-0000-000000000001', 'tenant-001', 1, '30000000-0000-0000-0000-000000000001', 120.00, 50.00, 0.0050, 1, 'ACTIVE', '2026-05-08 10:00:00', '2026-05-08 11:00:00', '2026-05-08 09:20:00', '2026-05-08 09:20:00');
INSERT INTO `network_resource_reservation` VALUES ('50000000-0000-0000-0000-000000000002', 'tenant-002', 2, '30000000-0000-0000-0000-000000000002', 80.00, 80.00, 0.0100, 2, 'PLANNED', '2026-05-08 10:30:00', '2026-05-08 12:00:00', '2026-05-08 09:22:00', '2026-05-08 09:22:00');
INSERT INTO `network_resource_reservation` VALUES ('50000000-0000-0000-0000-000000000003', 'tenant-003', 3, '30000000-0000-0000-0000-000000000003', 160.00, 90.00, 0.0100, 1, 'DEGRADED', '2026-05-08 10:00:00', '2026-05-08 11:30:00', '2026-05-08 09:24:00', '2026-05-08 09:24:00');
INSERT INTO `network_resource_reservation` VALUES ('50000000-0000-0000-0000-000000000004', 'tenant-004', 4, '30000000-0000-0000-0000-000000000004', 200.00, 70.00, 0.0050, 2, 'ACTIVE', '2026-05-08 10:30:00', '2026-05-08 12:30:00', '2026-05-08 10:05:30', '2026-05-08 10:05:30');
INSERT INTO `network_resource_reservation` VALUES ('50000000-0000-0000-0000-000000000005', 'tenant-005', 5, '30000000-0000-0000-0000-000000000005', 150.00, 60.00, 0.0050, 1, 'ACTIVE', '2026-05-08 10:20:00', '2026-05-08 12:00:00', '2026-05-08 10:16:40', '2026-05-08 10:16:40');

SET FOREIGN_KEY_CHECKS = 1;
