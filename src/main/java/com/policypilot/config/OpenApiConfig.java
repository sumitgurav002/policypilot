package com.policypilot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "PolicyPilot API",
        version = "0.1.0",
        description = "Agentic AI insurance assistant: upload policy PDFs, ask cited questions (RAG), "
                + "and chat with an agent that calls backend tools."))
public class OpenApiConfig {
}
