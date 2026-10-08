package com.policypilot.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom LLM metrics, visible at /actuator/metrics/policypilot.llm.latency
 * and /actuator/metrics/policypilot.llm.tokens (and /actuator/prometheus).
 */
@Component
public class AiMetrics {

    private final MeterRegistry registry;

    public AiMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void record(String endpoint, ChatResponse response, long startNanos) {
        Timer.builder("policypilot.llm.latency")
                .tag("endpoint", endpoint)
                .register(registry)
                .record(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);

        if (response == null || response.getMetadata() == null) {
            return;
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage == null) {
            return;
        }
        var prompt = usage.getPromptTokens();
        var total = usage.getTotalTokens();
        if (prompt != null) {
            registry.counter("policypilot.llm.tokens", "endpoint", endpoint, "type", "prompt").increment(prompt.doubleValue());
        }
        if (total != null) {
            registry.counter("policypilot.llm.tokens", "endpoint", endpoint, "type", "total").increment(total.doubleValue());
        }
    }
}
