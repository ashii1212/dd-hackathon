package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SurplusLinkApiTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions claim(String donationId, String volunteerId, Object kg, String plannedTime) throws Exception {
        String body = "{\"volunteerId\":\"" + volunteerId + "\",\"kg\":" + kg + ",\"plannedTime\":\"" + plannedTime + "\"}";
        return mockMvc.perform(post("/api/donations/" + donationId + "/claims")
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions moveClock(String time) throws Exception {
        return mockMvc.perform(put("/api/clock")
                .contentType(MediaType.APPLICATION_JSON).content("{\"now\":\"" + time + "\"}"));
    }

    @Test
    void seedIsLoaded() throws Exception {
        mockMvc.perform(get("/api/clock")).andExpect(jsonPath("$.now").value("2026-03-10T12:00"));
        mockMvc.perform(get("/api/people"))
                .andExpect(jsonPath("$.donors", hasSize(5)))
                .andExpect(jsonPath("$.volunteers", hasSize(6)));
    }

    @Test
    void validClaimReducesRemainingKg() throws Exception {
        claim("DN2", "V2", 6, "2026-03-10T18:00").andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/donors/D2/donations"))
                .andExpect(jsonPath("$[0].remainingKg").value(4));
    }

    @Test
    void reportsEveryViolatedRule() throws Exception {
        claim("DN1", "V5", 12, "2026-03-10T17:00")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0]", startsWith("Rule 2")))
                .andExpect(jsonPath("$.violations[1]", startsWith("Rule 4")))
                .andExpect(jsonPath("$.violations[2]", startsWith("Rule 5")));
    }

    @Test
    void rejectsNonWholeKgAsInvalidInput() throws Exception {
        claim("DN2", "V2", 2.5, "2026-03-10T18:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", startsWith("Invalid input")));
    }

    @Test
    void requiresSixtyMinutesBetweenOwnClaims() throws Exception {
        claim("DN2", "V2", 5, "2026-03-10T18:00").andExpect(status().isCreated());

        claim("DN1", "V2", 5, "2026-03-10T18:59")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations[0]", startsWith("Rule 7")));
        claim("DN1", "V2", 5, "2026-03-10T19:00").andExpect(status().isCreated());
    }

    @Test
    void rejectsMoreThanRemainingKg() throws Exception {
        claim("DN2", "V2", 8, "2026-03-10T18:00").andExpect(status().isCreated());

        claim("DN2", "V6", 3, "2026-03-10T19:00")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations", hasItem(startsWith("Rule 3"))));
    }

    @Test
    void clockCannotMoveBackwards() throws Exception {
        moveClock("2026-03-10T12:00").andExpect(status().isOk());
        moveClock("2026-03-10T11:59").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void collectOnlyInsidePickupWindow() throws Exception {
        claim("DN2", "V2", 5, "2026-03-10T18:00").andExpect(status().isCreated());
        Long claimId = firstClaimId("V2");

        mockMvc.perform(post("/api/volunteers/V2/claims/" + claimId + "/collect"))
                .andExpect(status().isUnprocessableEntity());

        moveClock("2026-03-10T18:30");
        mockMvc.perform(post("/api/volunteers/V2/claims/" + claimId + "/collect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COLLECTED"));
    }

    @Test
    void cancelOnlyBeforePlannedTimeAndReturnsKg() throws Exception {
        claim("DN2", "V2", 5, "2026-03-10T19:00").andExpect(status().isCreated());
        Long claimId = firstClaimId("V2");

        mockMvc.perform(post("/api/volunteers/V2/claims/" + claimId + "/cancel"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(get("/api/donors/D2/donations"))
                .andExpect(jsonPath("$[0].remainingKg").value(10));

        claim("DN2", "V2", 5, "2026-03-10T18:00").andExpect(status().isCreated());
        moveClock("2026-03-10T18:00");
        mockMvc.perform(post("/api/volunteers/V2/claims/" + firstClaimId("V2") + "/cancel"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void donorCannotCancelDonationWithActiveClaim() throws Exception {
        claim("DN2", "V2", 5, "2026-03-10T18:00").andExpect(status().isCreated());

        mockMvc.perform(post("/api/donors/D2/donations/DN2/cancel"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations[0]", containsString("ACTIVE or COLLECTED")));
    }

    @Test
    void activeClaimsBecomeMissedAndDashboardStaysBalanced() throws Exception {
        claim("DN1", "V1", 10, "2026-03-10T19:00").andExpect(status().isCreated());
        claim("DN3", "V3", 20, "2026-03-10T14:00").andExpect(status().isCreated());
        moveClock("2026-03-10T14:30");
        mockMvc.perform(post("/api/volunteers/V3/claims/" + firstClaimId("V3") + "/collect"))
                .andExpect(status().isOk());

        moveClock("2026-03-10T20:01");

        mockMvc.perform(get("/api/volunteers/V1/claims")).andExpect(jsonPath("$[0].status").value("MISSED"));
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(jsonPath("$.totals.listedKg").value(117))
                .andExpect(jsonPath("$.totals.collectedKg").value(20))
                .andExpect(jsonPath("$.totals.missedKg").value(10))
                .andExpect(jsonPath("$.totals.wastedKg").value(37))
                .andExpect(jsonPath("$.totals.inProgressKg").value(0))
                .andExpect(jsonPath("$.totals.stillOpenKg").value(50))
                .andExpect(jsonPath("$.totals.balanced").value(true));
    }

    @Test
    void wrongHttpMethodReturns405() throws Exception {
        mockMvc.perform(get("/api/donations/DN1/claims"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void rejectsDonationWithInvalidWindow() throws Exception {
        String body = "{\"description\":\"Soup\",\"quantityKg\":5,"
                + "\"pickupFrom\":\"2026-03-10T11:00\",\"pickupUntil\":\"2026-03-10T10:00\"}";
        mockMvc.perform(post("/api/donors/D1/donations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations", hasSize(2)));
    }

    private Long firstClaimId(String volunteerId) throws Exception {
        return claimIdAt(volunteerId, 0);
    }

    private Long claimIdAt(String volunteerId, int index) throws Exception {
        String json = mockMvc.perform(get("/api/volunteers/" + volunteerId + "/claims"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$[" + index + "].id")).longValue();
    }
}
