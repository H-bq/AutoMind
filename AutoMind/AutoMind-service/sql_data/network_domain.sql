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

 Date: 29/05/2026 11:51:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- Records of network_domain
-- ----------------------------
INSERT INTO `network_domain` VALUES ('10000000-0000-0000-0000-000000000001', 'tenant-001', 'BJ-OFFICE-DOMAIN', '北京云网科技办公域', 'RYU', 'http://ryu-bj-tenant1-sim:8080', 'ACTIVE', '2026-05-08 09:00:00', '2026-05-14 12:20:28');
INSERT INTO `network_domain` VALUES ('10000000-0000-0000-0000-000000000002', 'tenant-002', 'SH-FINANCE-DOMAIN', '上海金融租户生产域', 'ONOS', 'http://onos-sh-tenant2-sim:8181', 'ACTIVE', '2026-05-08 09:00:00', '2026-05-08 09:00:00');
INSERT INTO `network_domain` VALUES ('10000000-0000-0000-0000-000000000003', 'tenant-003', 'GZ-MANUFACTURE-DOMAIN', '广州制造租户工厂域', 'RYU', 'http://ryu-gz-tenant3-sim:8080', 'DEGRADED', '2026-05-08 09:00:00', '2026-05-08 09:00:00');
INSERT INTO `network_domain` VALUES ('10000000-0000-0000-0000-000000000004', 'tenant-004', 'CD-RD-DOMAIN', '成都研发租户研发域', 'ONOS', 'http://onos-cd-tenant4-sim:8181', 'ACTIVE', '2026-05-08 09:00:00', '2026-05-08 09:00:00');
INSERT INTO `network_domain` VALUES ('10000000-0000-0000-0000-000000000005', 'tenant-005', 'SZ-EDU-DOMAIN', '深圳教育租户教学域', 'RYU', 'http://ryu-sz-tenant5-sim:8080', 'ACTIVE', '2026-05-08 09:00:00', '2026-05-08 09:00:00');
INSERT INTO `network_domain` VALUES ('8e84bcbb-5025-11f1-8b97-00163e254a5b', 'admin', 'admin', '管理员', NULL, NULL, NULL, '2026-05-15 14:16:06', '2026-05-15 14:16:06');

SET FOREIGN_KEY_CHECKS = 1;
