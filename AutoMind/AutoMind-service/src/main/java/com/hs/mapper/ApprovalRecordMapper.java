package com.hs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hs.domain.dto.ApprovalQueryDTO;
import com.hs.domain.po.ApprovalRecord;
import com.hs.domain.vo.ApprovalDetailVO;
import com.hs.domain.vo.MyApprovalProgressVO;
import org.apache.ibatis.annotations.Param;

public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    ApprovalDetailVO selectDetail(@Param("id") Long id,
                                  @Param("tenantId") String tenantId,
                                  @Param("adminTenant") boolean adminTenant);

    Page<MyApprovalProgressVO> selectMyProgressPage(Page<MyApprovalProgressVO> page,
                                                    @Param("query") ApprovalQueryDTO query,
                                                    @Param("tenantId") String tenantId,
                                                    @Param("userId") Integer userId);
}
