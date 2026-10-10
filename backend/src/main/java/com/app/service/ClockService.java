package com.app.service;

import com.app.exception.RuleViolationException;
import com.app.model.AppClock;
import com.app.model.Claim;
import com.app.model.ClaimStatus;
import com.app.repository.AppClockRepository;
import com.app.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClockService {

    private final AppClockRepository clockRepository;
    private final ClaimRepository claimRepository;

    public ClockService(AppClockRepository clockRepository, ClaimRepository claimRepository) {
        this.clockRepository = clockRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public LocalDateTime now() {
        return clockRepository.findById(AppClock.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Clock is not initialised"))
                .getTime();
    }

    @Transactional
    public LocalDateTime lockAndGetNow() {
        return lockClock().getTime();
    }

    @Transactional
    public LocalDateTime moveTo(LocalDateTime newTime) {
        AppClock clock = lockClock();

        if (newTime.isBefore(clock.getTime())) {
            throw new RuleViolationException("The clock cannot move backwards");
        }
        clock.setTime(newTime);

        List<Claim> missed = claimRepository.findByStatusAndDonationPickupUntilBefore(ClaimStatus.ACTIVE, newTime);
        missed.forEach(claim -> claim.setStatus(ClaimStatus.MISSED));

        return clock.getTime();
    }

    private AppClock lockClock() {
        return clockRepository.findByIdForUpdate(AppClock.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Clock is not initialised"));
    }
}
