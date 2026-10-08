package com.policypilot.document;

import com.policypilot.common.BadRequestException;
import com.policypilot.common.NotFoundException;
import com.policypilot.config.AppProperties;
import com.policypilot.config.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/** Accepts uploads: stores the file, records it as PENDING and publishes an event for async ingestion. */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final PolicyDocumentRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Path uploadDir;

    public DocumentService(PolicyDocumentRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           AppProperties props) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.uploadDir = Path.of(props.uploadDir()).toAbsolutePath();
    }

    public PolicyDocument upload(MultipartFile file) {
        validate(file);
        UUID id = UUID.randomUUID();
        Path target = uploadDir.resolve(id + ".pdf");
        try {
            Files.createDirectories(uploadDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not store uploaded file", e);
        }

        PolicyDocument doc = repository.save(
                new PolicyDocument(id, cleanName(file.getOriginalFilename()), target.toString()));

        // Key = document id, so all events for one document go to the same partition (ordering).
        kafkaTemplate.send(Topics.DOCUMENT_UPLOADED, id.toString(), id.toString());
        log.info("Document {} ({}) stored and queued for ingestion", id, doc.getFileName());
        return doc;
    }

    public PolicyDocument get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Document not found: " + id));
    }

    public List<PolicyDocument> list() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload a non-empty PDF file");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are supported");
        }
    }

    private String cleanName(String original) {
        if (original == null || original.isBlank()) {
            return "document.pdf";
        }
        return Path.of(original).getFileName().toString();
    }
}
