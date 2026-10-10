package com.app.seed;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record SeedData(
        LocalDateTime clock,
        List<SeedDonor> donors,
        List<SeedVolunteer> volunteers,
        List<SeedDonation> donations
) {
    public record SeedDonor(String id, String name, String zone) {
    }

    public record SeedVolunteer(
            String id,
            String name,
            @JsonProperty("capacity_kg") int capacityKg,
            List<String> zones
    ) {
    }

    public record SeedDonation(
            String id,
            @JsonProperty("donor_id") String donorId,
            String description,
            @JsonProperty("quantity_kg") int quantityKg,
            @JsonProperty("pickup_from") LocalDateTime pickupFrom,
            @JsonProperty("pickup_until") LocalDateTime pickupUntil
    ) {
    }
}
