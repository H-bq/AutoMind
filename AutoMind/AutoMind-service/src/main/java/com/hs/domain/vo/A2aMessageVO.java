package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "A2A 跨域消息视图")
public class A2aMessageVO {

    private Long id;
    private String tenantId;
    private String sourceDomainId;
    private String targetDomainId;
    private String sourceAgent;
    private String targetAgent;
    private Long taskId;
    private String traceId;
    private String messageType;
    private String payload;
    private String status;
    private Integer latencyMs;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
}
