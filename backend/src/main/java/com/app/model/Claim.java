package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id")
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @Column(nullable = false)
    private int kg;

    @Column(name = "planned_time", nullable = false)
    private LocalDateTime plannedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    protected Claim() {
    }

    public Claim(Donation donation, Volunteer volunteer, int kg, LocalDateTime plannedTime) {
        this.donation = donation;
        this.volunteer = volunteer;
        this.kg = kg;
        this.plannedTime = plannedTime;
        this.status = ClaimStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public Donation getDonation() { return donation; }
    public Volunteer getVolunteer() { return volunteer; }
    public int getKg() { return kg; }
    public LocalDateTime getPlannedTime() { return plannedTime; }
    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }
}
