# SurplusLink

**A Smart Surplus Food Donation and Collection Platform**

SurplusLink is a web application that connects restaurants with volunteers to reduce food waste. Restaurants can list surplus cooked food with a pickup window, and volunteers can reserve and collect it. The backend enforces business rules, prevents overbooking, and tracks how much food is collected, missed, or wasted.

## Features

### 1. Acting as
- Select any donor or volunteer from a dropdown.
- No login or authentication required.
- Users can perform actions according to the selected role.

### 2. Simulated Clock
- Display the current simulated time on every page.
- Advance time by 30 minutes, 1 hour, or a chosen date and time.
- Moving the clock backwards is not allowed.
- All business rules use the simulated clock, never the real system time.

### 3. Donor Features
- Post surplus food donations with quantity and pickup window.
- View donations and all associated claims.
- Cancel donations.
- Track the status and remaining quantity of each donation.

### 4. Volunteer Features
- Browse open donations available in their zones.
- Reserve food by specifying the quantity and planned pickup time.
- Collect or cancel their own claims.
- View claim history and status.
- Receive a list of every violated business rule when a claim is refused.

### 5. Dashboard and Reports
- Track listed, collected, missed, wasted, in-progress, and still-open food quantities in kilograms.
- Verify that the food balance is correct.
- View collected quantities by donor.
- View collected quantities and missed claims by volunteer.

### 6. Database Persistence
- Store application state in MySQL.
- Preserve data across backend restarts.
- Load `seed.json` only when the database has no saved state.

## Technology Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite |
| Backend | Spring Boot 3.2, Java 17 |
| Persistence | Spring Data JPA |
| Database | MySQL 8 |
| Testing | JUnit 5, MockMvc, H2 |

## System Architecture

```text
React Frontend
      |
      | REST API / JSON
      v
Spring Boot Controllers
      |
      v
Service Layer
      |
      v
Spring Data JPA Repositories
      |
      v
MySQL Database
```

## Core Design Decisions

### 1. Calculated Donation Status

Donation status is calculated dynamically using `Donation.statusAt(now)` rather than stored in the database.

Statuses are evaluated in this order:

1. `CANCELLED`
2. `EXPIRED`
3. `FULLY_CLAIMED`
4. `OPEN`

Remaining quantity is calculated by subtracting all non-cancelled claims from the original donation quantity. Cancelling a claim automatically makes its quantity available again.

### 2. Stored Claim Status

Claim status is stored and can be:

- `ACTIVE`
- `COLLECTED`
- `CANCELLED`
- `MISSED`

When the simulated clock moves beyond a donation's `pickup_until` time, its active claims are marked as `MISSED` in the same transaction.

### 3. Simulated Clock

The simulated time is stored in a single row in the `app_clock` table. All time-dependent business operations use this clock.

### 4. Preventing Overbooking

Every write operation locks the clock row first and the relevant donation row second using `SELECT ... FOR UPDATE`.

If two volunteers attempt to reserve the last available kilograms simultaneously, the second request waits until the first transaction completes. It then checks the updated quantity and rejects any reservation that exceeds the remaining capacity.

### 5. Error Handling

| HTTP Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid input, missing fields, non-whole kilogram values, or invalid time format |
| `404 Not Found` | Unknown records or attempts to access another user's records |
| `405 Method Not Allowed` | Wrong HTTP method for an endpoint |
| `415 Unsupported Media Type` | Request body is not JSON |
| `422 Unprocessable Entity` | One or more business rules have been violated |

Business rule errors include a `violations` list containing every detected violation.

### 6. Dashboard Food Balance

For every non-cancelled donation, the original quantity is divided into:

- Collected food
- Missed food claims
- Active claims
- Remaining food that is either wasted or still open

Therefore:

**Listed = Collected + Missed + Wasted + In Progress + Still Open**

All dashboard quantities are measured in kilograms.

## Business Rules and Assumptions

1. A donation becomes `EXPIRED` only when the simulated time is strictly later than `pickup_until`. At the exact deadline, it can still be claimed or collected.
2. Active claims become `MISSED` when the simulated clock moves strictly beyond `pickup_until`.
3. Volunteers can see only donations with `OPEN` status. Fully claimed donations are hidden.
4. Rule 7 requires at least 60 minutes between a volunteer's claims, comparing planned pickup times against all their `ACTIVE` and `COLLECTED` claims across donations. A gap of exactly 60 minutes is allowed.
5. Claiming a fully claimed donation violates Rule 3 because insufficient quantity remains.
6. A donation can be posted with a `pickup_from` time in the past, provided `pickup_until` is not before the simulated current time.
7. Collection eligibility depends on the donation's pickup window, not the claim's planned pickup time.
8. Authentication is not implemented. The acting person is identified through the URL, and donors and volunteers can modify only their own records.
9. New donation IDs continue the seed pattern, such as `DN6`, `DN7`, and so on. Claim IDs are numeric.
10. All times use the local format `YYYY-MM-DDTHH:MM`, without a time-zone offset.

## Getting Started

### Prerequisites

Install the following software before running the project:

- JDK 17 or later
- Maven 3.8 or later
- Node.js 18 or later
- MySQL 8
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/ashii1212/dd-hackathon.git
cd dd-hackathon
```

### 2. Set Up the Database

Ensure MySQL is running on `localhost:3306`.

The application uses the `hackathon_db` database. The database and its tables are created automatically on first start.

### 3. Configure the Backend

Navigate to the backend directory:

```bash
cd backend
```

Create `application-local.properties` with your MySQL password:

```properties
spring.datasource.password=your_mysql_password
```

This file is already ignored by git.

Alternatively, configure the database connection through environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

### 4. Start the Backend

From the `backend` directory, run:

```bash
mvn spring-boot:run
```

The API will be available at:

**http://localhost:8080**

On the first run against an empty database, the application loads the initial data from `seed.json`.

### 5. Start the Frontend

Open a second terminal and navigate to the frontend directory:

```bash
cd frontend
npm install
npm run dev
```

Open the application at:

**http://localhost:5173**

The frontend is configured to proxy `/api` requests to the backend.

### 6. Run Tests

From the `backend` directory:

```bash
mvn test
```

The automated tests use an in-memory H2 database.

Coverage includes:

- Claim validation and business rules
- Reporting all rule violations
- Invalid input handling
- Collection and cancellation timing
- Missed claims
- Dashboard balance validation
- Concurrent claims for the last available kilograms

### 7. Reset the Application Data

To reload the initial seed data, stop the backend, drop the `hackathon_db` database, and restart the application.

**Warning:** Dropping the database permanently deletes its current data.

## REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/people` | Retrieve donors and volunteers |
| `GET` | `/api/clock` | Retrieve the simulated clock |
| `PUT` | `/api/clock` | Advance the simulated clock |
| `GET` | `/api/donors/{donorId}/donations` | Get a donor's donations and claims |
| `POST` | `/api/donors/{donorId}/donations` | Create a donation |
| `POST` | `/api/donors/{donorId}/donations/{donationId}/cancel` | Cancel a donation |
| `GET` | `/api/volunteers/{volunteerId}/donations` | Get open donations in the volunteer's zones |
| `POST` | `/api/donations/{donationId}/claims` | Create a claim |
| `GET` | `/api/volunteers/{volunteerId}/claims` | Get a volunteer's claims |
| `POST` | `/api/volunteers/{volunteerId}/claims/{claimId}/collect` | Mark a claim as collected |
| `POST` | `/api/volunteers/{volunteerId}/claims/{claimId}/cancel` | Cancel a claim |
| `GET` | `/api/dashboard` | Retrieve dashboard metrics |

### Example Request Bodies

**Create a donation**

```json
{
  "description": "Prepared vegetable meals",
  "quantityKg": 20,
  "pickupFrom": "2026-03-10T18:00",
  "pickupUntil": "2026-03-10T20:00"
}
```

**Create a claim**

```json
{
  "volunteerId": "V1",
  "kg": 5,
  "plannedTime": "2026-03-10T18:30"
}
```

**Advance the simulated clock**

```json
{
  "now": "2026-03-10T18:00"
}
```

The example timestamps illustrate the required format. Actual requests must respect the current simulated time and the applicable business rules.

### Example Business Rule Violation

When a claim violates multiple rules, the API returns all detected violations:

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

## Project Goals

- Reduce food waste by connecting food donors with volunteers.
- Enforce donation capacity and pickup constraints reliably.
- Prevent overbooking during concurrent requests.
- Provide transparent reporting of collected, missed, and wasted food.
- Demonstrate a full-stack application with persistent storage, transactional integrity, and automated testing.
