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

 Date: 29/05/2026 11:50:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- Records of network_node
-- ----------------------------
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000001', 'tenant-001', '10000000-0000-0000-0000-000000000001', 'BJ-OFFICE-CORE-01', '北京办公核心交换机 01', 'SWITCH', 'Beijing', '10.10.1.11', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000002', 'tenant-002', '10000000-0000-0000-0000-000000000002', 'SH-FINANCE-CORE-01', '上海金融核心交换机 01', 'SWITCH', 'Shanghai', '10.20.1.11', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000003', 'tenant-003', '10000000-0000-0000-0000-000000000003', 'GZ-MFG-EDGE-01', '广州制造边缘交换机 01', 'SWITCH', 'Guangzhou', '10.30.1.21', 'WARN', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000004', 'tenant-004', '10000000-0000-0000-0000-000000000004', 'CD-RD-CORE-01', '成都研发核心交换机 01', 'SWITCH', 'Chengdu', '10.40.1.11', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000005', 'tenant-005', '10000000-0000-0000-0000-000000000005', 'SZ-EDU-EDGE-01', '深圳教育边缘交换机 01', 'SWITCH', 'Shenzhen', '10.50.1.21', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000006', 'tenant-001', '10000000-0000-0000-0000-000000000001', 'BJ-OFFICE-ACCESS-01', '北京办公接入交换机 01', 'SWITCH', 'Beijing', '10.10.1.21', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000007', 'tenant-002', '10000000-0000-0000-0000-000000000002', 'SH-FINANCE-DR-01', '上海金融灾备交换机 01', 'SWITCH', 'Shanghai', '10.20.1.21', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000008', 'tenant-003', '10000000-0000-0000-0000-000000000003', 'GZ-MFG-CAMERA-01', '广州制造视频巡检交换机 01', 'SWITCH', 'Guangzhou', '10.30.1.31', 'WARN', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000009', 'tenant-004', '10000000-0000-0000-0000-000000000004', 'CD-RD-DC-01', '成都研发数据中心交换机 01', 'SWITCH', 'Chengdu', '10.40.1.31', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');
INSERT INTO `network_node` VALUES ('20000000-0000-0000-0000-000000000010', 'tenant-005', '10000000-0000-0000-0000-000000000005', 'SZ-EDU-LIVE-01', '深圳教育直播中心交换机 01', 'SWITCH', 'Shenzhen', '10.50.1.31', 'UP', '2026-05-08 09:05:00', '2026-05-08 09:05:00');

SET FOREIGN_KEY_CHECKS = 1;
