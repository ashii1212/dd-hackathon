package com.app.repository;

import com.app.model.AppClock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppClockRepository extends JpaRepository<AppClock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM AppClock c WHERE c.id = :id")
    Optional<AppClock> findByIdForUpdate(@Param("id") Long id);
}
