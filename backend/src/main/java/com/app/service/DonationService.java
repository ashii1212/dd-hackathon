package com.app.service;

import com.app.dto.DonationRequest;
import com.app.dto.DonationResponse;
import com.app.exception.ResourceNotFoundException;
import com.app.exception.RuleViolationException;
import com.app.model.ClaimStatus;
import com.app.model.Donation;
import com.app.model.DonationStatus;
import com.app.model.Donor;
import com.app.model.Volunteer;
import com.app.repository.DonationRepository;
import com.app.repository.DonorRepository;
import com.app.repository.VolunteerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonorRepository donorRepository;
    private final VolunteerRepository volunteerRepository;
    private final ClockService clockService;

    public DonationService(DonationRepository donationRepository, DonorRepository donorRepository,
                           VolunteerRepository volunteerRepository, ClockService clockService) {
        this.donationRepository = donationRepository;
        this.donorRepository = donorRepository;
        this.volunteerRepository = volunteerRepository;
        this.clockService = clockService;
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> getForDonor(String donorId) {
        findDonor(donorId);
        LocalDateTime now = clockService.now();
        return donationRepository.findByDonorIdOrderByPickupFromAsc(donorId).stream()
                .map(donation -> DonationResponse.from(donation, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> getOpenForVolunteer(String volunteerId) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found: " + volunteerId));
        LocalDateTime now = clockService.now();
        return donationRepository.findByDonorZoneInOrderByPickupFromAsc(volunteer.getZones()).stream()
                .filter(donation -> donation.statusAt(now) == DonationStatus.OPEN)
                .map(donation -> DonationResponse.from(donation, now))
                .toList();
    }

    @Transactional
    public DonationResponse post(String donorId, DonationRequest request) {
        Donor donor = findDonor(donorId);
        LocalDateTime now = clockService.now();

        List<String> violations = new ArrayList<>();
        if (!request.pickupFrom().isBefore(request.pickupUntil())) {
            violations.add("Pickup from must be before pickup until");
        }
        if (request.pickupUntil().isBefore(now)) {
            violations.add("Pickup until cannot be before the current time");
        }
        if (!violations.isEmpty()) {
            throw new RuleViolationException(violations);
        }

        Donation donation = new Donation(nextId(), donor, request.description().trim(), request.quantityKg(),
                request.pickupFrom(), request.pickupUntil());
        return DonationResponse.from(donationRepository.save(donation), now);
    }

    @Transactional
    public DonationResponse cancel(String donorId, String donationId) {
        Donation donation = donationRepository.findByIdForUpdate(donationId)
                .filter(d -> d.getDonor().getId().equals(donorId))
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));
        LocalDateTime now = clockService.now();

        List<String> violations = new ArrayList<>();
        DonationStatus status = donation.statusAt(now);
        if (status != DonationStatus.OPEN) {
            violations.add("Only an OPEN donation can be cancelled (current status: " + status + ")");
        }
        boolean hasLiveClaims = donation.getClaims().stream()
                .anyMatch(claim -> claim.getStatus() == ClaimStatus.ACTIVE || claim.getStatus() == ClaimStatus.COLLECTED);
        if (hasLiveClaims) {
            violations.add("A donation with ACTIVE or COLLECTED claims cannot be cancelled");
        }
        if (!violations.isEmpty()) {
            throw new RuleViolationException(violations);
        }

        donation.cancel();
        return DonationResponse.from(donation, now);
    }

    private Donor findDonor(String donorId) {
        return donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + donorId));
    }

    private String nextId() {
        long next = donationRepository.count() + 1;
        while (donationRepository.existsById("DN" + next)) {
            next++;
        }
        return "DN" + next;
    }
}
