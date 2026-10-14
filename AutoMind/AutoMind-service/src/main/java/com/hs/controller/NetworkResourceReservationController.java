package com.hs.controller;


import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkResourceReservation;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.INetworkResourceReservationService;
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
 * 网络资源预留记录表 前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "NetworkResourceReservation", description = "网络资源预留查询接口")
@RestController
@RequestMapping("/network-resource-reservation")
@RequiredArgsConstructor
public class NetworkResourceReservationController {

    private final INetworkResourceReservationService networkResourceReservationService;

//    @Operation(summary = "查询资源预留列表")
    @GetMapping("/list")
    public R<List<ReadModelVO.NetworkResourceReservationVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(networkResourceReservationService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkResourceReservation::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toNetworkResourceReservationVO).toList());
    }

//    @Operation(summary = "根据 id 查询资源预留")
    @GetMapping("/{id}")
    public R<ReadModelVO.NetworkResourceReservationVO> getById(@PathVariable("id") String id) {
        String tenantId = UserContext.requireTenantId();
        NetworkResourceReservation reservation = networkResourceReservationService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkResourceReservation::getTenantId, tenantId)
                .eq(NetworkResourceReservation::getId, id)
                .one();
        return R.ok(ReadModelVO.toNetworkResourceReservationVO(reservation));
    }

}
