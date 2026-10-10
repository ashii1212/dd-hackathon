package com.app.dto;

import com.app.model.Claim;
import com.app.model.ClaimStatus;

import java.time.LocalDateTime;

public record ClaimResponse(
        Long id,
        String donationId,
        String donationDescription,
        String donorName,
        String volunteerId,
        String volunteerName,
        int kg,
        LocalDateTime plannedTime,
        LocalDateTime pickupFrom,
        LocalDateTime pickupUntil,
        ClaimStatus status
) {
    public static ClaimResponse from(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getDonation().getId(),
                claim.getDonation().getDescription(),
                claim.getDonation().getDonor().getName(),
                claim.getVolunteer().getId(),
                claim.getVolunteer().getName(),
                claim.getKg(),
                claim.getPlannedTime(),
                claim.getDonation().getPickupFrom(),
                claim.getDonation().getPickupUntil(),
                claim.getStatus());
    }
}
