package com.app.dto;

import com.app.model.Donation;
import com.app.model.DonationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DonationResponse(
        String id,
        String donorId,
        String donorName,
        String zone,
        String description,
        int quantityKg,
        int remainingKg,
        LocalDateTime pickupFrom,
        LocalDateTime pickupUntil,
        DonationStatus status,
        List<ClaimResponse> claims
) {
    public static DonationResponse from(Donation donation, LocalDateTime now) {
        return new DonationResponse(
                donation.getId(),
                donation.getDonor().getId(),
                donation.getDonor().getName(),
                donation.getZone(),
                donation.getDescription(),
                donation.getQuantityKg(),
                donation.getRemainingKg(),
                donation.getPickupFrom(),
                donation.getPickupUntil(),
                donation.statusAt(now),
                donation.getClaims().stream().map(ClaimResponse::from).toList());
    }
}
