package com.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ClaimRequest(
        @NotBlank(message = "Volunteer is required")
        String volunteerId,

        @NotNull(message = "Kg is required")
        @Min(value = 1, message = "Kg must be at least 1")
        Integer kg,

        @NotNull(message = "Planned pickup time is required")
        LocalDateTime plannedTime
) {
}
