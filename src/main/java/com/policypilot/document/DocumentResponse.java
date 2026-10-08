package com.policypilot.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, String fileName, DocumentStatus status, Integer chunkCount,
                               String errorMessage, Instant createdAt, Instant updatedAt) {

    public static DocumentResponse from(PolicyDocument d) {
        return new DocumentResponse(d.getId(), d.getFileName(), d.getStatus(), d.getChunkCount(),
                d.getErrorMessage(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
