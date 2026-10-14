package com.hs.approval.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.a2a.A2aFallbackGuard;
import com.hs.a2a.A2aMessage;
import com.hs.a2a.A2aMessageType;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.AgentRunResult;
import com.hs.agent.orchestrator.IntentOrchestrationService;
import com.hs.agent.dto.ExecutionResult;
import com.hs.agent.dto.ParsedIntent;
import com.hs.agent.dto.PlanDecision;
import com.hs.agent.dto.SlaVerdict;
import com.hs.agent.impl.ExecutorAgent;
import com.hs.agent.impl.MonitorAgent;
import com.hs.agent.impl.PlannerAgent;
import com.hs.agent.impl.SupervisorAgent;
import com.hs.common.trace.TraceIdHolder;
import com.hs.domain.po.*;
import com.hs.mcp.McpCallContext;
import com.hs.mcp.McpClient;
import com.hs.mcp.McpResponse;
import com.hs.mcp.config.AutomindMcpProperties;
import com.hs.security.JwtTokenService;
import com.hs.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:stage4;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "mybatis-plus.configuration.map-underscore-to-camel-case=true",
        "automind.auth.jwt.secret=test-secret",
        "automind.auth.jwt.ttl-seconds=3600",
        "automind.approval.expire-minutes=60",
        "automind.mcp.timeout-ms=3000",
        "automind.mcp.max-retries=1",
        "automind.a2a.timeout-ms=3000",
        "aliyun.openai.api-key=test-key"
})
@AutoConfigureMockMvc
@Sql(scripts = "/stage4-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class StageFourEndToEndIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JwtTokenService jwtTokenService;
    @Autowired
    AutomindMcpProperties mcpProperties;
    @Autowired
    IAgentTaskService agentTaskService;
    @Autowired
    IIntentRequestService intentRequestService;
    @Autowired
    IApprovalRecordService approvalRecordService;
    @Autowired
    IMcpCallLogService mcpCallLogService;
    @Autowired
    IA2aMessageService a2aMessageService;
    @Autowired
    INetworkDomainService networkDomainService;
    @Autowired
    McpClient mcpClient;
    @Autowired
    A2aFallbackGuard a2aFallbackGuard;
    @Autowired
    IntentOrchestrationService orchestrationService;

    @MockBean
    ChatModel chatModel;
    @MockBean
    SupervisorAgent supervisorAgent;
    @MockBean
    PlannerAgent plannerAgent;
    @MockBean
    ExecutorAgent executorAgent;
    @MockBean
    MonitorAgent monitorAgent;

    @BeforeEach
    void setUp() {
        mcpProperties.setServerBaseUrl("http://localhost:" + port);
        Mockito.reset(supervisorAgent, plannerAgent, executorAgent, monitorAgent);
    }

    @Test
    void submitThenApproveHighRiskMcpAndQueryFullTrace() throws Exception {
        AtomicInteger executorCalls = new AtomicInteger();
        stubSuccessfulAgentFlow(executorCalls);

        String traceId = "trace-submit-approval";
        String submitBody = """
                {"inputText":"为 link-1 设置高优先级 QoS，带宽 100Mbps","priority":"P1"}
                """;
        String submitResponse = mockMvc.perform(post("/intent-request/submit")
                        .header("Authorization", bearer(10001, "tenant-001", "OPERATOR", "operator"))
                        .header("X-Trace-Id", traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.intentId").exists())
                .andReturn().getResponse().getContentAsString();
        Integer intentId = read(submitResponse).at("/data/intentId").asInt();

        ApprovalRecord pending = awaitApproval(intentId);
        assertEquals("HIGH_RISK_MCP", pending.getApprovalType());
        assertEquals("PENDING", pending.getStatus());
        assertEquals(1, mcpCallLogService.lambdaQuery()
                .eq(McpCallLog::getIntentId, intentId)
                .eq(McpCallLog::getErrorType, "PERMISSION")
                .count());

        mockMvc.perform(get("/approvals/pending")
                        .header("Authorization", bearer(10001, "tenant-001", "OPERATOR", "operator")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].status").value("PENDING"));

        mockMvc.perform(post("/approvals/{id}/approve", pending.getId())
                        .header("Authorization", bearer(1, "admin", "ADMIN", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"联调通过\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        awaitIntentStatus(intentId, "COMPLETED");
        assertTrue(executorCalls.get() >= 2, "approval resume should rerun executor");
        assertTrue(mcpCallLogService.lambdaQuery()
                .eq(McpCallLog::getIntentId, intentId)
                .eq(McpCallLog::getToolName, "set_qos")
                .eq(McpCallLog::getResponseCode, 200)
                .count() >= 1);

        mockMvc.perform(get("/intent-request/{id}/full-trace", intentId)
                        .header("Authorization", bearer(10001, "tenant-001", "OPERATOR", "operator")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.intent.id").value(intentId))
                .andExpect(jsonPath("$.data.summary.pevState").value("COMPLETED"))
                .andExpect(jsonPath("$.data.timeline[?(@.type=='APPROVAL')]").exists())
                .andExpect(jsonPath("$.data.timeline[?(@.type=='MCP_CALL')]").exists())
                .andExpect(jsonPath("$.data.timeline[?(@.type=='AGENT_TASK')]").exists());
    }

    @Test
    void mcpClientInvokesSameProcessMockServerAndWritesAuditLog() {
        AgentTask task = insertTask(7, null, "EXECUTOR", "EXECUTE", "trace-mcp-direct");
        AgentContext ctx = new AgentContext(task.getId(), null, 7, "tenant-001", "OPERATOR", 10001, "trace-mcp-direct", "EXECUTOR");

        McpResponse response = mcpClient.invoke(McpCallContext.of(ctx, "get_topo", "v1"), Map.of());

        assertEquals(200, response.httpStatus());
        assertEquals(1, mcpCallLogService.lambdaQuery()
                .eq(McpCallLog::getTraceId, "trace-mcp-direct")
                .eq(McpCallLog::getToolName, "get_topo")
                .eq(McpCallLog::getResponseCode, 200)
                .count());
    }

    @Test
    void a2aRoundTripUsesInboxAndWritesReqRespAuditMessages() {
        networkDomainService.lambdaUpdate()
                .eq(NetworkDomain::getId, "domain-remote")
                .set(NetworkDomain::getControllerEndpoint, "http://localhost:" + port)
                .update();
        AgentTask task = insertTask(8, null, "PLANNER", "PLAN", "trace-a2a");

        Map<String, Object> result = a2aFallbackGuard.sendOrFallback(
                new A2aMessage("tenant-001", "domain-local", "domain-remote", "PLANNER", "PLANNER",
                        task.getId(), "trace-a2a", A2aMessageType.REQ, Map.of("linkId", "link-1")),
                Duration.ofSeconds(3),
                response -> response.payload(),
                () -> Map.of("crossDomainDegraded", true));

        assertTrue(result.containsKey("links"));
        assertTrue(a2aMessageService.lambdaQuery()
                .eq(A2aMessagePO::getTraceId, "trace-a2a")
                .in(A2aMessagePO::getStatus, List.of("REQ", "RESP"))
                .count() >= 2);
    }

    @Test
    void approvalRejectionWritesValidJsonPayloadToAgentTask() throws Exception {
        IntentRequest intent = new IntentRequest()
                .setTenantId("tenant-001")
                .setUserId(10001)
                .setInputText("reject me")
                .setPriority(1)
                .setStatus("AWAIT_APPROVAL")
                .setTraceId("trace-reject")
                .setCreateTime(LocalDateTime.now());
        intentRequestService.save(intent);
        insertTask(intent.getId(), null, "EXECUTOR", "EXECUTE", "trace-reject");

        orchestrationService.rejectForApproval(intent.getId(), "approval timeout");

        AgentTask task = agentTaskService.lambdaQuery()
                .eq(AgentTask::getIntentId, intent.getId())
                .last("LIMIT 1")
                .one();
        assertEquals("FAILED", task.getPevState());
        assertEquals("APPROVAL_REJECTED", task.getErrorCode());
        assertEquals("REJECTED", objectMapper.readTree(task.getOutputPayload()).get("status").asText());
    }

    private void stubSuccessfulAgentFlow(AtomicInteger executorCalls) throws Exception {
        doAnswer(invocation -> {
            AgentContext ctx = invocation.getArgument(0);
            String input = invocation.getArgument(1);
            AgentTask task = insertTask(ctx.intentId(), ctx.parentTaskId(), "SUPERVISOR", "COMPLETED", ctx.traceId());
            ParsedIntent parsed = new ParsedIntent();
            parsed.setIntentType("QOS");
            parsed.setBandwidthMbps(BigDecimal.valueOf(100));
            parsed.setMaxLatencyMs(BigDecimal.valueOf(20));
            parsed.setPriority("P1");
            parsed.setConfidence(BigDecimal.valueOf(0.95));
            parsed.setMissingFields(List.of());
            task.setInputPayload(input);
            task.setOutputPayload(objectMapper.writeValueAsString(parsed));
            task.setFinishedAt(LocalDateTime.now());
            agentTaskService.updateById(task);
            return new AgentRunResult<>(ctx.withTaskId(task.getId()), parsed);
        }).when(supervisorAgent).executeWithContext(any(), any());

        doAnswer(invocation -> {
            AgentContext ctx = invocation.getArgument(0);
            AgentTask task = insertTask(ctx.intentId(), ctx.parentTaskId(), "PLANNER", "COMPLETED", ctx.traceId());
            PlanDecision plan = new PlanDecision();
            PlanDecision.LinkRef link = new PlanDecision.LinkRef();
            link.setLinkId("link-1");
            link.setLinkCode("L1");
            plan.setPrimaryPath(List.of(link));
            plan.setReservedBandwidthMbps(BigDecimal.valueOf(100));
            plan.setRiskLevel("LOW");
            plan.setRequiresApproval(false);
            plan.setCrossDomainDegraded(false);
            plan.setConfidence(BigDecimal.valueOf(0.95));
            task.setOutputPayload(objectMapper.writeValueAsString(plan));
            task.setFinishedAt(LocalDateTime.now());
            agentTaskService.updateById(task);
            return new AgentRunResult<>(ctx.withTaskId(task.getId()), plan);
        }).when(plannerAgent).executeWithContext(any(), any());

        doAnswer(invocation -> {
            AgentContext ctx = invocation.getArgument(0);
            AgentTask task = insertTask(ctx.intentId(), ctx.parentTaskId(), "EXECUTOR", "EXECUTE", ctx.traceId());
            executorCalls.incrementAndGet();
            AgentContext taskCtx = ctx.withTaskId(task.getId());
            mcpClient.invoke(McpCallContext.of(taskCtx, "set_qos", "v1"),
                    Map.of("linkId", "link-1", "reservedBandwidthMbps", 100, "priority", 1));
            ExecutionResult result = new ExecutionResult();
            result.setStatus("SUCCESS");
            result.setExecutorVia("MCP");
            result.setAffectedLinks(List.of("link-1"));
            result.setExecutedSteps(List.of("set_qos"));
            result.setConfidence(BigDecimal.valueOf(0.95));
            task.setPevState("COMPLETED");
            task.setOutputPayload(objectMapper.writeValueAsString(result));
            task.setFinishedAt(LocalDateTime.now());
            agentTaskService.updateById(task);
            return new AgentRunResult<>(taskCtx, result);
        }).when(executorAgent).executeWithContext(any(), any());

        doAnswer(invocation -> {
            AgentContext ctx = invocation.getArgument(0);
            AgentTask task = insertTask(ctx.intentId(), ctx.parentTaskId(), "MONITOR", "COMPLETED", ctx.traceId());
            SlaVerdict verdict = new SlaVerdict();
            verdict.setOverallBreach(false);
            verdict.setWorstMetric("NONE");
            verdict.setRecommendation("OK");
            verdict.setConfidence(BigDecimal.valueOf(0.95));
            task.setOutputPayload(objectMapper.writeValueAsString(verdict));
            task.setFinishedAt(LocalDateTime.now());
            agentTaskService.updateById(task);
            return new AgentRunResult<>(ctx.withTaskId(task.getId()), verdict);
        }).when(monitorAgent).executeWithContext(any(), any());
    }

    private AgentTask insertTask(Integer intentId, Long parentTaskId, String role, String state, String traceId) {
        AgentTask task = new AgentTask()
                .setTenantId("tenant-001")
                .setIntentId(intentId)
                .setParentTaskId(parentTaskId)
                .setTraceId(traceId)
                .setAgentRole(role)
                .setTaskType(role + "_TEST")
                .setPevState(state)
                .setRetryCount(0)
                .setStartedAt(LocalDateTime.now())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        agentTaskService.save(task);
        return task;
    }

    private ApprovalRecord awaitApproval(Integer intentId) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            ApprovalRecord record = approvalRecordService.lambdaQuery()
                    .eq(ApprovalRecord::getIntentId, intentId)
                    .eq(ApprovalRecord::getStatus, "PENDING")
                    .last("LIMIT 1")
                    .one();
            if (record != null) {
                return record;
            }
            Thread.sleep(100);
        }
        fail("approval was not created for intent " + intentId);
        return null;
    }

    private void awaitIntentStatus(Integer intentId, String expectedStatus) throws InterruptedException {
        for (int i = 0; i < 80; i++) {
            IntentRequest intent = intentRequestService.getById(intentId);
            if (intent != null && expectedStatus.equals(intent.getStatus())) {
                return;
            }
            Thread.sleep(100);
        }
        IntentRequest latest = intentRequestService.getById(intentId);
        fail("intent did not reach " + expectedStatus + ", latest=" + (latest == null ? null : latest.getStatus()));
    }

    private String bearer(Integer userId, String tenantId, String role, String username) {
        return "Bearer " + jwtTokenService.createToken(userId, tenantId, role, username);
    }

    private com.fasterxml.jackson.databind.JsonNode read(String json) throws Exception {
        return objectMapper.readTree(json);
    }
}
