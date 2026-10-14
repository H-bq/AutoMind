package com.hs.advice;

import com.hs.agent.exception.LowConfidenceException;
import com.hs.agent.exception.PevTimeoutException;
import com.hs.approval.exception.ApprovalPendingException;
import com.hs.approval.exception.ApprovalRejectedException;
import com.hs.common.domain.R;
import com.hs.common.trace.TraceIdHolder;
import com.hs.healing.exception.HumanHandoffException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class StageFourExceptionAdvice {

    @ExceptionHandler(LowConfidenceException.class)
    public ResponseEntity<R<Map<String, Object>>> handleLowConfidence(LowConfidenceException e) {
        Map<String, Object> body = baseDetails();
        body.put("actualConfidence", e.getActualConfidence());
        body.put("threshold", e.getThreshold());
        body.put("missingFields", new String[]{"confidence"});
        return ResponseEntity.status(422).body(new R<>(422, e.getMessage(), body));
    }

    @ExceptionHandler(ApprovalPendingException.class)
    public ResponseEntity<R<Map<String, Object>>> handleApprovalPending(ApprovalPendingException e) {
        Map<String, Object> body = baseDetails(e.getTraceId());
        body.put("approvalId", e.getApprovalId());
        body.put("approvalType", e.getApprovalType());
        body.put("intentId", e.getIntentId());
        body.put("agentTaskId", e.getAgentTaskId());
        return ResponseEntity.status(202).body(new R<>(202, "awaiting approval", body));
    }

    @ExceptionHandler(ApprovalRejectedException.class)
    public ResponseEntity<R<Map<String, Object>>> handleApprovalRejected(ApprovalRejectedException e) {
        Map<String, Object> body = baseDetails(e.getTraceId());
        body.put("approvalId", e.getApprovalId());
        body.put("approvalType", e.getApprovalType());
        body.put("decisionReason", e.getDecisionReason());
        return ResponseEntity.status(403).body(new R<>(403, e.getMessage(), body));
    }

    @ExceptionHandler(HumanHandoffException.class)
    public ResponseEntity<R<Map<String, Object>>> handleHumanHandoff(HumanHandoffException e) {
        Map<String, Object> body = baseDetails(e.getTraceId());
        body.put("intentId", e.getIntentId());
        body.put("lastTrigger", e.getLastTrigger());
        body.put("attemptCount", e.getAttemptCount());
        return ResponseEntity.status(503).body(new R<>(503, e.getMessage(), body));
    }

    @ExceptionHandler(PevTimeoutException.class)
    public ResponseEntity<R<Map<String, Object>>> handlePevTimeout(PevTimeoutException e) {
        return ResponseEntity.status(504).body(new R<>(504, e.getMessage(), baseDetails()));
    }

    private Map<String, Object> baseDetails() {
        return baseDetails(TraceIdHolder.get());
    }

    private Map<String, Object> baseDetails(String traceId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("traceId", traceId == null ? TraceIdHolder.get() : traceId);
        return body;
    }
}
