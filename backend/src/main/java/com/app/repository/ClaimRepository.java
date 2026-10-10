package com.app.repository;

import com.app.model.Claim;
import com.app.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByVolunteerIdOrderByPlannedTimeAsc(String volunteerId);

    List<Claim> findByVolunteerIdAndStatusIn(String volunteerId, Collection<ClaimStatus> statuses);

    List<Claim> findByStatusAndDonationPickupUntilBefore(ClaimStatus status, LocalDateTime time);
}
