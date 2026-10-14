package com.hs.realtime;

import com.hs.common.exception.UnauthorizedException;
import com.hs.controller.SlaRealtimeController;
import com.hs.security.JwtClaims;
import com.hs.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SlaRealtimeControllerTest {

    @Test
    void subscribesWithJwtQueryToken() {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        SlaRealtimePublisher publisher = mock(SlaRealtimePublisher.class);
        SseEmitter emitter = new SseEmitter();
        when(jwtTokenService.parseBearerToken("Bearer token-1"))
                .thenReturn(new JwtClaims(10001, "tenant-001", "OPERATOR", "operator"));
        when(publisher.subscribe("tenant-001", false)).thenReturn(emitter);
        SlaRealtimeController controller = new SlaRealtimeController(jwtTokenService, publisher);

        controller.stream("token-1");

        verify(publisher).subscribe("tenant-001", false);
    }

    @Test
    void rejectsInvalidJwtQueryToken() {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        SlaRealtimePublisher publisher = mock(SlaRealtimePublisher.class);
        when(jwtTokenService.parseBearerToken(anyString())).thenThrow(new UnauthorizedException("invalid token"));
        SlaRealtimeController controller = new SlaRealtimeController(jwtTokenService, publisher);

        assertThrows(UnauthorizedException.class, () -> controller.stream("bad-token"));
        verifyNoInteractions(publisher);
    }
}
