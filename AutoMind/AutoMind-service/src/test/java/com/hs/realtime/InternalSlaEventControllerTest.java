package com.hs.realtime;

import com.hs.common.exception.ForbiddenException;
import com.hs.controller.InternalSlaEventController;
import com.hs.realtime.dto.SlaRealtimeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InternalSlaEventControllerTest {

    @Test
    void rejectsInvalidInternalToken() {
        SlaRealtimePublisher publisher = mock(SlaRealtimePublisher.class);
        InternalSlaEventController controller = new InternalSlaEventController(publisher);
        ReflectionTestUtils.setField(controller, "internalToken", "expected-token");

        assertThrows(ForbiddenException.class,
                () -> controller.receive("bad-token", new SlaRealtimeEvent()));
        verifyNoInteractions(publisher);
    }

    @Test
    void publishesEventWhenTokenMatches() {
        SlaRealtimePublisher publisher = mock(SlaRealtimePublisher.class);
        InternalSlaEventController controller = new InternalSlaEventController(publisher);
        ReflectionTestUtils.setField(controller, "internalToken", "expected-token");
        SlaRealtimeEvent event = new SlaRealtimeEvent().setTenantId("tenant-001");

        controller.receive("expected-token", event);

        verify(publisher).publish(event);
    }
}
