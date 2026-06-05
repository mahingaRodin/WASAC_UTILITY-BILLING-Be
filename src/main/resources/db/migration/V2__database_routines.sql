CREATE OR REPLACE FUNCTION set_updated_at_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

CREATE OR REPLACE TRIGGER trg_customers_updated_at
BEFORE UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

CREATE OR REPLACE TRIGGER trg_meters_updated_at
BEFORE UPDATE ON meters
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

CREATE OR REPLACE TRIGGER trg_tariff_configurations_updated_at
BEFORE UPDATE ON tariff_configurations
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

CREATE OR REPLACE TRIGGER trg_charge_configurations_updated_at
BEFORE UPDATE ON charge_configurations
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

CREATE OR REPLACE TRIGGER trg_bills_updated_at
BEFORE UPDATE ON bills
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

CREATE OR REPLACE FUNCTION insert_bill_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_customer_email VARCHAR(150);
    v_customer_name VARCHAR(150);
BEGIN
    SELECT email, full_name
    INTO v_customer_email, v_customer_name
    FROM customers
    WHERE id = NEW.customer_id;

    INSERT INTO notifications (customer_id, bill_id, type, channel, subject, message, status)
    VALUES (
        NEW.customer_id,
        NEW.id,
        'BILL_GENERATED',
        'EMAIL',
        'WASAC Bill Generated - ' || NEW.bill_reference,
        'Dear ' || COALESCE(v_customer_name, 'Customer') || ', your bill ' || NEW.bill_reference ||
        ' of amount ' || NEW.amount_due || ' is generated and due on ' || NEW.due_date || '.',
        'PENDING'
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_bill_insert_notification
AFTER INSERT ON bills
FOR EACH ROW
EXECUTE FUNCTION insert_bill_notification();

CREATE OR REPLACE FUNCTION process_full_payment(
    p_bill_reference VARCHAR,
    p_amount_paid NUMERIC,
    p_payment_method VARCHAR,
    p_payment_date DATE
)
RETURNS BOOLEAN AS $$
DECLARE
    v_bill RECORD;
    v_overdue RECORD;
    overdue_cursor CURSOR FOR
        SELECT id, bill_reference
        FROM bills
        WHERE customer_id = v_bill.customer_id
          AND due_date < CURRENT_DATE
          AND outstanding_balance > 0;
BEGIN
    SELECT *
    INTO v_bill
    FROM bills
    WHERE bill_reference = p_bill_reference
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Bill with reference % not found', p_bill_reference;
    END IF;

    IF p_amount_paid <= 0 THEN
        RAISE EXCEPTION 'Payment amount must be positive';
    END IF;

    INSERT INTO payments (bill_id, bill_reference, amount_paid, payment_method, payment_date)
    VALUES (v_bill.id, v_bill.bill_reference, p_amount_paid, p_payment_method, p_payment_date);

    UPDATE bills
    SET paid_amount = paid_amount + p_amount_paid,
        outstanding_balance = GREATEST(outstanding_balance - p_amount_paid, 0),
        status = CASE
            WHEN GREATEST(outstanding_balance - p_amount_paid, 0) = 0 THEN 'PAID'
            WHEN paid_amount + p_amount_paid > 0 THEN 'PARTIALLY_PAID'
            ELSE status
        END
    WHERE id = v_bill.id;

    OPEN overdue_cursor;
    LOOP
        FETCH overdue_cursor INTO v_overdue;
        EXIT WHEN NOT FOUND;

        INSERT INTO notifications (customer_id, bill_id, type, channel, subject, message, status)
        VALUES (
            v_bill.customer_id,
            v_overdue.id,
            'OVERDUE_REMINDER',
            'EMAIL',
            'Overdue Bill Reminder - ' || v_overdue.bill_reference,
            'You have an overdue bill: ' || v_overdue.bill_reference || '. Please settle it immediately.',
            'PENDING'
        );
    END LOOP;
    CLOSE overdue_cursor;

    IF EXISTS (SELECT 1 FROM bills WHERE id = v_bill.id AND status = 'PAID') THEN
        INSERT INTO notifications (customer_id, bill_id, type, channel, subject, message, status)
        VALUES (
            v_bill.customer_id,
            v_bill.id,
            'PAYMENT_COMPLETED',
            'EMAIL',
            'Payment Received - ' || v_bill.bill_reference,
            'Thank you. Full payment has been received for bill ' || v_bill.bill_reference || '.',
            'PENDING'
        );
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
