package com.policypilot.guard;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * First line of defence before user text reaches the model:
 * length limit and a few well-known prompt-injection phrases.
 * The system prompt is the second line of defence.
 */
@Component
public class InputGuard {

    static final int MAX_LENGTH = 2000;

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+(all\\s+)?(the\\s+)?(previous|prior|above)\\s+instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(reveal|show|print)\\s+(your\\s+)?(system\\s+prompt|instructions)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now\\s+(a|an|in)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("developer\\s+mode|jailbreak", Pattern.CASE_INSENSITIVE));

    public void check(String userText) {
        if (userText == null || userText.isBlank()) {
            throw new GuardrailViolationException("Message must not be empty");
        }
        if (userText.length() > MAX_LENGTH) {
            throw new GuardrailViolationException("Message is too long (max " + MAX_LENGTH + " characters)");
        }
        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(userText).find()) {
                throw new GuardrailViolationException(
                        "Your message looks like an attempt to change the assistant's instructions, so it was blocked.");
            }
        }
    }
}
