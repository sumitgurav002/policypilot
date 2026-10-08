package com.policypilot.document;

public enum DocumentStatus {
    PENDING,     // uploaded, waiting in Kafka
    PROCESSING,  // consumer is chunking + embedding
    INDEXED,     // searchable
    FAILED       // ended in the dead-letter topic
}
