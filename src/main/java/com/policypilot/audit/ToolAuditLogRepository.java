package com.policypilot.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolAuditLogRepository extends JpaRepository<ToolAuditLog, Long> {

    List<ToolAuditLog> findTop50ByOrderByCreatedAtDesc();

    List<ToolAuditLog> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}
