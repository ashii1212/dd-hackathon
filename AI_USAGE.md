# AI Usage

I used **Claude Code** as a coding assistant. I directed the work: I chose the stack, worked out the requirements
and assumptions from the problem statement, approved the design before implementation, and reviewed, tested and
committed each change in small steps. Claude Code drafted most of the code; I accepted, changed or rejected each part.

## My decisions

- **Stack:** React, Spring Boot and MySQL, deployed on Vercel, Render and Aiven (hosting set up by me).
- **Scope:** build only what the statement asks; no login, no extra features.
- **Design approval:** data model, API shape and the README assumptions were reviewed and approved by me before coding.
- **Work order:** backend first (model, clock, donations, claims, dashboard), then frontend, tests and documentation.
- **Repository style:** small commits with clear messages, no unnecessary comments, no extra files.
- **UI:** I asked for a more colourful, professional design.

## Where AI helped

- Drafting the JPA entities, services and REST controllers.
- Implementing the seven claim rules so that every violated rule is reported.
- Suggesting database row locks to stop two volunteers over-booking the last kilograms.
- Drafting the React components, the dashboard calculation and the automated tests.
- Diagnosing a deployment failure (see below).

## What I changed or rejected

1. **Removed the starter's example code** so the repository only contains what the task needs.
2. **Rejected extra files** that were suggested (Windows run scripts, example config files).
3. **No real system time.** The starter's error handler added a `timestamp` from the system clock; it was removed because the statement forbids using real time.
4. **Strict number parsing.** By default `2.5` would be rounded to `2`. It is now rejected as invalid input, separate from rule violations.
5. **Deployment fix.** The deployed backend returned 502 although it worked locally. The cause was that Aiven MySQL requires a primary key on every table and the volunteer zones table had none; making the zone column non-null fixed it.
6. **Unknown URLs** returned 500; they now return 404.

## How the code was verified

- `mvn test`: 14 tests passing (claim rules, invalid input, collect and cancel timing, MISSED claims, dashboard balance, two simultaneous claims).
- Every rule and error case checked against the running API.
- Every flow checked in the browser as donor, volunteer and on the dashboard.
- `npm run build`: no errors.
