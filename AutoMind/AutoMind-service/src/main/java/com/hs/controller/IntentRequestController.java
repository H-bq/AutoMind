package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.exception.CommonException;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.trace.TraceIdHolder;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.IntentSubmitDTO;
import com.hs.domain.po.AuditLog;
import com.hs.domain.po.IntentRequest;
import com.hs.domain.vo.IntentSubmitVO;
import com.hs.domain.vo.IntentTraceVO;
import com.hs.domain.vo.AgentTaskVO;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.IAgentTaskService;
import com.hs.service.IAuditLogService;
import com.hs.service.IIntentRequestService;
import com.hs.agent.orchestrator.IntentOrchestrationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "IntentRequest", description = "意图请求查询接口")
@RestController
@RequestMapping("/intent-request")
@RequiredArgsConstructor
@Slf4j
public class IntentRequestController {

    private final IIntentRequestService intentRequestService;
    private final IAgentTaskService agentTaskService;
    private final IAuditLogService auditLogService;
    private final IntentOrchestrationService intentOrchestrationService;

//    @Operation(summary = "查询意图请求列表")
    @GetMapping("/list")
    public R<List<ReadModelVO.IntentRequestVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(intentRequestService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), IntentRequest::getTenantId, tenantId)
                .orderByDesc(IntentRequest::getCreateTime)
                .list().stream().map(ReadModelVO::toIntentRequestVO).toList());
    }

//    @Operation(summary = "根据 id 查询意图请求")
    @GetMapping("/{id}")
    public R<ReadModelVO.IntentRequestVO> getById(@PathVariable("id") Integer id) {
        String tenantId = UserContext.requireTenantId();
        IntentRequest intentRequest = intentRequestService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), IntentRequest::getTenantId, tenantId)
                .eq(IntentRequest::getId, id)
                .one();
        return R.ok(ReadModelVO.toIntentRequestVO(intentRequest));
    }

//    @Operation(summary = "提交意图请求", description = "写入意图请求并异步触发 Supervisor → Planner → Executor → Monitor PEV 编排")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = IntentSubmitVO.class)))
    @PostMapping("/submit")
    public R<IntentSubmitVO> submit(
            @Parameter(description = "意图提交请求", required = true)
            @RequestBody IntentSubmitDTO dto) {
        if (UserContext.isAdminTenant(UserContext.requireTenantId())) throw new ForbiddenException("管理员账号不可提交意图");
        String tenantId = UserContext.requireTenantId();
        IntentRequest intentRequest = new IntentRequest()
                .setTenantId(tenantId)
                .setUserId(UserContext.getUser())
                .setInputText(dto.getInputText())
                .setPriority(parsePriority(dto.getPriority()))
                .setStatus("SUBMITTED")
                .setTraceId(TraceIdHolder.get())
                .setCreateTime(LocalDateTime.now());
        intentRequestService.save(intentRequest);

        auditLogService.save(new AuditLog()
                .setTenantId(tenantId)
                .setActorId(UserContext.getUser())
                .setActorRole(UserContext.role())
                .setAction("CREATE_INTENT")
                .setCreatedAt(LocalDateTime.now()));
        intentOrchestrationService.kickoff(intentRequest.getId());
        return R.ok(new IntentSubmitVO(intentRequest.getId(), TraceIdHolder.get()));
    }

//    @Operation(summary = "查询意图 PEV 轨迹", description = "按 intentId 返回该意图关联的 Agent 任务时间线")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = IntentTraceVO.class)))
    @GetMapping("/{id}/trace")
    public R<IntentTraceVO> trace(
            @Parameter(description = "意图请求 ID", required = true)
            @PathVariable("id") Integer id) {
        String tenantId = UserContext.requireTenantId();
        IntentRequest intentRequest = intentRequestService.getById(id);
        if (intentRequest == null) {
            throw new CommonException("意图请求不存在", 404);
        }
        if (!UserContext.isAdminTenant(tenantId) && !tenantId.equals(intentRequest.getTenantId())) {
            auditLogService.save(new AuditLog()
                    .setTenantId(tenantId)
                    .setActorId(UserContext.getUser())
                    .setActorRole(UserContext.role())
                    .setAction("TENANT_VIOLATION")
                    .setCreatedAt(LocalDateTime.now()));
            throw new CommonException("意图请求不存在", 404);
        }
        List<AgentTaskVO> tasks = agentTaskService.byIntent(id);
        String traceId = tasks.isEmpty() ? null : tasks.get(0).getTraceId();
        return R.ok(new IntentTraceVO()
                .setIntentId(id)
                .setTraceId(traceId)
                .setTasks(tasks));
    }

    private Integer parsePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return 3;
        }
        String normalized = priority.trim().toUpperCase();
        if (normalized.startsWith("P")) {
            normalized = normalized.substring(1);
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            log.info("无法解析意图优先级，使用默认值 priority={}", priority);
            return 3;
        }
    }
}
