package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.AuditLog;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.IAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.PageQueryDTO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "AuditLog", description = "审计日志查询接口")
@RestController
@RequestMapping("/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final IAuditLogService auditLogService;

    @Operation(summary = "查询审计日志列表")
    @GetMapping("/list")
    public R<PageDTO<ReadModelVO.AuditLogVO>> list(@ParameterObject @ModelAttribute PageQueryDTO query) {
        String tenantId = UserContext.requireTenantId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AuditLog> page = query.toMpPage("created_at", false);
        auditLogService.page(page, com.baomidou.mybatisplus.core.toolkit.Wrappers.<AuditLog>lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), AuditLog::getTenantId, tenantId)
                .orderByDesc(AuditLog::getCreatedAt));
        return R.ok(PageDTO.of(page, ReadModelVO::toAuditLogVO));
    }

//    @Operation(summary = "根据 id 查询审计日志")
    @GetMapping("/{id}")
    public R<ReadModelVO.AuditLogVO> getById(@PathVariable("id") Integer id) {
        String tenantId = UserContext.requireTenantId();
        AuditLog auditLog = auditLogService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), AuditLog::getTenantId, tenantId)
                .eq(AuditLog::getId, id)
                .one();
        return R.ok(ReadModelVO.toAuditLogVO(auditLog));
    }

}
