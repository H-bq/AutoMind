package com.hs.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hs.simulator.domain.SlaRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface SlaRecordMapper extends BaseMapper<SlaRecord> {

    @Delete("DELETE FROM sla_record WHERE evaluated_at < #{cutoff} LIMIT #{limit}")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff, @Param("limit") int limit);
}
