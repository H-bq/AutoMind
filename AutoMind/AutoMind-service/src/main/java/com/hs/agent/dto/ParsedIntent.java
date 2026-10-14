package com.hs.agent.dto;

import com.hs.agent.core.Confident;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Supervisor Agent 结构化意图解析结果")
public class ParsedIntent implements Confident {

    private String intentType;
    private String sourceLocation;
    private String targetLocation;
    private String protocol;
    private BigDecimal bandwidthMbps;
    private BigDecimal maxLatencyMs;
    private BigDecimal maxLossRate;
    private String priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal confidence;
    private List<String> missingFields = new ArrayList<>();
}
