package com.policypilot.agent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "3. Agent chat", description = "Conversational agent that decides when to search documents or call backend tools")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @Operation(summary = "Chat with the PolicyPilot agent",
            description = "Try: 'What is the status of claim CLM-1023?', 'When is the premium due for POL-1002?', "
                    + "'Is flood damage covered?', 'Raise a request for POL-1001: please update my address'. "
                    + "Send the returned conversationId back to continue the same conversation.")
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return agentService.chat(request);
    }
}
