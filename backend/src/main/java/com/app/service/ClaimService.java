package com.app.service;

import com.app.config.JacksonConfig;
import com.app.dto.ClaimRequest;
import com.app.dto.ClaimResponse;
import com.app.exception.ResourceNotFoundException;
import com.app.exception.RuleViolationException;
import com.app.model.Claim;
import com.app.model.ClaimStatus;
import com.app.model.Donation;
import com.app.model.DonationStatus;
import com.app.model.Volunteer;
import com.app.repository.ClaimRepository;
import com.app.repository.DonationRepository;
import com.app.repository.VolunteerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimService {

    private static final long MIN_MINUTES_BETWEEN_PICKUPS = 60;

    private final ClaimRepository claimRepository;
    private final DonationRepository donationRepository;
    private final VolunteerRepository volunteerRepository;
    private final ClockService clockService;

    public ClaimService(ClaimRepository claimRepository, DonationRepository donationRepository,
                        VolunteerRepository volunteerRepository, ClockService clockService) {
        this.claimRepository = claimRepository;
        this.donationRepository = donationRepository;
        this.volunteerRepository = volunteerRepository;
        this.clockService = clockService;
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getForVolunteer(String volunteerId) {
        findVolunteer(volunteerId);
        return claimRepository.findByVolunteerIdOrderByPlannedTimeAsc(volunteerId).stream()
                .map(ClaimResponse::from)
                .toList();
    }

    @Transactional
    public ClaimResponse claim(String donationId, ClaimRequest request) {
        LocalDateTime now = clockService.lockAndGetNow();
        Donation donation = donationRepository.findByIdForUpdate(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));
        Volunteer volunteer = volunteerRepository.findByIdForUpdate(request.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found: " + request.volunteerId()));

        int kg = request.kg();
        LocalDateTime plannedTime = request.plannedTime();
        List<String> violations = new ArrayList<>();

        DonationStatus status = donation.statusAt(now);
        if (status == DonationStatus.CANCELLED || status == DonationStatus.EXPIRED) {
            violations.add("Rule 1: the donation is " + status);
        }
        if (kg > volunteer.getCapacityKg()) {
            violations.add("Rule 2: " + kg + " kg is more than your capacity of " + volunteer.getCapacityKg() + " kg");
        }
        if (kg > donation.getRemainingKg()) {
            violations.add("Rule 3: " + kg + " kg is more than the " + donation.getRemainingKg() + " kg remaining");
        }
        if (!volunteer.getZones().contains(donation.getZone())) {
            violations.add("Rule 4: the donation is in zone " + donation.getZone() + ", which is not one of your zones");
        }
        if (!donation.isPickupWindowOpenAt(plannedTime)) {
            violations.add("Rule 5: planned time must be between " + format(donation.getPickupFrom())
                    + " and " + format(donation.getPickupUntil()));
        }
        if (plannedTime.isBefore(now)) {
            violations.add("Rule 6: planned time cannot be before the current time " + format(now));
        }
        for (Claim other : claimRepository.findByVolunteerIdAndStatusIn(
                volunteer.getId(), List.of(ClaimStatus.ACTIVE, ClaimStatus.COLLECTED))) {
            long gap = Math.abs(Duration.between(other.getPlannedTime(), plannedTime).toMinutes());
            if (gap < MIN_MINUTES_BETWEEN_PICKUPS) {
                violations.add("Rule 7: planned time is only " + gap + " minutes from your claim #" + other.getId()
                        + " at " + format(other.getPlannedTime()) + " (at least 60 needed)");
            }
        }
        if (!violations.isEmpty()) {
            throw new RuleViolationException(violations);
        }

        Claim claim = claimRepository.save(new Claim(donation, volunteer, kg, plannedTime));
        donation.getClaims().add(claim);
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse collect(String volunteerId, Long claimId) {
        LocalDateTime now = clockService.lockAndGetNow();
        Claim claim = findOwnClaim(volunteerId, claimId);
        Donation donation = claim.getDonation();

        List<String> violations = new ArrayList<>();
        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            violations.add("Only an ACTIVE claim can be collected (current status: " + claim.getStatus() + ")");
        }
        if (!donation.isPickupWindowOpenAt(now)) {
            violations.add("Collection is only possible between " + format(donation.getPickupFrom())
                    + " and " + format(donation.getPickupUntil()) + "; it is now " + format(now));
        }
        if (!violations.isEmpty()) {
            throw new RuleViolationException(violations);
        }

        claim.setStatus(ClaimStatus.COLLECTED);
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse cancel(String volunteerId, Long claimId) {
        LocalDateTime now = clockService.lockAndGetNow();
        Claim claim = findOwnClaim(volunteerId, claimId);

        List<String> violations = new ArrayList<>();
        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            violations.add("Only an ACTIVE claim can be cancelled (current status: " + claim.getStatus() + ")");
        }
        if (!now.isBefore(claim.getPlannedTime())) {
            violations.add("A claim can only be cancelled before its planned time " + format(claim.getPlannedTime()));
        }
        if (!violations.isEmpty()) {
            throw new RuleViolationException(violations);
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        return ClaimResponse.from(claim);
    }

    private Claim findOwnClaim(String volunteerId, Long claimId) {
        return claimRepository.findById(claimId)
                .filter(claim -> claim.getVolunteer().getId().equals(volunteerId))
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));
    }

    private Volunteer findVolunteer(String volunteerId) {
        return volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found: " + volunteerId));
    }

    private static String format(LocalDateTime time) {
        return time.format(JacksonConfig.DATE_TIME_FORMAT);
    }
}
