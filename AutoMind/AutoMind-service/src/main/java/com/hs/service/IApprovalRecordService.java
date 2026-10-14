package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.ApprovalQueryDTO;
import com.hs.domain.po.ApprovalRecord;
import com.hs.domain.vo.ApprovalDetailVO;
import com.hs.domain.vo.MyApprovalProgressVO;
import com.hs.domain.vo.ApprovalRecordVO;

public interface IApprovalRecordService extends IService<ApprovalRecord> {

    PageDTO<ApprovalRecordVO> pageApprovals(ApprovalQueryDTO query, boolean pendingOnly);

    PageDTO<MyApprovalProgressVO> pageMyProgress(ApprovalQueryDTO query);

    ApprovalDetailVO detail(Long id);
}
