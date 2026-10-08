package com.policypilot.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, UUID> {

    List<PolicyDocument> findAllByOrderByCreatedAtDesc();
}
