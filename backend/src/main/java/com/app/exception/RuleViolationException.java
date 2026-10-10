package com.app.exception;

import java.util.List;

public class RuleViolationException extends RuntimeException {

    private final List<String> violations;

    public RuleViolationException(List<String> violations) {
        super(String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public RuleViolationException(String violation) {
        this(List.of(violation));
    }

    public List<String> getViolations() {
        return violations;
    }
}
