# Discovery Server

This service functions as the central registry for the DriftGuard microservices architecture, built using Spring Cloud Netflix Eureka.

### Key Responsibilities:
- **Service Registration:** Allows microservices (like Authentication and Gateway) to automatically register their presence and network locations upon startup.
- **Service Discovery:** Enables services to dynamically find and communicate with one another without hardcoding IP addresses or ports.
- **Health Monitoring:** Maintains an active directory of healthy, available service instances to facilitate reliable load balancing and routing.
