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

 Date: 29/05/2026 11:51:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SLA 评估记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sla_record
-- ----------------------------
INSERT INTO `sla_record` VALUES (1, 'tenant-001', 1, '30000000-0000-0000-0000-000000000001', 'LATENCY', 50.0000, 26.8000, 0, '5m', '2026-05-08 17:11:53.000');
INSERT INTO `sla_record` VALUES (2, 'tenant-002', 2, '30000000-0000-0000-0000-000000000002', 'LOSS', 0.0100, 0.0030, 0, '5m', '2026-05-08 17:11:53.000');
INSERT INTO `sla_record` VALUES (3, 'tenant-003', 3, '30000000-0000-0000-0000-000000000003', 'LATENCY', 90.0000, 225.6000, 1, '5m', '2026-05-08 17:11:53.000');
INSERT INTO `sla_record` VALUES (4, 'tenant-004', 4, '30000000-0000-0000-0000-000000000004', 'BANDWIDTH', 200.0000, 200.0000, 0, '5m', '2026-05-08 17:11:53.000');
INSERT INTO `sla_record` VALUES (5, 'tenant-005', 5, '30000000-0000-0000-0000-000000000005', 'BANDWIDTH', 150.0000, 150.0000, 0, '5m', '2026-05-08 17:11:53.000');
INSERT INTO `sla_record` VALUES (6, 'tenant-001', 11, '30000000-0000-0000-0000-000000000001', 'LATENCY', 50.0000, 28.4000, 0, '5m', '2026-05-28 13:47:27.979');

SET FOREIGN_KEY_CHECKS = 1;
