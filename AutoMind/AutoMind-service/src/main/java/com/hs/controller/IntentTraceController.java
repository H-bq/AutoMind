package com.hs.controller;

import com.hs.common.domain.R;
import com.hs.domain.vo.IntentFullTraceVO;
import com.hs.service.IntentFullTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "IntentFullTrace", description = "意图全链路总览接口")
@RestController
@RequiredArgsConstructor
public class IntentTraceController {

    private final IntentFullTraceService intentFullTraceService;

    @Operation(summary = "按意图 ID 查询全链路总览")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = IntentFullTraceVO.class)))
    @GetMapping("/intent-request/{intentId}/full-trace")
    public R<IntentFullTraceVO> byIntent(@Parameter(description = "意图 ID", required = true) @PathVariable Integer intentId) {
        return R.ok(intentFullTraceService.byIntentId(intentId));
    }

    @Operation(summary = "按 TraceId 查询全链路总览")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = IntentFullTraceVO.class)))
    @GetMapping("/trace/{traceId}")
    public R<IntentFullTraceVO> byTrace(@Parameter(description = "TraceId", required = true) @PathVariable String traceId) {
        return R.ok(intentFullTraceService.byTraceId(traceId));
    }
}
