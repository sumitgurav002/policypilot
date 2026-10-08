package com.policypilot.rag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
        @Schema(example = "Is flood damage covered under this policy?")
        @NotBlank @Size(max = 2000) String question,
        @Schema(description = "Optional: only search this document id", nullable = true)
        String documentId) {
}
