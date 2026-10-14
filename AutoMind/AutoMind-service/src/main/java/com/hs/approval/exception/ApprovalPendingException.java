package com.hs.approval.exception;

import com.hs.approval.ApprovalType;
import com.hs.common.exception.CommonException;
import lombok.Getter;

@Getter
public class ApprovalPendingException extends CommonException {

    private final Long approvalId;
    private final ApprovalType approvalType;
    private final Integer intentId;
    private final Long agentTaskId;
    private final String traceId;

    public ApprovalPendingException(Long approvalId, ApprovalType approvalType, Integer intentId, Long agentTaskId, String traceId) {
        super("awaiting approval", 202);
        this.approvalId = approvalId;
        this.approvalType = approvalType;
        this.intentId = intentId;
        this.agentTaskId = agentTaskId;
        this.traceId = traceId;
    }
}
