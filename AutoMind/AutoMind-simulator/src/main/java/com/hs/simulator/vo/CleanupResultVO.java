package com.hs.simulator.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CleanupResultVO {

    private LocalDateTime cutoffTime;
    private int deletedTelemetryRows;
    private int deletedSlaRecordRows;
}
