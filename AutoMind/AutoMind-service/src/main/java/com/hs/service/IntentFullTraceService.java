package com.hs.service;

import com.hs.domain.vo.IntentFullTraceVO;

public interface IntentFullTraceService {

    IntentFullTraceVO byIntentId(Integer intentId);

    IntentFullTraceVO byTraceId(String traceId);
}
