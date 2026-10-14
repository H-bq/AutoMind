package com.hs.common.utils;

import com.hs.common.exception.BadRequestException;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.trace.TraceIdHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserContext {
    private static final ThreadLocal<UserInfo> TL = ThreadLocal.withInitial(UserInfo::new);

    /**
     * 保存当前登录用户信息到ThreadLocal，保留原有调用面。
     * @param userId 用户id
     */
    public static void setUser(Integer userId) {
        TL.get().setUserId(userId);
    }

    public static void setUserInfo(Integer userId, String tenantId, String role, Integer intentId) {
        TL.set(new UserInfo(userId, tenantId, role, intentId));
    }

    public static void setTenantId(String tenantId) {
        TL.get().setTenantId(tenantId);
    }

    public static void setRole(String role) {
        TL.get().setRole(role);
    }

    public static void setIntentId(Integer intentId) {
        TL.get().setIntentId(intentId);
    }

    /**
     * 获取当前登录用户信息，保留原有调用面。
     *
     * @return 用户id
     */
    public static Integer getUser() {
        return TL.get().getUserId();
    }

    public static UserInfo getUserInfo() {
        return TL.get();
    }

    public static String tenantId() {
        return TL.get().getTenantId();
    }

    public static String role() {
        return TL.get().getRole();
    }

    public static Integer intentId() {
        return TL.get().getIntentId();
    }

    public static String traceId() {
        return TraceIdHolder.get();
    }

    public static String requireTenantId() {
        String tenantId = tenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new ForbiddenException("缺少租户上下文");
        }
        return tenantId;
    }

    public static boolean isAdminTenant(String tenantId) {
        return "admin".equalsIgnoreCase(tenantId);
    }

    public static Integer requireTenantIdAsInteger() {
        String tenantId = requireTenantId();
        try {
            return Integer.valueOf(tenantId);
        } catch (NumberFormatException e) {
            throw new BadRequestException("租户上下文必须是数字", e);
        }
    }

    public static String requireRole() {
        String role = role();
        if (role == null || role.isBlank()) {
            throw new ForbiddenException("缺少角色上下文");
        }
        return role;
    }

    /**
     * 移除当前登录用户信息
     */
    public static void removeUser(){
        TL.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Integer userId;
        private String tenantId;
        private String role;
        private Integer intentId;
    }
}
