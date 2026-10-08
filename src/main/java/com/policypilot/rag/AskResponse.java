package com.policypilot.rag;

import java.util.List;

public record AskResponse(String answer, List<SourceRef> sources) {
}
