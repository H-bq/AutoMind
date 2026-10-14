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

 Date: 29/05/2026 11:50:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- Records of network_link
-- ----------------------------
INSERT INTO `network_link` VALUES ('30000000-0000-0000-0000-000000000001', 'tenant-001', 'bj-office-link-01', '北京办公域核心链路', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000006', 'WAN_PRIMARY', 1000.00, 320.00, 50.00, 'UP', '2026-05-08 09:10:00', '2026-05-08 09:10:00');
INSERT INTO `network_link` VALUES ('30000000-0000-0000-0000-000000000002', 'tenant-002', 'sh-finance-link-01', '上海金融域结算链路', '20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000007', 'WAN_BACKUP', 500.00, 80.00, 80.00, 'UP', '2026-05-08 09:10:00', '2026-05-08 09:10:00');
INSERT INTO `network_link` VALUES ('30000000-0000-0000-0000-000000000003', 'tenant-003', 'gz-manufacture-link-01', '广州制造域生产链路', '20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000008', 'WAN_PRIMARY', 800.00, 640.00, 90.00, 'DEGRADED', '2026-05-08 09:10:00', '2026-05-08 09:10:00');
INSERT INTO `network_link` VALUES ('30000000-0000-0000-0000-000000000004', 'tenant-004', 'cd-rd-link-01', '成都研发域专线链路', '20000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000009', 'WAN_PRIMARY', 1200.00, 200.00, 70.00, 'UP', '2026-05-08 09:10:00', '2026-05-08 09:10:00');
INSERT INTO `network_link` VALUES ('30000000-0000-0000-0000-000000000005', 'tenant-005', 'sz-edu-link-01', '深圳教育域直播链路', '20000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000010', 'WAN_PRIMARY', 900.00, 150.00, 60.00, 'UP', '2026-05-08 09:10:00', '2026-05-08 09:10:00');

SET FOREIGN_KEY_CHECKS = 1;
