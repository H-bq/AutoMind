package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "意图 PEV 轨迹")
public class IntentTraceVO {

    private Integer intentId;
    private String traceId;
    private List<AgentTaskVO> tasks = new ArrayList<>();
}
