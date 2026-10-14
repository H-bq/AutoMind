package com.hs.simulator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.mapper.NetworkLinkMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryGeneratorService {

    private final NetworkLinkMapper networkLinkMapper;
    private final TelemetryDataMapper telemetryDataMapper;
    private final TelemetrySampleFactory telemetrySampleFactory;

    public int generateOnce() {
        List<NetworkLink> links = networkLinkMapper.selectList(new LambdaQueryWrapper<NetworkLink>()
                .isNotNull(NetworkLink::getTenantId)
                .isNotNull(NetworkLink::getId));
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (NetworkLink link : links) {
            telemetryDataMapper.insert(telemetrySampleFactory.create(link, now));
            count++;
        }
        return count;
    }
}
