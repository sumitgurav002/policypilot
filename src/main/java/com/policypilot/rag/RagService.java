package com.policypilot.rag;

import com.policypilot.guard.InputGuard;
import com.policypilot.observability.AiMetrics;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Classic RAG: retrieve the top-k chunks, put them in the prompt, and ask the model to answer
 * only from them with [S#] citations.
 */
@Service
public class RagService {

    static final String NOT_FOUND = "I couldn't find this in the uploaded policy documents.";

    private final ChatClient chatClient;
    private final DocumentSearchService search;
    private final InputGuard guard;
    private final AiMetrics metrics;

    public RagService(@Qualifier("ragChatClient") ChatClient chatClient,
                      DocumentSearchService search, InputGuard guard, AiMetrics metrics) {
        this.chatClient = chatClient;
        this.search = search;
        this.guard = guard;
        this.metrics = metrics;
    }

    public AskResponse ask(AskRequest request) {
        guard.check(request.question());

        List<DocumentSearchService.Hit> hits = search.search(request.question(), request.documentId());
        if (hits.isEmpty()) {
            // Nothing relevant retrieved: answer without calling the LLM (saves cost, avoids hallucination).
            return new AskResponse(NOT_FOUND, List.of());
        }

        String userPrompt = """
                Context excerpts:
                %s
                Question: %s
                """.formatted(DocumentSearchService.toContext(hits), request.question());

        long start = System.nanoTime();
        ChatResponse response = chatClient.prompt().user(userPrompt).call().chatResponse();
        metrics.record("ask", response, start);

        String answer = response == null || response.getResult() == null
                ? NOT_FOUND
                : response.getResult().getOutput().getText();
        return new AskResponse(answer, hits.stream().map(DocumentSearchService.Hit::source).toList());
    }
}
