package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkLinkResource;
import com.hs.domain.vo.ReadModelVO;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.INetworkLinkResourceService;
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
 * 链路当前资源与 QoS 状态表 前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "NetworkLinkResource", description = "链路资源与 QoS 状态查询接口")
@RestController
@RequestMapping("/network-link-resource")
@RequiredArgsConstructor
public class NetworkLinkResourceController {

    private final INetworkLinkResourceService networkLinkResourceService;

//    @Operation(summary = "查询链路资源列表")
    @GetMapping("/list")
    public R<List<TopologyVO.LinkResourceVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(networkLinkResourceService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toLinkResourceVO).toList());
    }

//    @Operation(summary = "根据 id 查询链路资源")
    @GetMapping("/{id}")
    public R<TopologyVO.LinkResourceVO> getById(@PathVariable("id") String id) {
        String tenantId = UserContext.requireTenantId();
        NetworkLinkResource resource = networkLinkResourceService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                .eq(NetworkLinkResource::getId, id)
                .one();
        return R.ok(ReadModelVO.toLinkResourceVO(resource));
    }

}
