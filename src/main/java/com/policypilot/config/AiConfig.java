package com.policypilot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /** Keeps the last 20 messages per conversation (in memory). */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    /** Stateless client for single-shot RAG answers (/api/ask). */
    @Bean
    ChatClient ragChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(Prompts.RAG_SYSTEM).build();
    }

    /** Agent client with conversation memory (/api/chat). Tools are attached per request. */
    @Bean
    ChatClient agentChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(Prompts.AGENT_SYSTEM)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
