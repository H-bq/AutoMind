package com.hs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hs.common.domain.PageDTO;
import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.SlaEvaluateDTO;
import com.hs.domain.dto.SlaRecordQueryDTO;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.vo.SlaDashboardVO;
import com.hs.domain.vo.SlaRecordVO;
import com.hs.domain.vo.SlaVerdictVO;
import com.hs.service.ISlaRecordService;
import com.hs.sla.SlaDashboardAggregator;
import com.hs.sla.SlaEvaluator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SLA", description = "B端 SLA 仪表盘与评估接口")
@RestController
@RequestMapping("/sla")
@RequiredArgsConstructor
public class SlaController {

    private final SlaDashboardAggregator slaDashboardAggregator;
    private final SlaEvaluator slaEvaluator;
    private final ISlaRecordService slaRecordService;

    @Operation(summary = "查询 SLA 仪表盘", description = "按 automind.sla.dashboard-window 聚合达标率、指标达标率和违约趋势")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = SlaDashboardVO.class)))
    @GetMapping("/dashboard")
    public R<SlaDashboardVO> dashboard() {
        return R.ok(slaDashboardAggregator.aggregateConfiguredWindow());
    }

//    @Operation(summary = "执行单次 SLA 评估", description = "根据链路、指标和窗口读取遥测数据，写入 sla_record 并返回结论")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = SlaVerdictVO.class)))
    @PostMapping("/evaluate")
    public R<SlaVerdictVO> evaluate(
            @Parameter(description = "SLA 评估请求", required = true)
            @RequestBody SlaEvaluateDTO dto) {
        return R.ok(slaEvaluator.evaluate(dto));
    }

    @Operation(summary = "分页查询 SLA 评估历史", description = "支持按链路、指标类型和是否违约过滤")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping("/records")
    public R<PageDTO<SlaRecordVO>> records(@ParameterObject @ModelAttribute SlaRecordQueryDTO query) {
        return R.ok(slaRecordService.records(query));
    }
}
