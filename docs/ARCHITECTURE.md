# WASAC Utility Billing Architecture

```mermaid
flowchart TD
    A[Client App / Swagger] --> B[REST Controllers]
    B --> C[Security Layer]
    C --> D[Service Layer]
    D --> E[Repositories]
    E --> F[(PostgreSQL)]
    D --> G[Notification Service]
    G --> H[SMTP Mail Server]
    F --> I[Flyway Migrations]
    I --> J[V1 Schema + V2 Routines/Triggers]
```

## Runtime Flow

- Request hits controller and passes through JWT filter.
- `@PreAuthorize` validates RBAC by user role.
- Service layer enforces business rules (meter status, reading rules, payment rules, tariff versioning).
- JPA persists to PostgreSQL with Flyway-managed schema.
- Notification service writes DB notification and sends SMTP email.
