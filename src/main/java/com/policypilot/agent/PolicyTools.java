package com.policypilot.agent;

import com.policypilot.audit.ToolAuditService;
import com.policypilot.domain.ClaimRepository;
import com.policypilot.domain.PolicyRepository;
import com.policypilot.domain.ServiceRequest;
import com.policypilot.domain.ServiceRequestRepository;
import com.policypilot.guard.PiiMasker;
import com.policypilot.rag.DocumentSearchService;
import com.policypilot.rag.SourceRef;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tools the LLM may decide to call. Spring AI sends the @Tool descriptions to the model;
 * when the model asks for a tool, Spring calls the method and returns the result to the model.
 * Every input is validated and every call is audited.
 */
@Component
public class PolicyTools {

    static final String CONVERSATION_ID = "conversationId";
    private static final Pattern CLAIM_ID = Pattern.compile("CLM-\\d{4}");
    private static final Pattern POLICY_NO = Pattern.compile("POL-\\d{4}");

    private final ClaimRepository claims;
    private final PolicyRepository policies;
    private final ServiceRequestRepository serviceRequests;
    private final DocumentSearchService search;
    private final ToolAuditService audit;

    public PolicyTools(ClaimRepository claims, PolicyRepository policies, ServiceRequestRepository serviceRequests,
                       DocumentSearchService search, ToolAuditService audit) {
        this.claims = claims;
        this.policies = policies;
        this.serviceRequests = serviceRequests;
        this.search = search;
        this.audit = audit;
    }

    public record ClaimStatus(String claimId, String policyNumber, String claimType, BigDecimal amount,
                              String status, LocalDate filedOn, String lastUpdate) {
    }

    public record PremiumInfo(String policyNumber, String holderName, String policyType, String policyStatus,
                              BigDecimal premiumAmount, LocalDate dueDate, long daysUntilDue) {
    }

    public record ServiceRequestCreated(Long requestId, String policyNumber, String status, String message) {
    }

    public record SearchResult(String excerptLabel, String fileName, Object page, String text) {
    }

    public record ToolError(String error) {
    }

    @Tool(description = "Get the current status of an insurance claim by claim id (format CLM-1234).")
    public Object getClaimStatus(
            @ToolParam(description = "Claim id, e.g. CLM-1023") String claimId,
            ToolContext context) {
        return audit.audited(conversationId(context), "getClaimStatus", "claimId=" + claimId, () -> {
            String id = normalize(claimId);
            if (!CLAIM_ID.matcher(id).matches()) {
                return new ToolError("Invalid claim id. Expected format CLM-1234.");
            }
            return claims.findById(id)
                    .<Object>map(c -> new ClaimStatus(c.getClaimId(), c.getPolicyNumber(), c.getClaimType(),
                            c.getAmount(), c.getStatus(), c.getFiledOn(), c.getLastUpdate()))
                    .orElse(new ToolError("No claim found with id " + id));
        });
    }

    @Tool(description = "Get the premium amount and next due date for a policy number (format POL-1234).")
    public Object getPremiumDue(
            @ToolParam(description = "Policy number, e.g. POL-1001") String policyNumber,
            ToolContext context) {
        return audit.audited(conversationId(context), "getPremiumDue", "policyNumber=" + policyNumber, () -> {
            String no = normalize(policyNumber);
            if (!POLICY_NO.matcher(no).matches()) {
                return new ToolError("Invalid policy number. Expected format POL-1234.");
            }
            return policies.findById(no)
                    .<Object>map(p -> new PremiumInfo(p.getPolicyNumber(), p.getHolderName(), p.getPolicyType(),
                            p.getStatus(), p.getPremiumAmount(), p.getPremiumDueDate(),
                            ChronoUnit.DAYS.between(LocalDate.now(), p.getPremiumDueDate())))
                    .orElse(new ToolError("No policy found with number " + no));
        });
    }

    @Tool(description = "Raise a customer service request for a policy. Only use when the user explicitly asks to raise or log a request/complaint.")
    public Object raiseServiceRequest(
            @ToolParam(description = "Policy number, e.g. POL-1001") String policyNumber,
            @ToolParam(description = "Short description of the customer's issue") String issue,
            ToolContext context) {
        return audit.audited(conversationId(context), "raiseServiceRequest",
                "policyNumber=" + policyNumber + ", issue=" + issue, () -> {
                    String no = normalize(policyNumber);
                    if (!POLICY_NO.matcher(no).matches()) {
                        return new ToolError("Invalid policy number. Expected format POL-1234.");
                    }
                    if (!policies.existsById(no)) {
                        return new ToolError("No policy found with number " + no);
                    }
                    if (issue == null || issue.isBlank() || issue.length() > 500) {
                        return new ToolError("Issue description must be 1-500 characters.");
                    }
                    ServiceRequest saved = serviceRequests.save(new ServiceRequest(no, PiiMasker.mask(issue.strip())));
                    return new ServiceRequestCreated(saved.getId(), no, saved.getStatus(),
                            "Service request created. Our team will contact the policy holder within 2 working days.");
                });
    }

    @Tool(description = "Search the uploaded insurance policy documents for coverage, exclusions, waiting periods, "
            + "limits or claim procedures. Returns excerpts with file name and page for citation.")
    public Object searchPolicyDocuments(
            @ToolParam(description = "What to look for, e.g. 'is flood damage covered'") String query,
            ToolContext context) {
        return audit.audited(conversationId(context), "searchPolicyDocuments", "query=" + query, () -> {
            if (query == null || query.isBlank()) {
                return new ToolError("Query must not be empty.");
            }
            List<SearchResult> results = search.search(query, null).stream()
                    .map(h -> {
                        SourceRef s = h.source();
                        return new SearchResult(s.label(), s.fileName(), s.page(), h.text());
                    })
                    .toList();
            if (results.isEmpty()) {
                return new ToolError("No relevant text found in the uploaded policy documents.");
            }
            return results;
        });
    }

    private static String normalize(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }

    private static String conversationId(ToolContext context) {
        if (context == null || context.getContext() == null) {
            return "unknown";
        }
        return String.valueOf(context.getContext().getOrDefault(CONVERSATION_ID, "unknown"));
    }
}
