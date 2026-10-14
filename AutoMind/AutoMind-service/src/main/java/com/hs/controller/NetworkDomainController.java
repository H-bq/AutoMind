package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkDomain;
import com.hs.domain.vo.ReadModelVO;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.INetworkDomainService;
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
 * 网络域与控制器域信息表 前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "NetworkDomain", description = "网络域查询接口")
@RestController
@RequestMapping("/network-domain")
@RequiredArgsConstructor
public class NetworkDomainController {

    private final INetworkDomainService networkDomainService;

//    @Operation(summary = "查询网络域列表")
    @GetMapping("/list")
    public R<List<TopologyVO.DomainVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(networkDomainService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkDomain::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toDomainVO).toList());
    }

//    @Operation(summary = "根据 id 查询网络域")
    @GetMapping("/{id}")
    public R<TopologyVO.DomainVO> getById(@PathVariable("id") String id) {
        String tenantId = UserContext.requireTenantId();
        NetworkDomain domain = networkDomainService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkDomain::getTenantId, tenantId)
                .eq(NetworkDomain::getId, id)
                .one();
        return R.ok(ReadModelVO.toDomainVO(domain));
    }

}
