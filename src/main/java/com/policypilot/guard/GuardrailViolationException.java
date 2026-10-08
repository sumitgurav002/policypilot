package com.policypilot.guard;

public class GuardrailViolationException extends RuntimeException {
    public GuardrailViolationException(String message) {
        super(message);
    }
}
