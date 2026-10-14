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
@Schema(description = "意图全链路追踪视图")
public class IntentFullTraceVO {
    private ReadModelVO.IntentRequestVO intent;
    private List<TimelineItemVO> timeline = new ArrayList<>();
    private Map<String, Object> summary = new LinkedHashMap<>();

    @Data
    @Accessors(chain = true)
    @Schema(description = "全链路时间线条目")
    public static class TimelineItemVO {
        private LocalDateTime time;
        private String type;
        private String summary;
        private Object payloadId;
        private Object payload;
    }
}
