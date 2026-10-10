package com.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record DonationRequest(
        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1 kg")
        Integer quantityKg,

        @NotNull(message = "Pickup from is required")
        LocalDateTime pickupFrom,

        @NotNull(message = "Pickup until is required")
        LocalDateTime pickupUntil
) {
}
