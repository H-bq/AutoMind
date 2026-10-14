package com.hs.controller;

import com.hs.common.domain.PageDTO;
import com.hs.common.domain.R;
import com.hs.domain.dto.HealingRecordQueryDTO;
import com.hs.domain.vo.HealingRecordVO;
import com.hs.service.IHealingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "HealingRecord", description = "自愈记录接口")
@RestController
@RequestMapping("/healing-records")
@RequiredArgsConstructor
public class HealingRecordController {

    private final IHealingRecordService healingRecordService;

    @Operation(summary = "分页查询自愈记录")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping
    public R<PageDTO<HealingRecordVO>> list(@ParameterObject HealingRecordQueryDTO query) {
        return R.ok(healingRecordService.pageRecords(query));
    }

    @Operation(summary = "查询自愈记录详情")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = HealingRecordVO.class)))
    @GetMapping("/{id}")
    public R<HealingRecordVO> detail(@Parameter(description = "自愈记录 ID", required = true) @PathVariable Long id) {
        return R.ok(healingRecordService.detail(id));
    }

    @Operation(summary = "按意图查询自愈记录")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = HealingRecordVO.class)))
    @GetMapping("/by-intent/{intentId}")
    public R<List<HealingRecordVO>> byIntent(@Parameter(description = "意图 ID", required = true) @PathVariable Integer intentId) {
        return R.ok(healingRecordService.byIntent(intentId));
    }
}
