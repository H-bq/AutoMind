package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(description = "Agent 执行时间线视图")
public class AgentExecutionTimelineVO {

    private Integer intentId;
    private String tenantId;
    private String traceId;
    private String inputText;
    private String status;
    private LocalDateTime createTime;
    private Map<String, Object> summary = new LinkedHashMap<>();
    private List<EventVO> events = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    @Schema(description = "Agent 执行时间线事件")
    public static class EventVO {
        private LocalDateTime eventTime;
        private String eventType;
        private String stage;
        private String status;
        private String title;
        private String summary;
        private Object payloadId;
        private Long agentTaskId;
        private Object payload;
    }
}
