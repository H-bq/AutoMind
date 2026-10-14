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

 Date: 29/05/2026 11:51:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- Records of telemetry_data
-- ----------------------------
INSERT INTO `telemetry_data` VALUES ('2026-05-08 17:11:53', 'tenant-001', '30000000-0000-0000-0000-000000000001', 'bj-office-link-01', 26.80, 0.0010, 41.50, 3.10, 0);
INSERT INTO `telemetry_data` VALUES ('2026-05-08 17:11:53', 'tenant-002', '30000000-0000-0000-0000-000000000002', 'sh-finance-link-01', 47.20, 0.0030, 28.00, 5.20, 0);
INSERT INTO `telemetry_data` VALUES ('2026-05-08 17:11:53', 'tenant-003', '30000000-0000-0000-0000-000000000003', 'gz-manufacture-link-01', 225.60, 0.0720, 86.40, 22.30, 1);
INSERT INTO `telemetry_data` VALUES ('2026-05-08 17:11:53', 'tenant-004', '30000000-0000-0000-0000-000000000004', 'cd-rd-link-01', 36.80, 0.0015, 31.70, 3.90, 0);
INSERT INTO `telemetry_data` VALUES ('2026-05-08 17:11:53', 'tenant-005', '30000000-0000-0000-0000-000000000005', 'sz-edu-link-01', 32.50, 0.0012, 44.60, 3.60, 0);

SET FOREIGN_KEY_CHECKS = 1;
