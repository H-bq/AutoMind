package com.hs.simulator.controller;

import com.hs.common.domain.R;
import com.hs.common.exception.ForbiddenException;
import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.dto.AnomalyGenerateDTO;
import com.hs.simulator.service.AnomalyInjectionService;
import com.hs.simulator.service.RetentionCleanupService;
import com.hs.simulator.service.SimulatorStateService;
import com.hs.simulator.vo.AnomalyInjectionResultVO;
import com.hs.simulator.vo.CleanupResultVO;
import com.hs.simulator.vo.SimulatorStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Simulator", description = "SLA 模拟器运维接口")
@RestController
@RequestMapping("/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorProperties properties;
    private final SimulatorStateService simulatorStateService;
    private final RetentionCleanupService retentionCleanupService;
    private final AnomalyInjectionService anomalyInjectionService;

    @Operation(summary = "查询模拟器状态")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = SimulatorStatusVO.class)))
    @GetMapping("/status")
    public R<SimulatorStatusVO> status() {
        return R.ok(simulatorStateService.status());
    }

    @Operation(summary = "启动模拟器")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = SimulatorStatusVO.class)))
    @PostMapping("/start")
    public R<SimulatorStatusVO> start(
            @Parameter(description = "模拟器运维令牌", required = true)
            @RequestHeader(value = "X-Simulator-Admin-Token", required = false) String token) {
        requireAdminToken(token);
        simulatorStateService.start();
        return R.ok(simulatorStateService.status());
    }

    @Operation(summary = "停止模拟器")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = SimulatorStatusVO.class)))
    @PostMapping("/stop")
    public R<SimulatorStatusVO> stop(
            @Parameter(description = "模拟器运维令牌", required = true)
            @RequestHeader(value = "X-Simulator-Admin-Token", required = false) String token) {
        requireAdminToken(token);
        simulatorStateService.stop();
        return R.ok(simulatorStateService.status());
    }

    @Operation(summary = "手动清理过期模拟数据")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = CleanupResultVO.class)))
    @PostMapping("/cleanup/run")
    public R<CleanupResultVO> cleanup(
            @Parameter(description = "模拟器运维令牌", required = true)
            @RequestHeader(value = "X-Simulator-Admin-Token", required = false) String token) {
        requireAdminToken(token);
        CleanupResultVO result = retentionCleanupService.cleanupExpired();
        simulatorStateService.recordCleanup(result);
        return R.ok(result);
    }

    @Operation(summary = "生成异常网络数据", description = "仅用于演示，插入异常遥测采样并立即生成 SLA 违约记录和实时事件")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = AnomalyInjectionResultVO.class)))
    @PostMapping("/anomaly/generate")
    public R<AnomalyInjectionResultVO> generateAnomaly(
            @Parameter(description = "模拟器运维令牌", required = true) @RequestHeader(value = "X-Simulator-Admin-Token", required = false) String token,
            @Parameter(description = "异常生成参数；为空时对全部链路生成默认数量异常采样") @RequestBody(required = false) AnomalyGenerateDTO request) {
        requireAdminToken(token);
        return R.ok(anomalyInjectionService.generate(request));
    }

    private void requireAdminToken(String token) {
        if (token == null || !token.equals(properties.getAdminToken())) {
            throw new ForbiddenException("模拟器运维令牌无效");
        }
    }
}
