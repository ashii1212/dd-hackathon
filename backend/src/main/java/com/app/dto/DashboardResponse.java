package com.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
        LocalDateTime now,
        Totals totals,
        List<DonorStats> donors,
        List<VolunteerStats> volunteers
) {
    public record Totals(
            int listedKg,
            int collectedKg,
            int missedKg,
            int wastedKg,
            int inProgressKg,
            int stillOpenKg,
            boolean balanced
    ) {
    }

    public record DonorStats(String id, String name, int collectedKg) {
    }

    public record VolunteerStats(String id, String name, int collectedKg, int missedClaims) {
    }
}
