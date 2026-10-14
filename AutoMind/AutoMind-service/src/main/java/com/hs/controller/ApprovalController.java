package com.hs.controller;

import com.hs.approval.ApprovalService;
import com.hs.approval.ApprovalStatus;
import com.hs.common.domain.PageDTO;
import com.hs.common.domain.R;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.ApprovalDecisionDTO;
import com.hs.domain.dto.ApprovalQueryDTO;
import com.hs.domain.dto.ApprovalRejectWithModificationDTO;
import com.hs.domain.po.AuditLog;
import com.hs.domain.vo.ApprovalDetailVO;
import com.hs.domain.vo.MyApprovalProgressVO;
import com.hs.domain.vo.ApprovalRecordVO;
import com.hs.service.IApprovalRecordService;
import com.hs.service.IAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Approval", description = "审批工作流接口")
@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final IApprovalRecordService approvalRecordService;
    private final ApprovalService approvalService;
    private final IAuditLogService auditLogService;

    @Operation(summary = "查询待审批列表", description = "admin 返回全量，普通租户仅返回本租户审批")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping("/pending")
    public R<PageDTO<ApprovalRecordVO>> pending(@ParameterObject ApprovalQueryDTO query) {
        return R.ok(approvalRecordService.pageApprovals(query, true));
    }

    @Operation(summary = "查询审批历史", description = "admin 返回全量，普通租户仅返回本租户审批")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping("/history")
    public R<PageDTO<ApprovalRecordVO>> history(@ParameterObject ApprovalQueryDTO query) {
        return R.ok(approvalRecordService.pageApprovals(query, false));
    }

    @Operation(summary = "查询我的审批进度", description = "返回当前登录用户提交意图的最新审批状态与决策原因")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping("/my-progress")
    public R<PageDTO<MyApprovalProgressVO>> myProgress(@ParameterObject ApprovalQueryDTO query) {
        return R.ok(approvalRecordService.pageMyProgress(query));
    }

    @Operation(summary = "查询审批详情")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = ApprovalDetailVO.class)))
    @GetMapping("/{id}")
    public R<ApprovalDetailVO> detail(@Parameter(description = "审批 ID", required = true) @PathVariable Long id) {
        return R.ok(approvalRecordService.detail(id));
    }

    @Operation(summary = "批准审批")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id, @RequestBody ApprovalDecisionDTO dto) {
        requireAdmin();
        approvalService.decide(id, ApprovalStatus.APPROVED, UserContext.getUser(), dto == null ? null : dto.getReason());
        return R.ok();
    }

    @Operation(summary = "拒绝审批")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestBody ApprovalDecisionDTO dto) {
        requireAdmin();
        approvalService.decide(id, ApprovalStatus.REJECTED, UserContext.getUser(), dto == null ? null : dto.getReason());
        return R.ok();
    }

    @Operation(summary = "拒绝审批并要求修改")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/{id}/reject-with-modification")
    public R<Void> rejectWithModification(@PathVariable Long id, @RequestBody ApprovalRejectWithModificationDTO dto) {
        requireAdmin();
        approvalService.rejectWithModification(id, UserContext.getUser(),
                dto == null ? null : dto.getReason(), dto == null ? null : dto.getModifiedPayload());
        return R.ok();
    }

    private void requireAdmin() {
        if (!UserContext.isAdminTenant(UserContext.requireTenantId())) {
            auditLogService.save(new AuditLog()
                    .setTenantId(UserContext.tenantId())
                    .setActorId(UserContext.getUser())
                    .setActorRole(UserContext.role())
                    .setAction("ROLE_VIOLATION")
                    .setCreatedAt(LocalDateTime.now()));
            throw new ForbiddenException("仅超级管理员可审批");
        }
    }
}
