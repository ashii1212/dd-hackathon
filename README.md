# SurplusLink

A web app where restaurants post surplus cooked food with a pickup window, and volunteers reserve and collect it.
The backend enforces the capacity, zone and timing rules, never over-books a donation, and reports how much food was saved or wasted.

## Features

- **Acting as**: pick any donor or volunteer from a dropdown (no login).
- **Simulated clock**: shown on every page; can be moved forward (+30 min, +1 h, or a chosen time), never backwards. The real system time is never used.
- **Donors**: post a donation, see their donations with all claims on them, cancel a donation.
- **Volunteers**: see OPEN donations in their zones, claim with a planned pickup time, collect and cancel their claims. A refused claim lists every violated rule.
- **Dashboard**: Listed, Collected, Missed, Wasted, In progress and Still open (kg), a live check that Listed equals the sum of the other five, collected kg per donor, and collected kg and missed claims per volunteer.
- **Persistence**: all state is stored in MySQL and survives a backend restart. `seed.json` is loaded only when the database has no saved state.

## Tech Stack

| Layer    | Technology                                 |
|----------|--------------------------------------------|
| Frontend | React 19, Vite                             |
| Backend  | Spring Boot 3.2 (Java 17), Spring Data JPA |
| Database | MySQL 8                                    |
| Tests    | JUnit 5, MockMvc, H2 (in-memory)           |

## Design

```
React  --REST/JSON-->  Controller -> Service -> Repository  -->  MySQL
```

- **Donation status is calculated, not stored.** `Donation.statusAt(now)` applies the rules in order: CANCELLED, EXPIRED, FULLY_CLAIMED, OPEN. Remaining kg is quantity minus all claims that are not CANCELLED, so cancelling a claim returns its kg automatically.
- **Claim status is stored** (ACTIVE, COLLECTED, CANCELLED, MISSED). When the clock moves past a donation's `pickup_until`, its ACTIVE claims are set to MISSED in the same transaction as the clock change.
- **The clock** is a single row in the `app_clock` table.
- **No over-booking.** Every write first locks the clock row and then the donation row (`SELECT ... FOR UPDATE`). If two volunteers claim the last kilograms at the same moment, the second request waits, then sees the updated remaining kg and is refused under rule 3.
- **Errors**: `400` for invalid input (missing fields, non-whole kg, wrong time format), `404` for unknown or someone else's records, `422` for business rule violations with a `violations` list containing every broken rule.
- **Dashboard balance**: each non-cancelled donation's quantity splits into its claims (collected, missed or active) and its remaining kg (wasted if expired, otherwise still open), so the five figures always add up to Listed.

## Assumptions

1. A donation is EXPIRED only when now is strictly after `pickup_until`. At exactly `pickup_until` it can still be claimed and collected.
2. ACTIVE claims become MISSED when the clock moves strictly past `pickup_until`.
3. Volunteers see only donations whose status is OPEN; FULLY_CLAIMED donations are hidden.
4. Rule 7 (60 minutes apart) compares against all of the volunteer's ACTIVE and COLLECTED claims, across all donations. A gap of exactly 60 minutes is allowed.
5. Claiming a FULLY_CLAIMED donation is refused under rule 3 (not enough remaining kg).
6. When posting, `pickup_from` may already be in the past, as long as `pickup_until` is not before now.
7. Collecting depends only on the pickup window, not on the planned time.
8. Without login, the acting person is sent in the URL. Donors can only cancel their own donations and volunteers can only collect or cancel their own claims.
9. New donations get ids continuing the seed pattern (`DN6`, `DN7`, ...). Claims get numeric ids.
10. Times are exchanged as `YYYY-MM-DDTHH:MM`, local, with no time zone.

## Running Locally

Prerequisites: JDK 17+, Maven 3.8+, Node 18+, MySQL 8 running on `localhost:3306`.

### Backend

```bash
cd backend
mvn spring-boot:run
```

Before the first run, create `backend/application-local.properties` (ignored by git) with your MySQL password:

```properties
spring.datasource.password=your_mysql_password
```

The database `hackathon_db` and its tables are created automatically, and the seed is loaded on first start.
Alternatively set the environment variables `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

The API runs at http://localhost:8080.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Requests to `/api` are proxied to the backend.

### Tests

```bash
cd backend
mvn test
```

The tests use an in-memory H2 database and cover the claim rules, reporting of all violations, invalid input, collect and cancel timing, MISSED claims, the dashboard balance, and two simultaneous claims for the last kilograms.

To start again from the seed, drop the `hackathon_db` database and restart the backend.

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET  | `/api/people` | Donors and volunteers |
| GET  | `/api/clock` | Current simulated time |
| PUT  | `/api/clock` | Move the clock forward: `{ "now": "2026-03-10T18:00" }` |
| GET  | `/api/donors/{donorId}/donations` | A donor's donations with their claims |
| POST | `/api/donors/{donorId}/donations` | Post a donation: `{ description, quantityKg, pickupFrom, pickupUntil }` |
| POST | `/api/donors/{donorId}/donations/{donationId}/cancel` | Cancel a donation |
| GET  | `/api/volunteers/{volunteerId}/donations` | OPEN donations in the volunteer's zones |
| POST | `/api/donations/{donationId}/claims` | Claim: `{ volunteerId, kg, plannedTime }` |
| GET  | `/api/volunteers/{volunteerId}/claims` | A volunteer's claims |
| POST | `/api/volunteers/{volunteerId}/claims/{claimId}/collect` | Mark a claim collected |
| POST | `/api/volunteers/{volunteerId}/claims/{claimId}/cancel` | Cancel a claim |
| GET  | `/api/dashboard` | Dashboard figures |

Example refusal:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Request violates business rules",
  "violations": [
    "Rule 2: 12 kg is more than your capacity of 10 kg",
    "Rule 4: the donation is in zone NORTH, which is not one of your zones",
    "Rule 5: planned time must be between 2026-03-10T18:00 and 2026-03-10T20:00"
  ]
}
```
