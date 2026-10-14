package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.common.domain.PageDTO;
import com.hs.common.exception.CommonException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.AgentTaskQueryDTO;
import com.hs.domain.po.A2aMessagePO;
import com.hs.domain.po.AgentTask;
import com.hs.domain.po.ApprovalRecord;
import com.hs.domain.po.IntentRequest;
import com.hs.domain.po.McpCallLog;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.vo.AgentExecutionTimelineVO;
import com.hs.domain.vo.AgentTaskDecisionSummaryVO;
import com.hs.domain.vo.AgentTaskGroupVO;
import com.hs.domain.vo.AgentTaskVO;
import com.hs.mapper.AgentTaskMapper;
import com.hs.service.IA2aMessageService;
import com.hs.service.IAgentTaskService;
import com.hs.service.IApprovalRecordService;
import com.hs.service.IIntentRequestService;
import com.hs.service.IMcpCallLogService;
import com.hs.service.ISlaRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentTaskServiceImpl extends ServiceImpl<AgentTaskMapper, AgentTask> implements IAgentTaskService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final IIntentRequestService intentRequestService;
    private final IMcpCallLogService mcpCallLogService;
    private final IA2aMessageService a2aMessageService;
    private final IApprovalRecordService approvalRecordService;
    private final ISlaRecordService slaRecordService;

    @Override
    public PageDTO<AgentTaskGroupVO> pageTasks(AgentTaskQueryDTO query) {
        String tenantId = UserContext.requireTenantId();
        Page<AgentTaskGroupVO> page = query.toMpPage(null, false);
        Page<AgentTaskGroupVO> result = baseMapper.selectIntentTaskGroupPage(
                page, query, tenantId, UserContext.isAdminTenant(tenantId));

        List<Integer> intentIds = result.getRecords().stream()
                .map(AgentTaskGroupVO::getIntentId)
                .toList();
        if (intentIds.isEmpty()) {
            return PageDTO.of(result);
        }

        Map<Integer, List<AgentTaskVO>> tasksByIntentId = lambdaQuery()
                .in(AgentTask::getIntentId, intentIds)
                .eq(!UserContext.isAdminTenant(tenantId), AgentTask::getTenantId, tenantId)
                .orderByAsc(AgentTask::getStartedAt)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.groupingBy(AgentTaskVO::getIntentId));

        result.getRecords().forEach(group -> group.setAgentTasks(
                tasksByIntentId.getOrDefault(group.getIntentId(), List.of())));
        return PageDTO.of(result);
    }

    @Override
    public AgentTaskVO detail(Long id) {
        String tenantId = UserContext.requireTenantId();
        AgentTask task = lambdaQuery()
                .eq(AgentTask::getId, id)
                .eq(!UserContext.isAdminTenant(tenantId), AgentTask::getTenantId, tenantId)
                .one();
        if (task == null) {
            throw new CommonException("Agent 任务不存在", 404);
        }
        return toVO(task);
    }

    @Override
    public List<AgentTaskVO> byTrace(String traceId) {
        String tenantId = UserContext.requireTenantId();
        return lambdaQuery()
                .eq(AgentTask::getTraceId, traceId)
                .eq(!UserContext.isAdminTenant(tenantId), AgentTask::getTenantId, tenantId)
                .orderByAsc(AgentTask::getStartedAt)
                .list().stream().map(this::toVO).toList();
    }

    @Override
    public List<AgentTaskVO> byIntent(Integer intentId) {
        String tenantId = UserContext.requireTenantId();
        return lambdaQuery()
                .eq(AgentTask::getIntentId, intentId)
                .eq(!UserContext.isAdminTenant(tenantId), AgentTask::getTenantId, tenantId)
                .orderByAsc(AgentTask::getStartedAt)
                .list().stream().map(this::toVO).toList();
    }

    @Override
    public AgentExecutionTimelineVO timelineByIntent(Integer intentId) {
        IntentRequest intent = findVisibleIntent(intentId);
        return buildTimeline(intent);
    }

    @Override
    public AgentExecutionTimelineVO timelineByTrace(String traceId) {
        String tenantId = UserContext.requireTenantId();
        IntentRequest intent = intentRequestService.lambdaQuery()
                .eq(IntentRequest::getTraceId, traceId)
                .eq(!UserContext.isAdminTenant(tenantId), IntentRequest::getTenantId, tenantId)
                .one();
        if (intent == null) {
            AgentTask task = lambdaQuery()
                    .eq(AgentTask::getTraceId, traceId)
                    .eq(!UserContext.isAdminTenant(tenantId), AgentTask::getTenantId, tenantId)
                    .orderByAsc(AgentTask::getStartedAt)
                    .last("LIMIT 1")
                    .one();
            if (task != null) {
                intent = findVisibleIntent(task.getIntentId());
            }
        }
        if (intent == null) {
            throw new CommonException("Trace 不存在", 404);
        }
        return buildTimeline(intent);
    }

    private LambdaQueryWrapper<AgentTask> baseWrapper(String tenantId) {
        return new LambdaQueryWrapper<AgentTask>()
                .eq(!UserContext.isAdminTenant(tenantId), AgentTask::getTenantId, tenantId);
    }

    private IntentRequest findVisibleIntent(Integer intentId) {
        String tenantId = UserContext.requireTenantId();
        IntentRequest intent = intentRequestService.lambdaQuery()
                .eq(IntentRequest::getId, intentId)
                .eq(!UserContext.isAdminTenant(tenantId), IntentRequest::getTenantId, tenantId)
                .one();
        if (intent == null) {
            throw new CommonException("意图请求不存在", 404);
        }
        return intent;
    }

    private AgentExecutionTimelineVO buildTimeline(IntentRequest intent) {
        List<AgentTask> tasks = lambdaQuery()
                .eq(AgentTask::getIntentId, intent.getId())
                .orderByAsc(AgentTask::getStartedAt)
                .list();
        List<McpCallLog> mcpCalls = mcpCallLogService.lambdaQuery()
                .eq(McpCallLog::getIntentId, intent.getId())
                .orderByAsc(McpCallLog::getCalledAt)
                .list();
        List<A2aMessagePO> a2aMessages = a2aMessageService.lambdaQuery()
                .eq(A2aMessagePO::getTraceId, intent.getTraceId())
                .orderByAsc(A2aMessagePO::getSentAt)
                .list();
        List<ApprovalRecord> approvals = approvalRecordService.lambdaQuery()
                .eq(ApprovalRecord::getIntentId, intent.getId())
                .orderByAsc(ApprovalRecord::getSubmittedAt)
                .list();
        List<SlaRecord> slaRecords = slaRecordService.lambdaQuery()
                .eq(SlaRecord::getIntentId, intent.getId())
                .orderByAsc(SlaRecord::getEvaluatedAt)
                .list();

        List<AgentExecutionTimelineVO.EventVO> events = new ArrayList<>();
        tasks.forEach(task -> events.add(agentTaskEvent(task)));
        mcpCalls.forEach(log -> events.add(mcpCallEvent(log)));
        a2aMessages.forEach(message -> events.add(a2aEvent(message)));
        approvals.forEach(approval -> events.add(approvalEvent(approval)));
        slaRecords.forEach(record -> events.add(slaEvent(record)));
        events.sort(Comparator.comparing(AgentExecutionTimelineVO.EventVO::getEventTime,
                Comparator.nullsLast(LocalDateTime::compareTo)));

        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("agentTaskCount", tasks.size());
        summary.put("mcpCallCount", mcpCalls.size());
        summary.put("a2aMessageCount", a2aMessages.size());
        summary.put("approvalCount", approvals.size());
        summary.put("slaRecordCount", slaRecords.size());
        summary.put("hasAwaitApproval", approvals.stream().anyMatch(a -> "PENDING".equalsIgnoreCase(a.getStatus())));
        summary.put("hasHighRiskMcp", approvals.stream().anyMatch(a -> "HIGH_RISK_MCP".equalsIgnoreCase(a.getApprovalType())));
        summary.put("slaBreaches", slaRecords.stream().filter(record -> Boolean.TRUE.equals(record.getBreachFlag())).count());
        summary.put("latestErrorCode", tasks.stream().map(AgentTask::getErrorCode).filter(Objects::nonNull).reduce((first, second) -> second).orElse(null));

        return new AgentExecutionTimelineVO()
                .setIntentId(intent.getId())
                .setTenantId(intent.getTenantId())
                .setTraceId(intent.getTraceId())
                .setInputText(intent.getInputText())
                .setStatus(intent.getStatus())
                .setCreateTime(intent.getCreateTime())
                .setSummary(summary)
                .setEvents(events);
    }

    private AgentExecutionTimelineVO.EventVO agentTaskEvent(AgentTask task) {
        AgentTaskVO taskVO = toVO(task);
        AgentTaskDecisionSummaryVO decisionSummary = taskVO.getDecisionSummary();
        return new AgentExecutionTimelineVO.EventVO()
                .setEventTime(task.getStartedAt())
                .setEventType("AGENT_TASK")
                .setStage(task.getAgentRole())
                .setStatus(task.getErrorCode() == null ? task.getPevState() : task.getErrorCode())
                .setTitle(decisionSummary == null ? task.getAgentRole() : decisionSummary.getTitle())
                .setSummary(decisionSummary == null ? task.getTaskType() : decisionSummary.getSummary())
                .setPayloadId(task.getId())
                .setAgentTaskId(task.getId())
                .setPayload(taskVO);
    }

    private AgentExecutionTimelineVO.EventVO mcpCallEvent(McpCallLog log) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("toolName", log.getToolName());
        payload.put("toolVersion", log.getToolVersion());
        payload.put("requestParams", parsePayload(log.getRequestParams()));
        payload.put("responseCode", log.getResponseCode());
        payload.put("responseBody", parsePayload(log.getResponseBody()));
        payload.put("errorType", log.getErrorType());
        payload.put("latencyMs", log.getLatencyMs());
        payload.put("attemptNo", log.getAttemptNo());
        payload.put("isHighRisk", log.getIsHighRisk());
        return new AgentExecutionTimelineVO.EventVO()
                .setEventTime(log.getCalledAt())
                .setEventType("MCP_CALL")
                .setStage("MCP")
                .setStatus(log.getErrorType() == null ? "SUCCESS" : log.getErrorType())
                .setTitle("MCP 调用 " + log.getToolName())
                .setSummary("响应码 " + defaultText(text(log.getResponseCode()), "-")
                        + (log.getErrorType() == null ? "" : "，错误 " + log.getErrorType()))
                .setPayloadId(log.getId())
                .setAgentTaskId(log.getAgentTaskId())
                .setPayload(payload);
    }

    private AgentExecutionTimelineVO.EventVO a2aEvent(A2aMessagePO message) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("sourceDomainId", message.getSourceDomainId());
        payload.put("targetDomainId", message.getTargetDomainId());
        payload.put("sourceAgent", message.getSourceAgent());
        payload.put("targetAgent", message.getTargetAgent());
        payload.put("messageType", message.getMessageType());
        payload.put("status", message.getStatus());
        payload.put("latencyMs", message.getLatencyMs());
        payload.put("payload", parsePayload(message.getPayload()));
        return new AgentExecutionTimelineVO.EventVO()
                .setEventTime(message.getSentAt() == null ? message.getReceivedAt() : message.getSentAt())
                .setEventType("A2A_MESSAGE")
                .setStage("A2A")
                .setStatus(message.getStatus())
                .setTitle("A2A " + message.getMessageType())
                .setSummary(defaultText(message.getSourceDomainId(), "-") + " -> " + defaultText(message.getTargetDomainId(), "-"))
                .setPayloadId(message.getId())
                .setAgentTaskId(message.getTaskId())
                .setPayload(payload);
    }

    private AgentExecutionTimelineVO.EventVO approvalEvent(ApprovalRecord approval) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("approvalType", approval.getApprovalType());
        payload.put("riskLevel", approval.getRiskLevel());
        payload.put("aiRecommendation", parsePayload(approval.getAiRecommendation()));
        payload.put("submittedPayload", parsePayload(approval.getSubmittedPayload()));
        payload.put("status", approval.getStatus());
        payload.put("decisionReason", approval.getDecisionReason());
        payload.put("submittedAt", approval.getSubmittedAt());
        payload.put("decidedAt", approval.getDecidedAt());
        return new AgentExecutionTimelineVO.EventVO()
                .setEventTime(approval.getSubmittedAt())
                .setEventType("APPROVAL")
                .setStage("APPROVAL")
                .setStatus(approval.getStatus())
                .setTitle("审批 " + approval.getApprovalType())
                .setSummary("风险 " + defaultText(approval.getRiskLevel(), "-") + "，状态 " + defaultText(approval.getStatus(), "-"))
                .setPayloadId(approval.getId())
                .setAgentTaskId(approval.getAgentTaskId())
                .setPayload(payload);
    }

    private AgentExecutionTimelineVO.EventVO slaEvent(SlaRecord record) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("linkId", record.getLinkId());
        payload.put("metricType", record.getMetricType());
        payload.put("targetValue", record.getTargetValue());
        payload.put("observedValue", record.getObservedValue());
        payload.put("breachFlag", record.getBreachFlag());
        payload.put("evaluationWindow", record.getEvaluationWindow());
        return new AgentExecutionTimelineVO.EventVO()
                .setEventTime(record.getEvaluatedAt())
                .setEventType("SLA_RECORD")
                .setStage("VERIFY")
                .setStatus(Boolean.TRUE.equals(record.getBreachFlag()) ? "BREACH" : "OK")
                .setTitle("SLA " + record.getMetricType())
                .setSummary("观测值 " + record.getObservedValue() + " / 目标 " + record.getTargetValue())
                .setPayloadId(record.getId())
                .setPayload(payload);
    }

    private AgentTaskVO toVO(AgentTask task) {
        return new AgentTaskVO()
                .setId(task.getId())
                .setTenantId(task.getTenantId())
                .setIntentId(task.getIntentId())
                .setParentTaskId(task.getParentTaskId())
                .setTraceId(task.getTraceId())
                .setAgentRole(task.getAgentRole())
                .setTaskType(task.getTaskType())
                .setPevState(task.getPevState())
                .setConfidence(task.getConfidence())
                .setInputPayload(task.getInputPayload())
                .setOutputPayload(task.getOutputPayload())
                .setErrorCode(task.getErrorCode())
                .setRetryCount(task.getRetryCount())
                .setStartedAt(task.getStartedAt())
                .setFinishedAt(task.getFinishedAt())
                .setDecisionSummary(decisionSummary(task));
    }

    private AgentTaskDecisionSummaryVO decisionSummary(AgentTask task) {
        Object input = parsePayload(task.getInputPayload());
        Object output = parsePayload(task.getOutputPayload());
        AgentTaskDecisionSummaryVO summary = new AgentTaskDecisionSummaryVO()
                .setRawInput(input)
                .setRawOutput(output)
                .setStatus(displayStatus(task.getPevState(), task.getErrorCode()));
        Map<String, Object> inputMap = asMap(input);
        Map<String, Object> outputMap = asMap(output);
        return switch (task.getAgentRole()) {
            case "SUPERVISOR" -> supervisorSummary(summary, inputMap, outputMap);
            case "PLANNER" -> plannerSummary(summary, outputMap);
            case "EXECUTOR" -> executorSummary(summary, outputMap, task.getErrorCode());
            case "MONITOR" -> monitorSummary(summary, outputMap);
            default -> summary
                    .setTitle(defaultText(task.getAgentRole(), "Agent") + " 任务")
                    .setSummary(defaultText(task.getPevState(), "未知状态"));
        };
    }

    private AgentTaskDecisionSummaryVO supervisorSummary(AgentTaskDecisionSummaryVO summary,
                                                        Map<String, Object> input,
                                                        Map<String, Object> output) {
        if (output.isEmpty()) {
            return summary.setTitle("意图解析").setSummary("暂无结构化解析结果");
        }
        String source = text(output.get("sourceLocation"));
        String target = text(output.get("targetLocation"));
        Object bandwidth = output.get("bandwidthMbps");
        String protocol = text(output.get("protocol"));
        String priority = text(output.get("priority"));
        summary.setTitle("意图解析完成")
                .setSummary(source + " -> " + target + "，" + valueWithUnit(bandwidth, "Mbps") + "，" + protocol + "，" + priority);
        add(summary, "业务类型", output.get("intentType"), null, null);
        add(summary, "源端", source, null, null);
        add(summary, "目的端", target, null, null);
        add(summary, "带宽", bandwidth, "Mbps", null);
        add(summary, "协议", protocol, null, null);
        add(summary, "优先级", priority, null, null);
        add(summary, "开始时间", output.get("startTime"), null, null);
        add(summary, "结束时间", output.get("endTime"), null, null);
        add(summary, "最大时延", output.get("maxLatencyMs"), "ms", "success");
        add(summary, "最大丢包率", percent(output.get("maxLossRate")), null, "success");
        add(summary, "缺失字段", output.get("missingFields"), null, null);
        return summary;
    }

    private AgentTaskDecisionSummaryVO plannerSummary(AgentTaskDecisionSummaryVO summary, Map<String, Object> output) {
        if (output.isEmpty()) {
            return summary.setTitle("路径规划").setSummary("暂无路径规划结果");
        }
        String primary = firstLinkCode(output.get("primaryPath"));
        String backup = firstLinkCode(output.get("backupPath"));
        Object latency = output.get("estimatedLatencyMs");
        Object loss = output.get("estimatedLossRate");
        summary.setTitle("路径规划完成")
                .setSummary("主链路 " + defaultText(primary, "-") + "，预计时延 "
                        + valueWithUnit(latency, "ms") + "，丢包率 " + percent(loss));
        add(summary, "主链路", primary, null, "success");
        add(summary, "备链路", backup, null, null);
        add(summary, "预留带宽", output.get("reservedBandwidthMbps"), "Mbps", "success");
        add(summary, "预估时延", latency, "ms", "success");
        add(summary, "预估丢包率", percent(loss), null, "success");
        add(summary, "风险等级", output.get("riskLevel"), null, riskStatus(output.get("riskLevel")));
        add(summary, "需要规划审批", yesNo(output.get("requiresApproval")), null, null);
        add(summary, "A2A 降级", yesNo(output.get("crossDomainDegraded")), null, null);
        add(summary, "资源冲突", output.get("conflicts"), null, null);
        return summary;
    }

    private AgentTaskDecisionSummaryVO executorSummary(AgentTaskDecisionSummaryVO summary,
                                                       Map<String, Object> output,
                                                       String errorCode) {
        if ("AWAIT_APPROVAL".equalsIgnoreCase(errorCode)) {
            return summary.setTitle("等待高风险 MCP 审批")
                    .setSummary("set_qos 属于高风险 MCP，等待管理员审批后继续执行")
                    .setStatus("warning")
                    .setItems(List.of(
                            item("审批类型", output.get("approvalType"), null, "warning"),
                            item("状态", output.get("status"), null, "warning")
                    ));
        }
        if (output.isEmpty()) {
            return summary.setTitle("资源执行").setSummary(defaultText(errorCode, "暂无执行结果"));
        }
        Object status = output.get("status");
        summary.setTitle("资源配置执行" + ("SUCCESS".equalsIgnoreCase(text(status)) ? "成功" : "完成"))
                .setSummary("执行方式 " + defaultText(text(output.get("executorVia")), "MCP")
                        + "，影响链路 " + defaultText(String.valueOf(output.get("affectedLinks")), "-"));
        add(summary, "执行状态", status, null, statusLabel(status));
        add(summary, "执行方式", output.get("executorVia"), null, null);
        add(summary, "影响链路", output.get("affectedLinks"), null, null);
        add(summary, "执行步骤", output.get("executedSteps"), null, null);
        add(summary, "预留编号", output.get("reservationId"), null, null);
        add(summary, "错误码", output.get("errorCode"), null, "danger");
        return summary;
    }

    private AgentTaskDecisionSummaryVO monitorSummary(AgentTaskDecisionSummaryVO summary, Map<String, Object> output) {
        if (output.isEmpty()) {
            return summary.setTitle("SLA 验证").setSummary("暂无 SLA 验证结果");
        }
        boolean breach = Boolean.TRUE.equals(output.get("overallBreach"));
        summary.setTitle("SLA 验证完成")
                .setSummary((breach ? "SLA 存在违约" : "SLA 通过") + "，建议 " + defaultText(text(output.get("recommendation")), "-"))
                .setStatus(breach ? "danger" : "success");
        add(summary, "整体违约", yesNo(output.get("overallBreach")), null, breach ? "danger" : "success");
        add(summary, "建议动作", output.get("recommendation"), null, null);
        add(summary, "最差指标", output.get("worstMetric"), null, null);
        add(summary, "指标结果", output.get("metricResults"), null, breach ? "danger" : "success");
        return summary;
    }

    private Object parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, Object.class);
        } catch (JsonProcessingException e) {
            return payload;
        }
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return objectMapper.convertValue(map, MAP_TYPE);
        }
        return Map.of();
    }

    private String firstLinkCode(Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            Map<String, Object> link = asMap(list.get(0));
            String linkCode = text(link.get("linkCode"));
            return linkCode == null || linkCode.isBlank() ? text(link.get("linkId")) : linkCode;
        }
        return null;
    }

    private void add(AgentTaskDecisionSummaryVO summary, String label, Object value, String unit, String status) {
        summary.getItems().add(item(label, value, unit, status));
    }

    private AgentTaskDecisionSummaryVO.ItemVO item(String label, Object value, String unit, String status) {
        return new AgentTaskDecisionSummaryVO.ItemVO()
                .setLabel(label)
                .setValue(value)
                .setUnit(unit)
                .setStatus(status);
    }

    private String displayStatus(String pevState, String errorCode) {
        if (errorCode != null && !errorCode.isBlank()) {
            if ("AWAIT_APPROVAL".equalsIgnoreCase(errorCode)) {
                return "warning";
            }
            return "danger";
        }
        if ("COMPLETED".equalsIgnoreCase(pevState)) {
            return "success";
        }
        if ("FAILED".equalsIgnoreCase(pevState)) {
            return "danger";
        }
        return "info";
    }

    private String riskStatus(Object riskLevel) {
        String value = text(riskLevel);
        if ("HIGH".equalsIgnoreCase(value)) {
            return "danger";
        }
        if ("MEDIUM".equalsIgnoreCase(value)) {
            return "warning";
        }
        if ("LOW".equalsIgnoreCase(value)) {
            return "success";
        }
        return null;
    }

    private String statusLabel(Object status) {
        return "SUCCESS".equalsIgnoreCase(text(status)) ? "success" : null;
    }

    private String yesNo(Object value) {
        if (value instanceof Boolean bool) {
            return bool ? "是" : "否";
        }
        return String.valueOf(value);
    }

    private String percent(Object value) {
        if (value == null) {
            return "-";
        }
        try {
            BigDecimal decimal = new BigDecimal(String.valueOf(value));
            return decimal.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
        } catch (NumberFormatException e) {
            return String.valueOf(value);
        }
    }

    private String valueWithUnit(Object value, String unit) {
        if (value == null) {
            return "-";
        }
        return String.valueOf(value) + unit;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
