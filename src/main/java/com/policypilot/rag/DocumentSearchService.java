package com.policypilot.rag;

import com.policypilot.config.AppProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Semantic search over indexed policy chunks. Shared by /api/ask and the agent's search tool. */
@Service
public class DocumentSearchService {

    private static final int EXCERPT_CHARS = 240;

    private final VectorStore vectorStore;
    private final AppProperties.Rag rag;

    public DocumentSearchService(VectorStore vectorStore, AppProperties props) {
        this.vectorStore = vectorStore;
        this.rag = props.rag();
    }

    public record Hit(SourceRef source, String text) {
    }

    /**
     * @param documentId optional: restrict the search to one uploaded document
     */
    public List<Hit> search(String query, String documentId) {
        SearchRequest.Builder request = SearchRequest.builder()
                .query(query)
                .topK(rag.topK())
                .similarityThreshold(rag.similarityThreshold());
        if (documentId != null && !documentId.isBlank()) {
            request.filterExpression("docId == '" + documentId.trim() + "'");
        }

        List<Document> results = vectorStore.similaritySearch(request.build());
        List<Hit> hits = new ArrayList<>();
        if (results == null) {
            return hits;
        }
        for (int i = 0; i < results.size(); i++) {
            Document d = results.get(i);
            String text = d.getText() == null ? "" : d.getText();
            SourceRef ref = new SourceRef(
                    "S" + (i + 1),
                    String.valueOf(d.getMetadata().get("docId")),
                    String.valueOf(d.getMetadata().get("fileName")),
                    d.getMetadata().get("page_number"),
                    d.getScore(),
                    text.length() > EXCERPT_CHARS ? text.substring(0, EXCERPT_CHARS) + "..." : text);
            hits.add(new Hit(ref, text));
        }
        return hits;
    }

    /** Formats hits as numbered context blocks for the prompt. */
    public static String toContext(List<Hit> hits) {
        StringBuilder sb = new StringBuilder();
        for (Hit h : hits) {
            sb.append("[").append(h.source().label()).append("] (file: ").append(h.source().fileName())
                    .append(", page: ").append(h.source().page()).append(")\n")
                    .append(h.text().strip()).append("\n\n");
        }
        return sb.toString();
    }
}
