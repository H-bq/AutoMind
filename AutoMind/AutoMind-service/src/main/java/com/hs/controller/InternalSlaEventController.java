package com.hs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.domain.R;
import com.hs.common.exception.ForbiddenException;
import com.hs.domain.po.Person;
import com.hs.realtime.SlaRealtimePublisher;
import com.hs.realtime.dto.SlaRealtimeEvent;
import com.hs.service.IPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "InternalSlaEvents", description = "内部 SLA 事件接收接口")
@RestController
@RequestMapping("/internal/sla-events")
@RequiredArgsConstructor
public class InternalSlaEventController {

    private final SlaRealtimePublisher slaRealtimePublisher;

    @Autowired
    private IPersonService personService;

    @Value("${automind.realtime.internal-token:automind-internal-dev-token}")
    private String internalToken;

    @Operation(summary = "接收内部 SLA 实时事件", description = "供 AutoMind-simulator 调用，校验内部令牌后推送给 SSE 客户端")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = R.class)))
    @PostMapping
    public R<Void> receive(
            @Parameter(description = "内部服务令牌", required = true)
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @Parameter(description = "实时 SLA 事件", required = true)
            @RequestBody SlaRealtimeEvent event) {
        if (token == null || !token.equals(internalToken)) {
            throw new ForbiddenException("内部 SLA 事件令牌无效");
        }
        if (event.getSource() == null || event.getSource().isBlank()) {
            event.setSource("SIMULATOR");
        }
        if (event.getTenantId() != null && !event.getTenantId().isBlank()) {
            Person person = personService.getOne(new LambdaQueryWrapper<Person>().eq(Person::getTenantId, event.getTenantId()), false);
            if (person != null) {
                event.setPosition(person.getPosition());
            }
        }
        slaRealtimePublisher.publish(event);
        return R.ok();
    }
}
