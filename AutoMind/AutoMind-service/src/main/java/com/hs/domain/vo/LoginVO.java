package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录成功后的前端上下文")
public class LoginVO {


    @Schema(description = "业务用户ID", example = "10001")
    private Integer userId;

    @Schema(description = "用户名", example = "tenant1_admin")
    private String username;

    @Schema(description = "用户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "用户角色", example = "ADMIN")
    private String role;

    @Schema(description = "租户ID", example = "tenant-001")
    private String tenantId;

    @Schema(description = "人员姓名", example = "张晨")
    private String name;

    @Schema(description = "岗位", example = "北京租户网络管理员")
    private String position;

    @Schema(description = "Bearer token, use it in Authorization request header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "请求头名称：租户ID", example = "X-Tenant-Id")
    private String tenantHeaderName;

    @Schema(description = "请求头名称：用户ID", example = "X-User-Id")
    private String userHeaderName;

    @Schema(description = "请求头名称：用户角色", example = "X-User-Role")
    private String roleHeaderName;

    @Schema(description = "请求头名称：意图ID，访问具体意图链路时按需携带", example = "X-Intent-Id")
    private String intentHeaderName;
}
