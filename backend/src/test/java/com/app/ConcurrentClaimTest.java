package com.app;

import com.app.dto.ClaimRequest;
import com.app.exception.RuleViolationException;
import com.app.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:concurrentdb;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConcurrentClaimTest {

    @Autowired
    private ClaimService claimService;

    @Test
    void onlyOneOfTwoSimultaneousClaimsForTheLastKgSucceeds() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> rhea = attempt(start, "V1", LocalDateTime.of(2026, 3, 10, 17, 0));
        Callable<Boolean> divya = attempt(start, "V5", LocalDateTime.of(2026, 3, 10, 17, 30));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Boolean>> results = List.of(pool.submit(rhea), pool.submit(divya));
        start.countDown();

        long succeeded = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                succeeded++;
            }
        }
        pool.shutdown();

        assertThat(succeeded).isEqualTo(1);
    }

    private Callable<Boolean> attempt(CountDownLatch start, String volunteerId, LocalDateTime plannedTime) {
        return () -> {
            start.await();
            try {
                claimService.claim("DN4", new ClaimRequest(volunteerId, 8, plannedTime));
                return true;
            } catch (RuleViolationException e) {
                return false;
            }
        };
    }
}
