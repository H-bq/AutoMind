package com.hs.service.impl;

import com.hs.domain.po.NetworkNode;
import com.hs.mapper.NetworkNodeMapper;
import com.hs.service.INetworkNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 网络节点信息表 服务实现类
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Service
public class NetworkNodeServiceImpl extends ServiceImpl<NetworkNodeMapper, NetworkNode> implements INetworkNodeService {

}
