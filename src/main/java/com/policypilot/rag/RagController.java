package com.policypilot.rag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ask")
@Tag(name = "2. Ask (RAG)", description = "Single question answered from policy documents, with citations")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @Operation(summary = "Ask a question about the uploaded policies",
            description = "Retrieves the most relevant chunks from pgvector and answers only from them, citing [S#] sources.")
    @PostMapping
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return ragService.ask(request);
    }
}
