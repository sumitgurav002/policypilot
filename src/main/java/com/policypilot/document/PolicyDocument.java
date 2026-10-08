package com.policypilot.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Tracks an uploaded PDF through the ingestion pipeline. */
@Entity
@Table(name = "policy_documents")
public class PolicyDocument {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private Integer chunkCount;

    @Column(length = 1000)
    private String errorMessage;

    private Instant createdAt;
    private Instant updatedAt;

    protected PolicyDocument() {
    }

    public PolicyDocument(UUID id, String fileName, String storagePath) {
        this.id = id;
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.status = DocumentStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void markProcessing() {
        status = DocumentStatus.PROCESSING;
        errorMessage = null;
    }

    public void markIndexed(int chunks) {
        status = DocumentStatus.INDEXED;
        chunkCount = chunks;
        errorMessage = null;
    }

    public void markFailed(String reason) {
        status = DocumentStatus.FAILED;
        errorMessage = reason == null ? null : reason.substring(0, Math.min(reason.length(), 1000));
    }

    public UUID getId() { return id; }
    public String getFileName() { return fileName; }
    public String getStoragePath() { return storagePath; }
    public DocumentStatus getStatus() { return status; }
    public Integer getChunkCount() { return chunkCount; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
