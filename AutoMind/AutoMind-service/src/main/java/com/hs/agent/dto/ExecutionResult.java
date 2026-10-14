package com.hs.agent.dto;

import com.hs.agent.core.Confident;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Executor Agent 执行结果")
public class ExecutionResult implements Confident {

    private List<String> executedSteps = new ArrayList<>();
    private List<String> affectedLinks = new ArrayList<>();
    private String reservationId;
    private String executorVia;
    private String status;
    private String errorCode;
    private BigDecimal confidence;
}
