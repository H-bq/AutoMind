package com.hs.a2a;

import com.hs.common.exception.ForbiddenException;
import com.hs.domain.po.NetworkLinkResource;
import com.hs.service.INetworkLinkResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "A2A Inbox", description = "阶段三同进程 A2A 消息接收端")
@RestController
@RequiredArgsConstructor
public class A2aMessageReceiver {

    private final A2aPayloadSanitizer sanitizer;
    private final A2aMessageSender sender;
    private final INetworkLinkResourceService linkResourceService;

    @Operation(summary = "接收 A2A 消息", description = "接收外域链路状态查询消息并返回脱敏链路概要")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("${automind.a2a.inbox-path:/a2a/inbox}")
    public ResponseEntity<Map<String, Object>> inbox(@RequestHeader("X-Trace-Id") String traceId,
                                                     @RequestHeader("X-Tenant-Id") String tenantId,
                                                     @RequestBody A2aMessage request) {
        if (traceId == null || traceId.isBlank() || tenantId == null || tenantId.isBlank()) {
            throw new ForbiddenException("A2A 消息缺少必要上下文");
        }
        A2aMessage req = request.withPayload(sanitizer.sanitize(request.payload()));
        sender.saveMessage(req.withType(A2aMessageType.REQ), "REQ", null, LocalDateTime.now(), LocalDateTime.now());
        List<Map<String, Object>> links = linkResourceService.lambdaQuery()
                .eq(NetworkLinkResource::getTenantId, tenantId)
                .last("limit 20")
                .list()
                .stream()
                .map(resource -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("linkId", resource.getLinkId());
                    payload.put("latencyMs", resource.getCurrentLatencyMs());
                    payload.put("packetLossRate", resource.getCurrentPacketLossRate());
                    payload.put("availableBandwidthMbps", resource.getAvailableBandwidthMbps());
                    payload.put("updatedAt", resource.getSampledAt());
                    return sanitizer.sanitize(payload);
                })
                .toList();
        return ResponseEntity.ok(Map.of("links", links));
    }
}
