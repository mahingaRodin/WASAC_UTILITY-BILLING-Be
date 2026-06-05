# WASAC Utility Billing — Manual Test Flow (Swagger)

This is a step-by-step script for testing the full system in **Swagger UI**
(`http://localhost:4000/swagger-ui.html` or `/swagger-ui/index.html`) using the real
people in your scenario. Test every endpoint in order; each step says **who** is logged in,
**what** to call, the **body** to send, and what to **verify** (including the emails sent).

## Cast (real accounts)

| Who | Email | Role | Password |
|-----|-------|------|----------|
| System Admin (seeded) | the value of `MAIL_USERNAME` in your `.env` | `ROLE_ADMIN` | `Rodin!132` |
| Mahinga Rodin (you) | `mahingarodin@gmail.com` | `ROLE_CUSTOMER` | `Olga!132` |
| Olga Muhorakeye | `muhorakeyeolga35@gmail.com` | `ROLE_OPERATOR` | set during activation |
| Elvis Mwiza | `mwizaelvis@gmaill.com` | `ROLE_FINANCE` | set during activation |

> Notes
> - The operator address `muhorakeyeolga35` had no domain in the request — this guide uses
>   `muhorakeyeolga35@gmail.com`. Adjust if it should be different.
> - The finance address is used exactly as you wrote it: `mwizaelvis@gmaill.com`. If that
>   double-l was a typo, use `mwizaelvis@gmail.com` so the approval emails actually arrive.
> - Customer `Mahinga Rodin` is already registered and auth-tested. Steps assume that.

## How to authenticate in Swagger

1. Call `POST /api/auth/login`, copy the `token` from the response.
2. Click **Authorize** (top-right padlock), paste the token (Swagger adds `Bearer `).
3. All secured calls now run as that user. Re-authorize whenever you switch person.

---

## Phase 1 — Admin creates the staff accounts

**Login as Admin** (`POST /api/auth/login`):

```json
{ "email": "PUT-YOUR-MAIL_USERNAME-HERE", "password": "Rodin!132" }
```

### 1.1 Create the OPERATOR — `POST /api/users`

```json
{
  "fullName": "Olga Muhorakeye",
  "email": "muhorakeyeolga35@gmail.com",
  "phone": "0788111222",
  "password": "Temp@1234",
  "role": "ROLE_OPERATOR"
}
```

Verify: `201/200` with the user; `emailVerified=false`, `mustChangePassword=true`.
An **activation email** with the temporary password `Temp@1234` is sent to the operator.

### 1.2 Create the FINANCE user — `POST /api/users`

```json
{
  "fullName": "Elvis Mwiza",
  "email": "mwizaelvis@gmaill.com",
  "phone": "0788333444",
  "password": "Temp@5678",
  "role": "ROLE_FINANCE"
}
```

Verify: same as above; activation email sent to the finance user.

### 1.3 Prove the temp password cannot log in yet — `POST /api/auth/login`

```json
{ "email": "muhorakeyeolga35@gmail.com", "password": "Temp@1234" }
```

Verify: `400` — *"You must set your own password before logging in. Use /api/auth/activate-account."*

---

## Phase 2 — Staff activate their accounts (set their own password)

These endpoints are **public** (no token needed).

### 2.1 Operator activates — `POST /api/auth/activate-account`

```json
{
  "email": "muhorakeyeolga35@gmail.com",
  "temporaryPassword": "Temp@1234",
  "newPassword": "Olga@2026"
}
```

Verify: `200` *"Account activated…"*. The operator gets a **welcome email** + a
**password-updated email**.

### 2.2 Finance activates — `POST /api/auth/activate-account`

```json
{
  "email": "mwizaelvis@gmaill.com",
  "temporaryPassword": "Temp@5678",
  "newPassword": "Elvis@2026"
}
```

### 2.3 Confirm they can now log in — `POST /api/auth/login`

```json
{ "email": "muhorakeyeolga35@gmail.com", "password": "Olga@2026" }
```

Verify: `200` with a JWT. Do the same for finance (`Elvis@2026`).

---

## Phase 3 — Operator sets up customer data & captures a reading

**Login as Operator** (`Olga@2026`) and Authorize in Swagger.

### 3.1 Create the customer record for Mahinga — `POST /api/customers`

> The email **must match** the customer's login (`mahingarodin@gmail.com`) so the
> customer's `/me` self-service endpoints resolve.

```json
{
  "nationalId": "1199080012345678",
  "fullName": "Mahinga Rodin",
  "email": "mahingarodin@gmail.com",
  "phone": "0788415318",
  "status": "ACTIVE",
  "address": {
    "province": "Kigali",
    "district": "Gasabo",
    "sector": "Remera",
    "cell": "Rukiri",
    "village": "Amahoro"
  }
}
```

Copy the returned customer **`id`**.

### 3.2 Create a meter — `POST /api/meters`

```json
{
  "meterNumber": "WTR-0001",
  "type": "WATER",
  "installationDate": "2026-01-01",
  "status": "ACTIVE",
  "customerId": "PASTE-CUSTOMER-ID"
}
```

Copy the returned meter **`id`**.

### 3.3 Capture a meter reading — `POST /api/meter-readings`

```json
{
  "meterId": "PASTE-METER-ID",
  "previousReading": 0,
  "currentReading": 25,
  "readingDate": "2026-01-31"
}
```

Verify: `200`. Business rules in play — current > previous, meter must be ACTIVE,
one reading per meter per month.

### 3.4 Operator views ONLY their own readings — `GET /api/meter-readings/me`

Verify: the reading just captured appears, with `recordedByEmail = muhorakeyeolga35@gmail.com`.

---

## Phase 4 — Operator generates a bill (starts PENDING)

### 4.1 Create a bill — `POST /api/bills` (Operator)

```json
{
  "billReference": "BILL-2026-01-RODIN",
  "customerId": "PASTE-CUSTOMER-ID",
  "meterId": "PASTE-METER-ID",
  "billingYear": 2026,
  "billingMonth": 1,
  "unitsConsumed": 25,
  "amountDue": 10000,
  "dueDate": "2026-02-28",
  "status": "UNPAID",
  "lineItems": [
    { "itemType": "CONSUMPTION", "description": "Water usage 25 m3", "quantity": 25, "unitPrice": 360 },
    { "itemType": "FIXED_SERVICE", "description": "Fixed service charge", "quantity": 1, "unitPrice": 1000 }
  ]
}
```

Verify: `200`, `approvalStatus = PENDING`. **A finance email** ("Bill Awaiting Approval")
is sent to `mwizaelvis@gmaill.com`. The customer is **not** notified yet.

### 4.2 Customer cannot see/pay it yet (optional check)

**Login as Mahinga** (`mahingarodin@gmail.com` / `Olga!132`) → `GET /api/bills/me`.
The bill appears in their list but is still `PENDING` approval; trying to pay it (Phase 6)
before approval returns `400 Bill has not been approved by finance yet.`

---

## Phase 5 — Finance reviews & approves the bill

**Login as Finance** (`Elvis@2026`).

### 5.1 List bills awaiting approval — `GET /api/bills/pending`

Verify: `BILL-2026-01-RODIN` is listed. Copy its **`id`**.

### 5.2 Approve the bill — `POST /api/bills/{id}/approve`

Verify: `200`, `approvalStatus = APPROVED`. **The customer is now emailed the bill with the
PDF attached** ("Bill Generated").

> To reject instead: `POST /api/bills/{id}/reject` → `approvalStatus = REJECTED` (no customer email).

### 5.3 Customer downloads the bill PDF

**Login as Mahinga** → `GET /api/bills/{id}/pdf` (open in browser/Swagger "Download").
Verify: a styled PDF bill.

---

## Phase 6 — Customer pays partially; finance approves; customer notified

### 6.1 Customer submits a partial payment — `POST /api/payments` (Mahinga)

```json
{
  "billReference": "BILL-2026-01-RODIN",
  "amountPaid": 4000,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-02-10"
}
```

Verify: `200`, response `status = PENDING`, message says "pending finance approval",
`receiptUrl = null`. **Finance gets a "Payment Awaiting Approval" email** and **Mahinga gets
a "Payment Submitted" email** saying the payment is pending finance approval. The bill balance
is unchanged so far.

### 6.2 Finance approves the partial payment — `POST /api/payments/{id}/approve` (Finance)

First `GET /api/payments/pending` to copy the payment **`id`**, then approve.

Verify: `200`, bill `status = PARTIALLY_PAID`, `outstandingBalance = 6000`.
**The customer is emailed** the amount paid (4000 FRW) and remaining balance (6000 FRW).

---

## Phase 7 — Customer pays the rest; finance approves; receipt issued

### 7.1 Customer submits the remaining payment — `POST /api/payments` (Mahinga)

```json
{
  "billReference": "BILL-2026-01-RODIN",
  "amountPaid": 6000,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-02-20"
}
```

Verify: `200`, `status = PENDING`. Finance and Mahinga both get pending-payment emails.

### 7.2 Finance approves it — `POST /api/payments/{id}/approve` (Finance)

Verify: `200`, bill `status = PAID`, `outstandingBalance = 0`, response includes
`receiptUrl = /api/bills/{billId}/receipt`. **The customer is emailed** the exact message:

> Dear Mahinga Rodin,
> Your 1/2026 utility bill of 10000 FRW has been successfully processed.

…with the **receipt PDF attached**.

### 7.3 Customer downloads the receipt — `GET /api/bills/{billId}/receipt` (Mahinga)

Verify: a styled PAID receipt PDF (only available because the bill is fully `PAID`).

---

## Phase 8 — Other notifications to test

### 8.1 Password change email — `POST /api/auth/forgot-password/request` then `/reset`

As Mahinga: request reset (OTP emailed), then `POST /api/auth/forgot-password/reset`:

```json
{ "email": "mahingarodin@gmail.com", "otp": "PASTE-OTP", "newPassword": "Olga!2026" }
```

Verify: `200` and a **"Password Updated"** email.

### 8.2 Tariff broadcast — `POST /api/tariffs` (Admin)

```json
{
  "utilityType": "WATER",
  "tariffType": "FLAT",
  "flatRate": 400,
  "effectiveFrom": "2026-07-01",
  "version": 2
}
```

Verify: `200`. **All active customers and all users except the admin who set it** receive a
"New Tariff Published" email (so Mahinga, Olga and Elvis get it; the acting admin does not).

### 8.3 Charge broadcast — `POST /api/charges` (Admin)

```json
{
  "chargeType": "VAT",
  "utilityType": "WATER",
  "valueType": "PERCENTAGE",
  "value": 18,
  "effectiveFrom": "2026-07-01",
  "version": 2
}
```

Verify: broadcast email like 8.2.

### 8.4 Deadline reminder (scheduled)

`BillReminderScheduler` runs on cron `app.reminders.cron` (default `0 0 8 * * *`) and emails
customers whose approved, unpaid bills are due within `app.reminders.lead-days` (default 3).
To see it immediately, set a near-future `dueDate` on an approved unpaid bill and a soon cron,
or lower `app.reminders.lead-days`.

---

## Quick RBAC sanity checks (expect 403)

- Operator calls `POST /api/payments/{id}/approve` → 403 (finance/admin only).
- Customer calls `GET /api/bills/pending` → 403.
- Finance calls `POST /api/bills` (create) → 403 (operator/admin only).
- Customer calls `GET /api/meter-readings/me` → 403 (operator/admin only).
- Operator calls `POST /api/users` → 403 (admin only).

## Endpoint cheat-sheet (new in this iteration)

| Method & path | Role | Purpose |
|---------------|------|---------|
| `POST /api/auth/activate-account` | public | Staff set own password using temp password |
| `GET /api/meter-readings/me` | OPERATOR/ADMIN | Operator's own captured readings |
| `GET /api/bills/pending` | FINANCE/ADMIN | Bills awaiting approval |
| `POST /api/bills/{id}/approve` | FINANCE/ADMIN | Approve bill → notify customer + PDF |
| `POST /api/bills/{id}/reject` | FINANCE/ADMIN | Reject bill |
| `POST /api/payments` | CUSTOMER/FINANCE/ADMIN | Submit a payment (pending) |
| `GET /api/payments/pending` | FINANCE/ADMIN | Payments awaiting approval |
| `POST /api/payments/{id}/approve` | FINANCE/ADMIN | Approve payment → update balance + notify |
| `POST /api/payments/{id}/reject` | FINANCE/ADMIN | Reject payment |
