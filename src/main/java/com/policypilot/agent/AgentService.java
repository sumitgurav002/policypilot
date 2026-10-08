package com.policypilot.agent;

import com.policypilot.guard.InputGuard;
import com.policypilot.observability.AiMetrics;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * The agent: the model reads the conversation and decides whether to answer directly,
 * search the policy documents, or call backend tools (claims, premiums, service requests).
 */
@Service
public class AgentService {

    private final ChatClient chatClient;
    private final PolicyTools tools;
    private final InputGuard guard;
    private final AiMetrics metrics;

    public AgentService(@Qualifier("agentChatClient") ChatClient chatClient, PolicyTools tools,
                        InputGuard guard, AiMetrics metrics) {
        this.chatClient = chatClient;
        this.tools = tools;
        this.guard = guard;
        this.metrics = metrics;
    }

    public ChatResponse chat(ChatRequest request) {
        guard.check(request.message());
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.conversationId().trim();

        long start = System.nanoTime();
        org.springframework.ai.chat.model.ChatResponse response = chatClient.prompt()
                .user(request.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(tools)
                .toolContext(Map.of(PolicyTools.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();
        metrics.record("chat", response, start);

        String reply = response == null || response.getResult() == null
                ? "Sorry, I couldn't generate a reply."
                : response.getResult().getOutput().getText();
        return new ChatResponse(conversationId, reply);
    }
}
