# WASAC Utility Billing — Architecture & Flow

The backend is a layered Spring Boot application. Every request (except the public
`/api/auth/**` endpoints and Swagger/actuator) passes the JWT filter, is authorized by
role with `@PreAuthorize`, then flows controller → service → repository → PostgreSQL,
with notifications fanned out to the database and SMTP.

## 1. Layered components

```mermaid
flowchart TD
    subgraph Client
        U[User / API client]
    end

    U -->|HTTP + Bearer JWT| F[JwtAuthenticationFilter]
    F --> SC[SecurityFilterChain / @PreAuthorize RBAC]
    SC --> C[REST Controllers]

    subgraph Controllers
        AC[AuthController]
        UC[UserController]
        CC[CustomerController]
        MC[MeterController]
        RC[MeterReadingController]
        TC[TariffController]
        BC[BillController]
        PC[PaymentController]
        NC[NotificationController]
    end
    C --> Controllers

    Controllers --> S[Service layer + business rules]
    subgraph Services
        AS[AuthService / OtpService]
        US[UserService]
        CS[CustomerService]
        MS[MeterService]
        RS[MeterReadingService]
        TS[TariffService]
        BS[BillService]
        PS[PaymentService]
        NS[NotificationService]
        CR[CurrentCustomerResolver]
    end
    S --> Services

    Services --> MAP[DTO Mappers]
    Services --> R[Spring Data JPA Repositories]
    R --> DB[(PostgreSQL)]
    DB --> FW[Flyway V1 schema + V2 routines + V3 approval workflow]
    NS --> MAIL[SMTP Mail / Thymeleaf templates]
    NS --> PDF[PdfService: bill & receipt PDFs]
    NS --> DB
    SCH[BillReminderScheduler @Scheduled] --> NS
```

## 2. Role → capability flow

```mermaid
flowchart LR
    ADMIN[ROLE_ADMIN]
    OPERATOR[ROLE_OPERATOR]
    FINANCE[ROLE_FINANCE]
    CUSTOMER[ROLE_CUSTOMER]

    ADMIN --> UM["Users CRUD + roles/status (creates OPERATOR/FINANCE staff)"]
    ADMIN --> CFG[Tariffs & charges config]
    ADMIN --> ALL[Full access to all modules]
    ADMIN --> NOTIF[All notifications]

    OPERATOR --> CUST[Customers create/list/update]
    OPERATOR --> MET[Meters create/list/status]
    OPERATOR --> READ["Meter readings capture + view own (/me)"]
    OPERATOR --> BILL[Bill generation/list]

    FINANCE --> BILLAP["Bills: review pending, approve/reject"]
    FINANCE --> PAYAP["Payments: review pending, approve/reject"]
    FINANCE --> CFGV[Tariffs/charges view]
    FINANCE --> CUSTV[Customers view]

    CUSTOMER --> SELF["Self-service /me: profile, meters, bills, payments, notifications"]
    CUSTOMER --> SUBMIT["Submit payments for own bills (pending finance approval)"]
```

> **Onboarding rule:** only `ROLE_CUSTOMER` can self-register via `POST /api/auth/signup`.
> OPERATOR / FINANCE / ADMIN accounts are created by an admin (`POST /api/users`) with a
> temporary password; the new staff member must call `POST /api/auth/activate-account`
> to set their own password before they can log in.

## 3. Authentication & onboarding flow

Self-registering customers verify by OTP; admin-created staff activate by replacing a
temporary password.

```mermaid
sequenceDiagram
    actor Customer
    actor Admin
    actor Staff as Operator/Finance
    participant Auth as AuthController/AuthService
    participant Otp as OtpService
    participant Mail as SMTP
    participant DB as PostgreSQL

    Customer->>Auth: POST /api/auth/signup (forced ROLE_CUSTOMER)
    Auth->>DB: save user (emailVerified=false)
    Auth->>Otp: generate OTP
    Otp->>Mail: send verification email
    Customer->>Auth: POST /api/auth/verify-email/confirm (OTP)
    Auth->>DB: set emailVerified=true
    Customer->>Auth: POST /api/auth/login -> JWT

    Admin->>DB: POST /api/users (role + temporary password,
    Note over DB: mustChangePassword=true, emailVerified=false)
    Auth->>Mail: email staff the temporary password + activation steps
    Staff->>Auth: POST /api/auth/activate-account (temp + new password)
    Auth->>DB: set new password, mustChangePassword=false, emailVerified=true
    Staff->>Auth: POST /api/auth/login -> JWT
```

## 4. Billing & payment flow (finance approval gated)

A bill is created PENDING and is invisible/non-payable to the customer until **finance
approves** it; only then is the customer emailed the bill PDF. A customer's payment is
recorded PENDING and does not change the balance until **finance approves** it; only then
is the customer emailed the partial-balance update or the full-payment receipt PDF.

```mermaid
sequenceDiagram
    actor Operator
    actor Finance
    actor Customer
    participant Bill as BillService
    participant Pay as PaymentService
    participant Notif as NotificationService
    participant Mail as SMTP

    Operator->>Bill: POST /api/bills (RBAC: OPERATOR/ADMIN)
    Bill->>Bill: validations + approvalStatus=PENDING, status=UNPAID
    Bill->>Notif: notifyFinanceOfPendingBill
    Notif->>Mail: email FINANCE team ("bill awaiting approval")

    Finance->>Bill: POST /api/bills/{id}/approve
    Bill->>Bill: approvalStatus=APPROVED
    Bill->>Notif: sendBillGeneratedNotification
    Notif->>Mail: email Customer + attach bill PDF

    Customer->>Pay: POST /api/payments (own bill, must be APPROVED)
    Pay->>Pay: validate (amount <= outstanding - pending), status=PENDING
    Pay->>Notif: notifyFinanceOfPendingPayment
    Notif->>Mail: email FINANCE team ("payment awaiting approval")

    Finance->>Pay: POST /api/payments/{id}/approve
    Pay->>Pay: update paidAmount/outstanding/status
    alt outstanding == 0 (PAID)
        Pay->>Notif: sendFullPaymentNotification
        Notif->>Mail: "bill processed" email + receipt PDF
    else partial (PARTIALLY_PAID)
        Pay->>Notif: sendPartialPaymentNotification
        Notif->>Mail: "amount paid / remaining balance" email
    end
```

> The V2 DB routines (`trg_bill_insert_notification`, `process_full_payment`) still exist
> as required database-level trigger/procedure/cursor artifacts; the application-level
> approval workflow above is what drives customer-facing emails and PDFs.

## 5. Request lifecycle (every secured call)

1. Client sends request with `Authorization: Bearer <JWT>`.
2. `JwtAuthenticationFilter` validates the token and loads the user's authority (`ROLE_*`).
3. `SecurityFilterChain` permits `/api/auth/**`, Swagger and actuator; everything else requires authentication.
4. `@Valid` validates the request body (→ `400` via `GlobalExceptionHandler`).
5. `@PreAuthorize` enforces the role matrix (→ `403` on denial).
6. The service applies business rules; `CurrentCustomerResolver` maps a `ROLE_CUSTOMER` principal to their `Customer` by email for `/me` endpoints.
7. Repositories persist via JPA; Flyway owns the schema and the V2 trigger/procedure/cursor routines.
8. `NotificationService` writes a notification row and sends an SMTP email rendered from Thymeleaf templates.
9. The service maps entities to response DTOs (no lazy proxies leak to JSON) and the controller wraps them in `ApiResponse`.
