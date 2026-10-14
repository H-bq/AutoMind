package com.hs.service.impl;

import com.hs.domain.po.AuditLog;
import com.hs.mapper.AuditLogMapper;
import com.hs.service.IAuditLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {

}
