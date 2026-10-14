package com.hs.controller;

import com.hs.common.domain.PageDTO;
import com.hs.common.domain.R;
import com.hs.domain.dto.A2aMessageQueryDTO;
import com.hs.domain.vo.A2aMessageVO;
import com.hs.service.IA2aMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "A2A 消息审计", description = "A2A 跨域消息查询接口")
@RestController
@RequestMapping("/a2a-messages")
@RequiredArgsConstructor
public class A2aMessageController {

    private final IA2aMessageService a2aMessageService;

    @Operation(summary = "分页查询 A2A 消息", description = "按 traceId、源/目标网络域、消息类型、状态和时间窗过滤")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping
    public R<PageDTO<A2aMessageVO>> list(@ParameterObject @ModelAttribute A2aMessageQueryDTO query) {
        return R.ok(a2aMessageService.pageMessages(query));
    }

    @Operation(summary = "查询 A2A 消息详情")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = A2aMessageVO.class)))
    @GetMapping("/{id}")
    public R<A2aMessageVO> detail(
            @Parameter(description = "A2A 消息 ID", required = true)
            @PathVariable("id") Long id) {
        return R.ok(a2aMessageService.detail(id));
    }

    @Operation(summary = "按 traceId 查询 A2A 消息")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = A2aMessageVO.class)))
    @GetMapping("/by-trace/{traceId}")
    public R<List<A2aMessageVO>> byTrace(
            @Parameter(description = "全链路 TraceId", required = true)
            @PathVariable("traceId") String traceId) {
        return R.ok(a2aMessageService.byTrace(traceId));
    }
}
