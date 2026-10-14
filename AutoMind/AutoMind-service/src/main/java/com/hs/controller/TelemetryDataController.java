package com.hs.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.TelemetryData;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.ITelemetryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 网络链路遥测数据表 前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "TelemetryData", description = "网络链路遥测数据查询接口")
@RestController
@RequestMapping("/telemetry-data")
@RequiredArgsConstructor
public class TelemetryDataController {

    private final ITelemetryDataService telemetryDataService;

//    @Operation(summary = "查询遥测数据列表")
    @GetMapping("/list")
    public R<List<ReadModelVO.TelemetryDataVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(telemetryDataService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), TelemetryData::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toTelemetryDataVO).toList());
    }

//    @Operation(summary = "根据链路 id 查询遥测数据")
    @GetMapping("/link/{linkId}")
    public R<List<ReadModelVO.TelemetryDataVO>> listByLinkId(
            @Parameter(description = "链路标识") @PathVariable("linkId") String linkId) {
        String tenantId = UserContext.requireTenantId();
        List<TelemetryData> data = telemetryDataService.list(
                Wrappers.<TelemetryData>lambdaQuery()
                        .eq(!UserContext.isAdminTenant(tenantId), TelemetryData::getTenantId, tenantId)
                        .eq(TelemetryData::getLinkId, linkId)
                        .orderByDesc(TelemetryData::getTime));
        return R.ok(data.stream().map(ReadModelVO::toTelemetryDataVO).toList());
    }

}
