package com.policypilot.audit;

import com.policypilot.guard.PiiMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/** Wraps every tool call: times it, masks PII in arguments, and stores an audit row. */
@Service
public class ToolAuditService {

    private static final Logger log = LoggerFactory.getLogger(ToolAuditService.class);

    private final ToolAuditLogRepository repository;

    public ToolAuditService(ToolAuditLogRepository repository) {
        this.repository = repository;
    }

    public <T> T audited(String conversationId, String toolName, String arguments, Supplier<T> call) {
        long start = System.currentTimeMillis();
        String maskedArgs = PiiMasker.mask(arguments);
        try {
            T result = call.get();
            save(conversationId, toolName, maskedArgs, "SUCCESS", null, start);
            return result;
        } catch (RuntimeException e) {
            save(conversationId, toolName, maskedArgs, "ERROR", e.getMessage(), start);
            throw e;
        }
    }

    private void save(String conversationId, String toolName, String args, String outcome, String detail, long start) {
        long latency = System.currentTimeMillis() - start;
        log.info("tool={} conversation={} args={} outcome={} latencyMs={}", toolName, conversationId, args, outcome, latency);
        try {
            repository.save(new ToolAuditLog(conversationId, toolName, args, outcome, PiiMasker.mask(detail), latency));
        } catch (RuntimeException e) {
            log.warn("Could not write tool audit log", e); // auditing must never break the user's request
        }
    }
}
