package com.policypilot.document;

/** Thrown when a PDF has no extractable text (e.g. a scanned image). Not worth retrying. */
public class UnreadableDocumentException extends RuntimeException {
    public UnreadableDocumentException(String message) {
        super(message);
    }
}
