package com.hs.approval.event;

import com.hs.domain.po.ApprovalRecord;
import org.springframework.context.ApplicationEvent;

public class ApprovalDecidedEvent extends ApplicationEvent {

    private final ApprovalRecord approval;

    public ApprovalDecidedEvent(Object source, ApprovalRecord approval) {
        super(source);
        this.approval = approval;
    }

    public ApprovalRecord approval() {
        return approval;
    }
}
