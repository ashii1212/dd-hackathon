# Hackathon Starter

Hello-world starter: React frontend, Spring Boot backend, MySQL database.

## Run locally

Prerequisites: JDK 17, Maven, Node 18+, MySQL 8 on port 3306.

Backend:
```bash
cd backend
mvn spring-boot:run
```
Create `backend/application-local.properties` with `spring.datasource.password=<your MySQL password>` first. The database is created automatically.

Frontend:
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173
