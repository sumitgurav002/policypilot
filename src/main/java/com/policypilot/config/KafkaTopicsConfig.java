package com.policypilot.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    NewTopic documentUploadedTopic() {
        return TopicBuilder.name(Topics.DOCUMENT_UPLOADED).partitions(3).replicas(1).build();
    }
    // Retry and dead-letter topics are created automatically by @RetryableTopic.
}
