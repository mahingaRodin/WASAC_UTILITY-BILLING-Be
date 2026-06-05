-- Force admin-created staff (OPERATOR/FINANCE/ADMIN) to set their own password before login.
ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Track which operator captured each meter reading (for "my readings" self-service).
ALTER TABLE meter_readings ADD COLUMN recorded_by_user_id UUID NULL REFERENCES users(id);

-- Finance approval workflow for bills: bills start PENDING and become payable once APPROVED.
ALTER TABLE bills ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
-- Any bills that existed before this workflow are treated as already approved.
UPDATE bills SET approval_status = 'APPROVED';

-- Finance approval workflow for payments: payments start PENDING and only affect the
-- bill balance / customer notifications once APPROVED.
ALTER TABLE payments ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
-- Any payments that existed before this workflow are treated as already approved.
UPDATE payments SET status = 'APPROVED';
