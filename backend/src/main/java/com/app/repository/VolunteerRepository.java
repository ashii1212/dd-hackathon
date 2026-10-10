package com.app.repository;

import com.app.model.Volunteer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VolunteerRepository extends JpaRepository<Volunteer, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Volunteer v WHERE v.id = :id")
    Optional<Volunteer> findByIdForUpdate(@Param("id") String id);
}
