package com.hs.approval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalExpireScheduler {

    private final ApprovalService approvalService;

    @Scheduled(cron = "${automind.approval.expire-scan-cron:0 * * * * *}")
    public void markExpired() {
        int count = approvalService.markExpired();
        if (count > 0) {
            log.info("Expired approvals marked count={}", count);
        }
    }
}
