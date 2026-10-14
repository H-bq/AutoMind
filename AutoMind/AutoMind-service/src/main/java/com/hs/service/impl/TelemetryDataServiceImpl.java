package com.hs.service.impl;

import com.hs.domain.po.TelemetryData;
import com.hs.mapper.TelemetryDataMapper;
import com.hs.service.ITelemetryDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 网络链路遥测数据表 服务实现类
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Service
public class TelemetryDataServiceImpl extends ServiceImpl<TelemetryDataMapper, TelemetryData> implements ITelemetryDataService {

}
