# Ingestor Service

The **Ingestor Service** is a core Java Spring Boot microservice in the DriftGuard ecosystem. It operates on port `9002` (routed via the API Gateway on port `9000`) and serves as the authoritative source for managing infrastructure project lifecycles and configuration baselines.

## Core Responsibilities

1. **Project Management:** 
   Allows authenticated Project Managers to create and manage cloud infrastructure monitoring projects.
2. **Baseline Storage:** 
   Securely ingests and stores the master JSON/YAML baseline configuration files directly into PostgreSQL as `bytea` (BLOB) objects.
3. **Tracking Hash Generation:** 
   Automatically computes and assigns a unique `Project Hash` (SHA-256) upon project creation. This hash is strictly used to authenticate external telemetry data against this specific project.
4. **Data Seeding:** 
   Contains a `DataSeeder` that automatically provisions a "Sample Project" and baseline file on startup to streamline development.

## API Endpoints

All endpoints are protected by JWT authentication and expect requests to be routed through the API Gateway (`/INGESTOR/...`).

* `POST /enter/project/details` - Registers a new project and uploads the initial baseline file (Multipart). Returns the generated Project Hash.
* `GET /get/project/info` - Retrieves a list of all projects owned by the authenticated Project Manager.
* `PUT /change/status` - Updates the tracking status (e.g., `PLANNING`, `IN_PROGRESS`, `FINISHED`) of a project.
* `PUT /change/baseline` - Uploads a new baseline configuration file to overwrite the existing one.

## Database Schema (PostgreSQL)

The service maps the `ProjectManagerEntity` to PostgreSQL with the following key fields:
* `id` (Primary Key)
* `projectName` (Unique identifier)
* `managerName` (The owner of the project)
* `status` (Enum representing the current monitoring state)
* `projectHash` (SHA-256 hash used for external agent authentication)
* `baselineFile` (The raw JSON/YAML configuration file stored as a `bytea` column)

## Local Development

Ensure PostgreSQL is running on `localhost:5432` with a database named `project_db`.

To run the Ingestor Service locally:
```bash
mvn clean compile spring-boot:run
```
