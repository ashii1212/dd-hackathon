package com.app.controller;

import com.app.dto.DonationRequest;
import com.app.dto.DonationResponse;
import com.app.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping("/api/donors/{donorId}/donations")
    public List<DonationResponse> getForDonor(@PathVariable String donorId) {
        return donationService.getForDonor(donorId);
    }

    @PostMapping("/api/donors/{donorId}/donations")
    public ResponseEntity<DonationResponse> post(@PathVariable String donorId,
                                                 @Valid @RequestBody DonationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(donationService.post(donorId, request));
    }

    @PostMapping("/api/donors/{donorId}/donations/{donationId}/cancel")
    public DonationResponse cancel(@PathVariable String donorId, @PathVariable String donationId) {
        return donationService.cancel(donorId, donationId);
    }

    @GetMapping("/api/volunteers/{volunteerId}/donations")
    public List<DonationResponse> getOpenForVolunteer(@PathVariable String volunteerId) {
        return donationService.getOpenForVolunteer(volunteerId);
    }
}
