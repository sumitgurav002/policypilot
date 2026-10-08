package com.policypilot.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "1. Documents", description = "Upload policy PDFs and track ingestion status")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @Operation(summary = "Upload a policy PDF",
            description = "Stores the file and queues it on Kafka. Returns 202 immediately; poll GET /api/documents/{id} until status is INDEXED.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(DocumentResponse.from(service.upload(file)));
    }

    @Operation(summary = "Get a document's ingestion status")
    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable UUID id) {
        return DocumentResponse.from(service.get(id));
    }

    @Operation(summary = "List all uploaded documents (newest first)")
    @GetMapping
    public List<DocumentResponse> list() {
        return service.list().stream().map(DocumentResponse::from).toList();
    }
}
