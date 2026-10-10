package com.app.controller;

import com.app.dto.ClaimRequest;
import com.app.dto.ClaimResponse;
import com.app.service.ClaimService;
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
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/api/donations/{donationId}/claims")
    public ResponseEntity<ClaimResponse> claim(@PathVariable String donationId,
                                               @Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.claim(donationId, request));
    }

    @GetMapping("/api/volunteers/{volunteerId}/claims")
    public List<ClaimResponse> getForVolunteer(@PathVariable String volunteerId) {
        return claimService.getForVolunteer(volunteerId);
    }

    @PostMapping("/api/volunteers/{volunteerId}/claims/{claimId}/collect")
    public ClaimResponse collect(@PathVariable String volunteerId, @PathVariable Long claimId) {
        return claimService.collect(volunteerId, claimId);
    }

    @PostMapping("/api/volunteers/{volunteerId}/claims/{claimId}/cancel")
    public ClaimResponse cancel(@PathVariable String volunteerId, @PathVariable Long claimId) {
        return claimService.cancel(volunteerId, claimId);
    }
}
