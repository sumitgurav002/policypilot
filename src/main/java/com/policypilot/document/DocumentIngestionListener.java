package com.policypilot.document;

import com.policypilot.config.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes "document-uploaded" events: PDF -> pages -> chunks -> embeddings -> pgvector.
 * Failures are retried with exponential backoff; after the last attempt the event goes to the
 * dead-letter topic and the document is marked FAILED.
 */
@Component
public class DocumentIngestionListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionListener.class);

    private final PolicyDocumentRepository repository;
    private final VectorStore vectorStore;

    // chunkSize (tokens), minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator
    private final TokenTextSplitter splitter = new TokenTextSplitter(600, 200, 10, 10000, true);

    public DocumentIngestionListener(PolicyDocumentRepository repository, VectorStore vectorStore) {
        this.repository = repository;
        this.vectorStore = vectorStore;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            exclude = UnreadableDocumentException.class)
    @KafkaListener(topics = Topics.DOCUMENT_UPLOADED, groupId = "policypilot-ingestion")
    public void onDocumentUploaded(String documentId,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        UUID id = UUID.fromString(documentId);
        PolicyDocument doc = repository.findById(id).orElse(null);
        if (doc == null) {
            log.warn("Document {} no longer exists, skipping", id);
            return;
        }
        log.info("Ingesting document {} from topic {}", id, topic);
        doc.markProcessing();
        repository.save(doc);

        List<Document> pages = new PagePdfDocumentReader(
                new FileSystemResource(doc.getStoragePath()),
                PdfDocumentReaderConfig.builder().withPagesPerDocument(1).build()).get();

        List<Document> textPages = pages.stream()
                .filter(p -> p.getText() != null && !p.getText().isBlank())
                .toList();
        if (textPages.isEmpty()) {
            throw new UnreadableDocumentException("No extractable text found (is it a scanned image PDF?)");
        }

        List<Document> chunks = splitter.apply(textPages).stream()
                .map(chunk -> withMetadata(chunk, doc))
                .toList();

        // Idempotent re-processing: remove any vectors from a previous attempt first.
        vectorStore.delete("docId == '" + id + "'");
        vectorStore.add(chunks);

        doc.markIndexed(chunks.size());
        repository.save(doc);
        log.info("Document {} indexed with {} chunks", id, chunks.size());
    }

    @DltHandler
    public void onDeadLetter(String documentId) {
        log.error("Document {} moved to dead-letter topic", documentId);
        repository.findById(UUID.fromString(documentId)).ifPresent(doc -> {
            doc.markFailed("Ingestion failed after retries. Check the PDF has selectable text and the embedding model is running.");
            repository.save(doc);
        });
    }

    private Document withMetadata(Document chunk, PolicyDocument doc) {
        Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
        metadata.put("docId", doc.getId().toString());
        metadata.put("fileName", doc.getFileName());
        // PagePdfDocumentReader stores the page as "page_number"; keep it for citations.
        return new Document(chunk.getText(), metadata);
    }
}
