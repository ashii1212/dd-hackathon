package com.app.repository;

import com.app.model.Donation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Donation d WHERE d.id = :id")
    Optional<Donation> findByIdForUpdate(@Param("id") String id);

    List<Donation> findByDonorIdOrderByPickupFromAsc(String donorId);

    List<Donation> findByDonorZoneInOrderByPickupFromAsc(Collection<String> zones);

    List<Donation> findAllByOrderByIdAsc();
}
