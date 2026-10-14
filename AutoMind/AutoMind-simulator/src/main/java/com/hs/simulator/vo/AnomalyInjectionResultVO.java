package com.hs.simulator.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AnomalyInjectionResultVO {

    private LocalDateTime injectedAt;
    private int linkCount;
    private int sampleCountPerLink;
    private int generatedTelemetryCount;
    private int generatedSlaRecordCount;
}
