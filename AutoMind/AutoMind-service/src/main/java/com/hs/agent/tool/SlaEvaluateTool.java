package com.hs.agent.tool;

import com.hs.common.utils.UserContext;
import com.hs.domain.vo.SlaVerdictVO;
import com.hs.sla.SlaEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlaEvaluateTool {

    private final SlaEvaluator slaEvaluator;

    @Tool(name = "evaluateSla", description = "按链路、指标和窗口执行 SLA 评估并写入 sla_record")
    public SlaVerdictVO evaluate(String linkId, String metricType, String window) {
        UserContext.requireTenantId();
        return slaEvaluator.evaluate(linkId, metricType, window);
    }
}
