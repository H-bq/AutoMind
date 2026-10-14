package com.hs.approval.exception;

import com.hs.approval.ApprovalType;
import com.hs.common.exception.CommonException;
import lombok.Getter;

@Getter
public class ApprovalRejectedException extends CommonException {

    private final Long approvalId;
    private final ApprovalType approvalType;
    private final String decisionReason;
    private final String traceId;

    public ApprovalRejectedException(Long approvalId, ApprovalType approvalType, String decisionReason, String traceId) {
        super("approval rejected", 403);
        this.approvalId = approvalId;
        this.approvalType = approvalType;
        this.decisionReason = decisionReason;
        this.traceId = traceId;
    }
}
