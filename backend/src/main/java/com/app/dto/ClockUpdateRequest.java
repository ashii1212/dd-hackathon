package com.app.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ClockUpdateRequest(
        @NotNull(message = "Time is required")
        LocalDateTime now
) {
}
