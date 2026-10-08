package com.policypilot.rag;

/** A citation: which document and page a piece of the answer came from. */
public record SourceRef(String label, String documentId, String fileName, Object page, Double score, String excerpt) {
}
