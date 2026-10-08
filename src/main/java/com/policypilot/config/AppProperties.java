package com.policypilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Application settings under the "policypilot" prefix in application.yml. */
@ConfigurationProperties(prefix = "policypilot")
public record AppProperties(String uploadDir, Rag rag) {

    public record Rag(int topK, double similarityThreshold) {
    }
}
