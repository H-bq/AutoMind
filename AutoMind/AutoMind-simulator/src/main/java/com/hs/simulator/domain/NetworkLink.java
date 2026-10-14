package com.hs.simulator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@TableName("network_link")
public class NetworkLink {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("link_code")
    private String linkCode;

    @TableField("max_latency_ms")
    private BigDecimal maxLatencyMs;

    @TableField("status")
    private String status;
}
