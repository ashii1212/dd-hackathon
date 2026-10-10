package com.app.service;

import com.app.dto.DashboardResponse;
import com.app.dto.DashboardResponse.DonorStats;
import com.app.dto.DashboardResponse.Totals;
import com.app.dto.DashboardResponse.VolunteerStats;
import com.app.model.Claim;
import com.app.model.ClaimStatus;
import com.app.model.Donation;
import com.app.model.DonationStatus;
import com.app.repository.DonationRepository;
import com.app.repository.DonorRepository;
import com.app.repository.VolunteerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DonationRepository donationRepository;
    private final DonorRepository donorRepository;
    private final VolunteerRepository volunteerRepository;
    private final ClockService clockService;

    public DashboardService(DonationRepository donationRepository, DonorRepository donorRepository,
                            VolunteerRepository volunteerRepository, ClockService clockService) {
        this.donationRepository = donationRepository;
        this.donorRepository = donorRepository;
        this.volunteerRepository = volunteerRepository;
        this.clockService = clockService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get() {
        LocalDateTime now = clockService.now();

        int listed = 0, collected = 0, missed = 0, wasted = 0, inProgress = 0, stillOpen = 0;
        Map<String, Integer> collectedByDonor = new HashMap<>();
        Map<String, Integer> collectedByVolunteer = new HashMap<>();
        Map<String, Integer> missedByVolunteer = new HashMap<>();

        for (Donation donation : donationRepository.findAllByOrderByIdAsc()) {
            DonationStatus status = donation.statusAt(now);
            if (status == DonationStatus.CANCELLED) {
                continue;
            }
            listed += donation.getQuantityKg();
            if (status == DonationStatus.EXPIRED) {
                wasted += donation.getRemainingKg();
            } else {
                stillOpen += donation.getRemainingKg();
            }

            for (Claim claim : donation.getClaims()) {
                String volunteerId = claim.getVolunteer().getId();
                switch (claim.getStatus()) {
                    case COLLECTED -> {
                        collected += claim.getKg();
                        collectedByDonor.merge(donation.getDonor().getId(), claim.getKg(), Integer::sum);
                        collectedByVolunteer.merge(volunteerId, claim.getKg(), Integer::sum);
                    }
                    case MISSED -> {
                        missed += claim.getKg();
                        missedByVolunteer.merge(volunteerId, 1, Integer::sum);
                    }
                    case ACTIVE -> inProgress += claim.getKg();
                    case CANCELLED -> {
                    }
                }
            }
        }

        boolean balanced = listed == collected + missed + wasted + inProgress + stillOpen;
        Totals totals = new Totals(listed, collected, missed, wasted, inProgress, stillOpen, balanced);

        List<DonorStats> donors = donorRepository.findAll(Sort.by("id")).stream()
                .map(d -> new DonorStats(d.getId(), d.getName(), collectedByDonor.getOrDefault(d.getId(), 0)))
                .toList();
        List<VolunteerStats> volunteers = volunteerRepository.findAll(Sort.by("id")).stream()
                .map(v -> new VolunteerStats(v.getId(), v.getName(),
                        collectedByVolunteer.getOrDefault(v.getId(), 0),
                        missedByVolunteer.getOrDefault(v.getId(), 0)))
                .toList();

        return new DashboardResponse(now, totals, donors, volunteers);
    }
}
