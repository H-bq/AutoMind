package com.hs.approval.aop;

import com.hs.agent.core.StructuredOutputConverter;
import com.hs.agent.dto.ParsedIntent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StructuredOutputConverterTest {

    @Test
    void parsesModelOutputWithMysqlStyleLocalDateTime() {
        String modelOutput = """
                {"bandwidthMbps":200,"confidence":0.95,"endTime":"2026-05-29 10:00:00","intentType":"video-conference","maxLatencyMs":40,"maxLossRate":null,"missingFields":["maxLossRate","protocol"],"priority":"P1","protocol":null,"sourceLocation":"北京","startTime":"2026-05-29 08:00:00","targetLocation":"上海"}
                """;

        ParsedIntent parsed = StructuredOutputConverter.of(ParsedIntent.class, BigDecimal.valueOf(0.7)).convert(modelOutput);

        assertEquals("video-conference", parsed.getIntentType());
        assertEquals(2026, parsed.getStartTime().getYear());
        assertEquals(8, parsed.getStartTime().getHour());
        assertEquals(10, parsed.getEndTime().getHour());
    }
}
