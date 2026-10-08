package com.policypilot.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** One row per tool the agent called: what, with which (masked) arguments, outcome and latency. */
@Entity
@Table(name = "tool_audit_log")
public class ToolAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String conversationId;
    private String toolName;
    @Column(length = 1000)
    private String arguments;
    private String outcome;
    @Column(length = 1000)
    private String detail;
    private long latencyMs;
    private Instant createdAt;

    protected ToolAuditLog() {
    }

    public ToolAuditLog(String conversationId, String toolName, String arguments,
                        String outcome, String detail, long latencyMs) {
        this.conversationId = conversationId;
        this.toolName = toolName;
        this.arguments = truncate(arguments);
        this.outcome = outcome;
        this.detail = truncate(detail);
        this.latencyMs = latencyMs;
        this.createdAt = Instant.now();
    }

    private static String truncate(String s) {
        return s == null ? null : s.substring(0, Math.min(s.length(), 1000));
    }

    public Long getId() { return id; }
    public String getConversationId() { return conversationId; }
    public String getToolName() { return toolName; }
    public String getArguments() { return arguments; }
    public String getOutcome() { return outcome; }
    public String getDetail() { return detail; }
    public long getLatencyMs() { return latencyMs; }
    public Instant getCreatedAt() { return createdAt; }
}
