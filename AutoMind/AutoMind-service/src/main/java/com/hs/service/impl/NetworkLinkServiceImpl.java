package com.hs.service.impl;

import com.hs.domain.po.NetworkLink;
import com.hs.mapper.NetworkLinkMapper;
import com.hs.service.INetworkLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 网络链路拓扑与基础容量信息表 服务实现类
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Service
public class NetworkLinkServiceImpl extends ServiceImpl<NetworkLinkMapper, NetworkLink> implements INetworkLinkService {

}
