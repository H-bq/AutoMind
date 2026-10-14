package com.hs.agent.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.TelemetryData;
import com.hs.service.ITelemetryDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TelemetryQueryTool {

    private final ITelemetryDataService telemetryDataService;

    @Tool(name = "queryTelemetry", description = "查询当前租户指定链路最近窗口内的遥测采样，window 示例 5m/1h")
    public List<TelemetryData> recent(String linkId, String window) {
        String tenantId = UserContext.requireTenantId();
        Duration duration = parseWindow(window);
        return telemetryDataService.list(new LambdaQueryWrapper<TelemetryData>()
                .eq(!UserContext.isAdminTenant(tenantId), TelemetryData::getTenantId, tenantId)
                .eq(TelemetryData::getLinkId, linkId)
                .ge(TelemetryData::getTime, LocalDateTime.now().minus(duration))
                .orderByDesc(TelemetryData::getTime)
                .last("limit 20"));
    }

    private Duration parseWindow(String window) {
        String value = (window == null || window.isBlank() ? "5m" : window).trim().toLowerCase(Locale.ROOT);
        if (value.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        if (value.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        if (value.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        return Duration.ofMinutes(5);
    }
}
