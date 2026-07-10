# Authentication Service

This service is responsible for managing user identities, roles, and securing access to the DriftGuard platform. 

### Key Responsibilities:
- **User Authentication:** Validates user credentials and issues signed JWT (JSON Web Tokens) upon successful login.
- **Account Management:** Handles creation of new analysts, updating user details, and toggling account status (activation/invalidation).
- **Role-Based Access Control:** Enforces authorization rules using `@PreAuthorize` to ensure only Admins can create new users or alter system access.
- **Self-Service:** Allows authenticated users to view and update their own profile information securely.
