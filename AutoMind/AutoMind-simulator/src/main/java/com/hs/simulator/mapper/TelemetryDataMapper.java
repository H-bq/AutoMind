package com.hs.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hs.simulator.domain.TelemetryData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface TelemetryDataMapper extends BaseMapper<TelemetryData> {

    @Delete("DELETE FROM telemetry_data WHERE time < #{cutoff} LIMIT #{limit}")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff, @Param("limit") int limit);
}
