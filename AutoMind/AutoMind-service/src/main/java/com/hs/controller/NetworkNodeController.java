package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkNode;
import com.hs.domain.vo.ReadModelVO;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.INetworkNodeService;
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
 * 网络节点信息表 前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "NetworkNode", description = "网络节点查询接口")
@RestController
@RequestMapping("/network-node")
@RequiredArgsConstructor
public class NetworkNodeController {

    private final INetworkNodeService networkNodeService;

//    @Operation(summary = "查询网络节点列表")
    @GetMapping("/list")
    public R<List<TopologyVO.NodeVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(networkNodeService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkNode::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toNodeVO).toList());
    }

//    @Operation(summary = "根据 id 查询网络节点")
    @GetMapping("/{id}")
    public R<TopologyVO.NodeVO> getById(@PathVariable("id") String id) {
        String tenantId = UserContext.requireTenantId();
        NetworkNode node = networkNodeService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkNode::getTenantId, tenantId)
                .eq(NetworkNode::getId, id)
                .one();
        return R.ok(ReadModelVO.toNodeVO(node));
    }

}
