package com.policypilot.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @Schema(description = "Reuse the id from a previous response to keep conversation memory. Leave empty to start a new conversation.",
                nullable = true)
        String conversationId,
        @Schema(example = "What is the status of my claim CLM-1023?")
        @NotBlank @Size(max = 2000) String message) {
}
