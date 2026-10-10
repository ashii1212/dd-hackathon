package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @Column(nullable = false)
    private String description;

    @Column(name = "quantity_kg", nullable = false)
    private int quantityKg;

    @Column(name = "pickup_from", nullable = false)
    private LocalDateTime pickupFrom;

    @Column(name = "pickup_until", nullable = false)
    private LocalDateTime pickupUntil;

    @Column(nullable = false)
    private boolean cancelled;

    @OneToMany(mappedBy = "donation")
    @OrderBy("id")
    private List<Claim> claims = new ArrayList<>();

    protected Donation() {
    }

    public Donation(String id, Donor donor, String description, int quantityKg,
                    LocalDateTime pickupFrom, LocalDateTime pickupUntil) {
        this.id = id;
        this.donor = donor;
        this.description = description;
        this.quantityKg = quantityKg;
        this.pickupFrom = pickupFrom;
        this.pickupUntil = pickupUntil;
    }

    public int getRemainingKg() {
        int claimedKg = claims.stream()
                .filter(claim -> claim.getStatus() != ClaimStatus.CANCELLED)
                .mapToInt(Claim::getKg)
                .sum();
        return quantityKg - claimedKg;
    }

    public DonationStatus statusAt(LocalDateTime now) {
        if (cancelled) {
            return DonationStatus.CANCELLED;
        }
        if (now.isAfter(pickupUntil)) {
            return DonationStatus.EXPIRED;
        }
        if (getRemainingKg() == 0) {
            return DonationStatus.FULLY_CLAIMED;
        }
        return DonationStatus.OPEN;
    }

    public boolean isPickupWindowOpenAt(LocalDateTime time) {
        return !time.isBefore(pickupFrom) && !time.isAfter(pickupUntil);
    }

    public String getZone() { return donor.getZone(); }
    public String getId() { return id; }
    public Donor getDonor() { return donor; }
    public String getDescription() { return description; }
    public int getQuantityKg() { return quantityKg; }
    public LocalDateTime getPickupFrom() { return pickupFrom; }
    public LocalDateTime getPickupUntil() { return pickupUntil; }
    public boolean isCancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }
    public List<Claim> getClaims() { return claims; }
}
