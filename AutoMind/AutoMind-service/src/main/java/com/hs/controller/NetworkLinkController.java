package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkLink;
import com.hs.domain.vo.ReadModelVO;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.INetworkLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 网络链路拓扑与基础容量信息表 前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "NetworkLink", description = "网络链路查询接口")
@RestController
@RequestMapping("/network-link")
@RequiredArgsConstructor
public class NetworkLinkController {

    private final INetworkLinkService networkLinkService;

//    @Operation(summary = "查询网络链路列表")
    @GetMapping("/list")
    public R<List<TopologyVO.LinkVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toLinkVO).toList());
    }

//    @Operation(summary = "根据 id 查询网络链路")
    @GetMapping("/{id}")
    public R<TopologyVO.LinkVO> getById(@PathVariable("id") String id) {
        String tenantId = UserContext.requireTenantId();
        NetworkLink link = networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .eq(NetworkLink::getId, id)
                .one();
        return R.ok(ReadModelVO.toLinkVO(link));
    }

}
