CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    role VARCHAR(30) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login TIMESTAMP NULL
);

CREATE TABLE otp_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_code VARCHAR(10) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    national_id VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    address_province VARCHAR(100) NOT NULL,
    address_district VARCHAR(100) NOT NULL,
    address_sector VARCHAR(100) NOT NULL,
    address_cell VARCHAR(100) NOT NULL,
    address_village VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE meters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_number VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL,
    installation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE meter_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_id UUID NOT NULL REFERENCES meters(id),
    previous_reading NUMERIC(16, 3) NOT NULL,
    current_reading NUMERIC(16, 3) NOT NULL,
    reading_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (meter_id, reading_date)
);

CREATE TABLE tariff_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utility_type VARCHAR(30) NOT NULL,
    tariff_type VARCHAR(30) NOT NULL,
    flat_rate NUMERIC(16, 3) NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    version INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tariff_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tariff_configuration_id UUID NOT NULL REFERENCES tariff_configurations(id) ON DELETE CASCADE,
    lower_bound NUMERIC(16, 3) NOT NULL,
    upper_bound NUMERIC(16, 3) NULL,
    rate NUMERIC(16, 3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE charge_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    charge_type VARCHAR(30) NOT NULL,
    utility_type VARCHAR(30) NULL,
    value_type VARCHAR(20) NOT NULL,
    charge_value NUMERIC(16, 3) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    version INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE bills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_reference VARCHAR(80) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    meter_id UUID NOT NULL REFERENCES meters(id),
    billing_year INTEGER NOT NULL,
    billing_month INTEGER NOT NULL,
    units_consumed NUMERIC(16, 3) NOT NULL,
    amount_due NUMERIC(16, 3) NOT NULL,
    paid_amount NUMERIC(16, 3) NOT NULL DEFAULT 0,
    outstanding_balance NUMERIC(16, 3) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (meter_id, billing_year, billing_month)
);

CREATE TABLE bill_line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id UUID NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    item_type VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity NUMERIC(16, 3) NOT NULL,
    unit_price NUMERIC(16, 3) NOT NULL,
    amount NUMERIC(16, 3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id UUID NOT NULL REFERENCES bills(id),
    bill_reference VARCHAR(80) NOT NULL,
    amount_paid NUMERIC(16, 3) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    bill_id UUID NULL REFERENCES bills(id),
    type VARCHAR(30) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
