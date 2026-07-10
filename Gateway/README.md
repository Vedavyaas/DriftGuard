# Gateway Service

The Gateway acts as the single entry point for all frontend client requests into the DriftGuard microservices ecosystem. It operates on port `9000`.

### Key Responsibilities:
- **Routing:** Intercepts incoming HTTP requests and routes them to the appropriate backend microservice (e.g., routing `/AUTHENTICATION/**` traffic to the Authentication service).
- **Load Balancing:** Seamlessly distributes requests across multiple instances of backend services by integrating with the Discovery Server.
- **Abstraction:** Hides the internal network structure and individual microservice ports from the frontend, providing a clean, unified API surface.
