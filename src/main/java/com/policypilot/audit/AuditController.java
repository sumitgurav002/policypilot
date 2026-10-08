package com.policypilot.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "4. Audit", description = "See which tools the agent called")
public class AuditController {

    private final ToolAuditLogRepository repository;

    public AuditController(ToolAuditLogRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Recent tool calls (latest 50), or all calls for one conversation")
    @GetMapping("/tool-calls")
    public List<ToolAuditLog> toolCalls(@RequestParam(required = false) String conversationId) {
        if (conversationId != null && !conversationId.isBlank()) {
            return repository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        }
        return repository.findTop50ByOrderByCreatedAtDesc();
    }
}
