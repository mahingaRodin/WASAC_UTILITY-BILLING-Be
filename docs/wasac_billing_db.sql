--
-- PostgreSQL database dump
--

\restrict XxscBcrZ0fJDDir3klmd3eP8rTnKwJ3I2IdltUZWidaDmnDlZ5bdQ3Ekd8E0i6s

-- Dumped from database version 17.10 (Debian 17.10-1.pgdg13+1)
-- Dumped by pg_dump version 17.10 (Debian 17.10-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.tariff_tiers DROP CONSTRAINT IF EXISTS tariff_tiers_tariff_configuration_id_fkey;
ALTER TABLE IF EXISTS ONLY public.payments DROP CONSTRAINT IF EXISTS payments_bill_id_fkey;
ALTER TABLE IF EXISTS ONLY public.otp_tokens DROP CONSTRAINT IF EXISTS otp_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_customer_id_fkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_bill_id_fkey;
ALTER TABLE IF EXISTS ONLY public.meters DROP CONSTRAINT IF EXISTS meters_customer_id_fkey;
ALTER TABLE IF EXISTS ONLY public.meter_readings DROP CONSTRAINT IF EXISTS meter_readings_recorded_by_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.meter_readings DROP CONSTRAINT IF EXISTS meter_readings_meter_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bills DROP CONSTRAINT IF EXISTS bills_meter_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bills DROP CONSTRAINT IF EXISTS bills_customer_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bill_line_items DROP CONSTRAINT IF EXISTS bill_line_items_bill_id_fkey;
DROP TRIGGER IF EXISTS trg_users_updated_at ON public.users;
DROP TRIGGER IF EXISTS trg_tariff_configurations_updated_at ON public.tariff_configurations;
DROP TRIGGER IF EXISTS trg_meters_updated_at ON public.meters;
DROP TRIGGER IF EXISTS trg_customers_updated_at ON public.customers;
DROP TRIGGER IF EXISTS trg_charge_configurations_updated_at ON public.charge_configurations;
DROP TRIGGER IF EXISTS trg_bills_updated_at ON public.bills;
DROP TRIGGER IF EXISTS trg_bill_insert_notification ON public.bills;
DROP INDEX IF EXISTS public.flyway_schema_history_s_idx;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE IF EXISTS ONLY public.tariff_tiers DROP CONSTRAINT IF EXISTS tariff_tiers_pkey;
ALTER TABLE IF EXISTS ONLY public.tariff_configurations DROP CONSTRAINT IF EXISTS tariff_configurations_pkey;
ALTER TABLE IF EXISTS ONLY public.payments DROP CONSTRAINT IF EXISTS payments_pkey;
ALTER TABLE IF EXISTS ONLY public.otp_tokens DROP CONSTRAINT IF EXISTS otp_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_pkey;
ALTER TABLE IF EXISTS ONLY public.meters DROP CONSTRAINT IF EXISTS meters_pkey;
ALTER TABLE IF EXISTS ONLY public.meters DROP CONSTRAINT IF EXISTS meters_meter_number_key;
ALTER TABLE IF EXISTS ONLY public.meter_readings DROP CONSTRAINT IF EXISTS meter_readings_pkey;
ALTER TABLE IF EXISTS ONLY public.meter_readings DROP CONSTRAINT IF EXISTS meter_readings_meter_id_reading_date_key;
ALTER TABLE IF EXISTS ONLY public.flyway_schema_history DROP CONSTRAINT IF EXISTS flyway_schema_history_pk;
ALTER TABLE IF EXISTS ONLY public.customers DROP CONSTRAINT IF EXISTS customers_pkey;
ALTER TABLE IF EXISTS ONLY public.customers DROP CONSTRAINT IF EXISTS customers_national_id_key;
ALTER TABLE IF EXISTS ONLY public.customers DROP CONSTRAINT IF EXISTS customers_email_key;
ALTER TABLE IF EXISTS ONLY public.charge_configurations DROP CONSTRAINT IF EXISTS charge_configurations_pkey;
ALTER TABLE IF EXISTS ONLY public.bills DROP CONSTRAINT IF EXISTS bills_pkey;
ALTER TABLE IF EXISTS ONLY public.bills DROP CONSTRAINT IF EXISTS bills_meter_id_billing_year_billing_month_key;
ALTER TABLE IF EXISTS ONLY public.bills DROP CONSTRAINT IF EXISTS bills_bill_reference_key;
ALTER TABLE IF EXISTS ONLY public.bill_line_items DROP CONSTRAINT IF EXISTS bill_line_items_pkey;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.tariff_tiers;
DROP TABLE IF EXISTS public.tariff_configurations;
DROP TABLE IF EXISTS public.payments;
DROP TABLE IF EXISTS public.otp_tokens;
DROP TABLE IF EXISTS public.notifications;
DROP TABLE IF EXISTS public.meters;
DROP TABLE IF EXISTS public.meter_readings;
DROP TABLE IF EXISTS public.flyway_schema_history;
DROP TABLE IF EXISTS public.customers;
DROP TABLE IF EXISTS public.charge_configurations;
DROP TABLE IF EXISTS public.bills;
DROP TABLE IF EXISTS public.bill_line_items;
DROP FUNCTION IF EXISTS public.set_updated_at_timestamp();
DROP FUNCTION IF EXISTS public.process_full_payment(p_bill_reference character varying, p_amount_paid numeric, p_payment_method character varying, p_payment_date date);
DROP FUNCTION IF EXISTS public.insert_bill_notification();
DROP EXTENSION IF EXISTS pgcrypto;
--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: insert_bill_notification(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.insert_bill_notification() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: process_full_payment(character varying, numeric, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.process_full_payment(p_bill_reference character varying, p_amount_paid numeric, p_payment_method character varying, p_payment_date date) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: set_updated_at_timestamp(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.set_updated_at_timestamp() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: bill_line_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bill_line_items (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bill_id uuid NOT NULL,
    item_type character varying(30) NOT NULL,
    description character varying(255) NOT NULL,
    quantity numeric(16,3) NOT NULL,
    unit_price numeric(16,3) NOT NULL,
    amount numeric(16,3) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: bills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bills (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bill_reference character varying(80) NOT NULL,
    customer_id uuid NOT NULL,
    meter_id uuid NOT NULL,
    billing_year integer NOT NULL,
    billing_month integer NOT NULL,
    units_consumed numeric(16,3) NOT NULL,
    amount_due numeric(16,3) NOT NULL,
    paid_amount numeric(16,3) DEFAULT 0 NOT NULL,
    outstanding_balance numeric(16,3) NOT NULL,
    due_date date NOT NULL,
    status character varying(20) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    approval_status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL
);


--
-- Name: charge_configurations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.charge_configurations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    charge_type character varying(30) NOT NULL,
    utility_type character varying(30),
    value_type character varying(20) NOT NULL,
    charge_value numeric(16,3) NOT NULL,
    effective_from date NOT NULL,
    effective_to date,
    version integer NOT NULL,
    active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: customers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customers (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    national_id character varying(30) NOT NULL,
    full_name character varying(150) NOT NULL,
    email character varying(150) NOT NULL,
    phone character varying(30) NOT NULL,
    status character varying(20) NOT NULL,
    address_province character varying(100) NOT NULL,
    address_district character varying(100) NOT NULL,
    address_sector character varying(100) NOT NULL,
    address_cell character varying(100) NOT NULL,
    address_village character varying(100) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: meter_readings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.meter_readings (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    meter_id uuid NOT NULL,
    previous_reading numeric(16,3) NOT NULL,
    current_reading numeric(16,3) NOT NULL,
    reading_date date NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    recorded_by_user_id uuid
);


--
-- Name: meters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.meters (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    meter_number character varying(100) NOT NULL,
    type character varying(30) NOT NULL,
    installation_date date NOT NULL,
    status character varying(20) NOT NULL,
    customer_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    customer_id uuid NOT NULL,
    bill_id uuid,
    type character varying(30) NOT NULL,
    channel character varying(30) NOT NULL,
    subject character varying(200) NOT NULL,
    message text NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: otp_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.otp_tokens (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    otp_code character varying(10) NOT NULL,
    purpose character varying(30) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    used boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bill_id uuid NOT NULL,
    bill_reference character varying(80) NOT NULL,
    amount_paid numeric(16,3) NOT NULL,
    payment_method character varying(30) NOT NULL,
    payment_date date NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL
);


--
-- Name: tariff_configurations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tariff_configurations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    utility_type character varying(30) NOT NULL,
    tariff_type character varying(30) NOT NULL,
    flat_rate numeric(16,3),
    effective_from date NOT NULL,
    effective_to date,
    version integer NOT NULL,
    active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tariff_tiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tariff_tiers (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tariff_configuration_id uuid NOT NULL,
    lower_bound numeric(16,3) NOT NULL,
    upper_bound numeric(16,3),
    rate numeric(16,3) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    full_name character varying(150) NOT NULL,
    email character varying(150) NOT NULL,
    phone character varying(30) NOT NULL,
    password character varying(255) NOT NULL,
    status character varying(20) NOT NULL,
    role character varying(30) NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    last_login timestamp without time zone,
    must_change_password boolean DEFAULT false NOT NULL
);


--
-- Data for Name: bill_line_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.bill_line_items (id, bill_id, item_type, description, quantity, unit_price, amount, created_at) FROM stdin;
78f918f7-229d-4777-8eec-0d9e72e8417b	ddb075fc-248e-4073-8138-5ff535a2f7c1	CONSUMPTION	Water usage 25 m3	25.000	360.000	9000.000	2026-06-05 12:16:49.523008
d2a191c2-6b09-456f-b35b-6fd2a07e1ca8	ddb075fc-248e-4073-8138-5ff535a2f7c1	FIXED_SERVICE	Fixed service charge	1.000	1000.000	1000.000	2026-06-05 12:16:49.523538
\.


--
-- Data for Name: bills; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.bills (id, bill_reference, customer_id, meter_id, billing_year, billing_month, units_consumed, amount_due, paid_amount, outstanding_balance, due_date, status, created_at, updated_at, approval_status) FROM stdin;
ddb075fc-248e-4073-8138-5ff535a2f7c1	BILL-2026-01-RODIN	9c55a06e-f367-43b1-abff-b269edbc8fce	4c9f4369-42f0-4bea-8f47-4445306d7de1	2026	1	25.000	10000.000	10000.000	0.000	2026-02-28	PAID	2026-06-05 12:16:49.521872	2026-06-05 12:33:44.279114	APPROVED
\.


--
-- Data for Name: charge_configurations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.charge_configurations (id, charge_type, utility_type, value_type, charge_value, effective_from, effective_to, version, active, created_at, updated_at) FROM stdin;
a04e30b9-7032-47aa-8ffa-da57fcf75074	VAT	WATER	PERCENTAGE	18.000	2026-07-01	\N	2	t	2026-06-05 12:36:39.860122	2026-06-05 12:36:39.860122
\.


--
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.customers (id, national_id, full_name, email, phone, status, address_province, address_district, address_sector, address_cell, address_village, created_at, updated_at) FROM stdin;
9c55a06e-f367-43b1-abff-b269edbc8fce	1199080012345678	Mahinga Rodin	mahingarodin@gmail.com	0788415318	ACTIVE	Kigali	Gasabo	Remera	Rukiri	Amahoro	2026-06-05 12:09:39.086691	2026-06-05 12:09:39.086691
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	init wasac schema	SQL	V1__init_wasac_schema.sql	-638006182	wasac_user	2026-06-05 09:33:51.810035	47	t
2	2	database routines	SQL	V2__database_routines.sql	-1136785974	wasac_user	2026-06-05 09:33:51.895184	33	t
3	3	approval workflow	SQL	V3__approval_workflow.sql	359623794	wasac_user	2026-06-05 11:49:04.693284	97	t
\.


--
-- Data for Name: meter_readings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.meter_readings (id, meter_id, previous_reading, current_reading, reading_date, created_at, recorded_by_user_id) FROM stdin;
e533d407-91ee-420e-ad4f-cc706173276d	4c9f4369-42f0-4bea-8f47-4445306d7de1	0.000	25.000	2026-01-01	2026-06-05 12:12:16.302945	9280d4c1-2357-40fd-b2b8-1c341490a6f8
\.


--
-- Data for Name: meters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.meters (id, meter_number, type, installation_date, status, customer_id, created_at, updated_at) FROM stdin;
4c9f4369-42f0-4bea-8f47-4445306d7de1	WTR-0001	WATER	2026-01-01	ACTIVE	9c55a06e-f367-43b1-abff-b269edbc8fce	2026-06-05 12:11:17.13086	2026-06-05 12:11:17.13086
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notifications (id, customer_id, bill_id, type, channel, subject, message, status, created_at) FROM stdin;
1b2e2082-78f4-407e-93f5-40df46862b19	9c55a06e-f367-43b1-abff-b269edbc8fce	ddb075fc-248e-4073-8138-5ff535a2f7c1	BILL_GENERATED	EMAIL	WASAC Bill Generated - BILL-2026-01-RODIN	Dear Mahinga Rodin, your bill BILL-2026-01-RODIN of amount 10000.000 is generated and due on 2026-02-28.	PENDING	2026-06-05 12:16:49.511893
1684527a-0b4f-4c75-a184-10101a2ae9c8	9c55a06e-f367-43b1-abff-b269edbc8fce	ddb075fc-248e-4073-8138-5ff535a2f7c1	BILL_GENERATED	EMAIL	WASAC Bill Generated - BILL-2026-01-RODIN	Dear Mahinga Rodin, your bill BILL-2026-01-RODIN of 10000.000 FRW for 1/2026 has been generated and is due on 2026-02-28. Your bill PDF is attached.	PENDING	2026-06-05 12:23:02.956371
efc5b1dc-16b1-4ef5-b060-f76dd6566c7f	9c55a06e-f367-43b1-abff-b269edbc8fce	ddb075fc-248e-4073-8138-5ff535a2f7c1	PARTIAL_PAYMENT	EMAIL	Payment Received	Dear Mahinga Rodin, we received your payment of 4000.000 FRW for bill BILL-2026-01-RODIN. Remaining balance: 6000.000 FRW.	PENDING	2026-06-05 12:29:18.755626
94a261b6-bc39-4c77-a0d5-d2886aa7f72d	9c55a06e-f367-43b1-abff-b269edbc8fce	ddb075fc-248e-4073-8138-5ff535a2f7c1	PAYMENT_COMPLETED	EMAIL	Payment Received - BILL-2026-01-RODIN	Dear Mahinga Rodin,\nYour 1/2026 utility bill of 10000.000 FRW has been successfully processed.	PENDING	2026-06-05 12:33:44.288768
4a5707dc-828d-4e3b-93ad-daf8fcb59786	9c55a06e-f367-43b1-abff-b269edbc8fce	\N	CHARGE_UPDATE	EMAIL	Charge Configuration Updated	An updated VAT charge (version 2, effective from 2026-07-01) has been configured.	PENDING	2026-06-05 12:36:39.869098
\.


--
-- Data for Name: otp_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.otp_tokens (id, user_id, otp_code, purpose, expires_at, used, created_at) FROM stdin;
f7efc1b7-435f-4934-9799-9dc8f5782b03	dd5710e5-4e07-472b-aa12-b14e1915e431	326774	EMAIL_VERIFICATION	2026-06-05 09:48:09.074122	t	2026-06-05 09:38:09.074122
cb99646f-2c5f-4d40-b079-c28d522f283f	dd5710e5-4e07-472b-aa12-b14e1915e431	889619	PASSWORD_RESET	2026-06-05 10:42:28.396248	t	2026-06-05 10:32:28.412713
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.payments (id, bill_id, bill_reference, amount_paid, payment_method, payment_date, created_at, status) FROM stdin;
5c542ed8-2e42-4bd2-b919-5681c49006dc	ddb075fc-248e-4073-8138-5ff535a2f7c1	BILL-2026-01-RODIN	4000.000	MOBILE_MONEY	2026-02-10	2026-06-05 12:25:46.165948	APPROVED
c6564ecf-0695-433e-b5c9-3e8725258584	ddb075fc-248e-4073-8138-5ff535a2f7c1	BILL-2026-01-RODIN	6000.000	MOBILE_MONEY	2026-02-20	2026-06-05 12:31:42.88061	APPROVED
\.


--
-- Data for Name: tariff_configurations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tariff_configurations (id, utility_type, tariff_type, flat_rate, effective_from, effective_to, version, active, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: tariff_tiers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tariff_tiers (id, tariff_configuration_id, lower_bound, upper_bound, rate, created_at) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, full_name, email, phone, password, status, role, email_verified, created_at, updated_at, last_login, must_change_password) FROM stdin;
9280d4c1-2357-40fd-b2b8-1c341490a6f8	Olga Muhorakeye	muhorakeyeolga35@gmail.com	0788111222	$2a$10$wv0J/yPwU3rPk5aVtVVXzu.F8GSXbLgJ.tIoodNevwFE0DJ6yR6ce	ACTIVE	ROLE_OPERATOR	t	2026-06-05 11:55:25.370082	2026-06-05 12:08:44.694484	2026-06-05 12:08:44.772203	f
dd5710e5-4e07-472b-aa12-b14e1915e431	Mahinga Rodin	mahingarodin@gmail.com	+250794415318	$2a$10$2odb6wk4bx1R8PTqtEH6lujgEjDBzqnHqt0f3oPQyboA9O9kY7uKy	ACTIVE	ROLE_CUSTOMER	t	2026-06-05 09:38:09.074122	2026-06-05 12:31:02.964774	2026-06-05 12:31:03.041869	f
bb907996-b46e-4fb5-b842-3f7e439c5f5d	Elvis Mwiza	mwizaelvis@gmail.com	0788333444	$2a$10$VXttDPM6jMYLAjhwYtJ6..qrvp1skNnZIRBa.oeW1uvwxxx2Qnn9C	ACTIVE	ROLE_FINANCE	t	2026-06-05 12:05:57.72126	2026-06-05 12:32:57.193612	2026-06-05 12:32:57.286796	f
457838a5-e787-4968-8c5b-237d25d73989	System Administrator	agressive.one04@gmail.com	+250794415318	$2a$10$3vID5BHRiqnK7DMcvsmbq.EuS9bVJ.w33O8PCihdEa2R0hh3wNKQy	ACTIVE	ROLE_ADMIN	t	2026-06-05 09:33:54.449679	2026-06-05 12:36:22.854091	2026-06-05 12:36:22.930861	f
\.


--
-- Name: bill_line_items bill_line_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bill_line_items
    ADD CONSTRAINT bill_line_items_pkey PRIMARY KEY (id);


--
-- Name: bills bills_bill_reference_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT bills_bill_reference_key UNIQUE (bill_reference);


--
-- Name: bills bills_meter_id_billing_year_billing_month_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT bills_meter_id_billing_year_billing_month_key UNIQUE (meter_id, billing_year, billing_month);


--
-- Name: bills bills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT bills_pkey PRIMARY KEY (id);


--
-- Name: charge_configurations charge_configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.charge_configurations
    ADD CONSTRAINT charge_configurations_pkey PRIMARY KEY (id);


--
-- Name: customers customers_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_email_key UNIQUE (email);


--
-- Name: customers customers_national_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_national_id_key UNIQUE (national_id);


--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: meter_readings meter_readings_meter_id_reading_date_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT meter_readings_meter_id_reading_date_key UNIQUE (meter_id, reading_date);


--
-- Name: meter_readings meter_readings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT meter_readings_pkey PRIMARY KEY (id);


--
-- Name: meters meters_meter_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meters
    ADD CONSTRAINT meters_meter_number_key UNIQUE (meter_number);


--
-- Name: meters meters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meters
    ADD CONSTRAINT meters_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: otp_tokens otp_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.otp_tokens
    ADD CONSTRAINT otp_tokens_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: tariff_configurations tariff_configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tariff_configurations
    ADD CONSTRAINT tariff_configurations_pkey PRIMARY KEY (id);


--
-- Name: tariff_tiers tariff_tiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tariff_tiers
    ADD CONSTRAINT tariff_tiers_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: bills trg_bill_insert_notification; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_bill_insert_notification AFTER INSERT ON public.bills FOR EACH ROW EXECUTE FUNCTION public.insert_bill_notification();


--
-- Name: bills trg_bills_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_bills_updated_at BEFORE UPDATE ON public.bills FOR EACH ROW EXECUTE FUNCTION public.set_updated_at_timestamp();


--
-- Name: charge_configurations trg_charge_configurations_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_charge_configurations_updated_at BEFORE UPDATE ON public.charge_configurations FOR EACH ROW EXECUTE FUNCTION public.set_updated_at_timestamp();


--
-- Name: customers trg_customers_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_customers_updated_at BEFORE UPDATE ON public.customers FOR EACH ROW EXECUTE FUNCTION public.set_updated_at_timestamp();


--
-- Name: meters trg_meters_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_meters_updated_at BEFORE UPDATE ON public.meters FOR EACH ROW EXECUTE FUNCTION public.set_updated_at_timestamp();


--
-- Name: tariff_configurations trg_tariff_configurations_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_tariff_configurations_updated_at BEFORE UPDATE ON public.tariff_configurations FOR EACH ROW EXECUTE FUNCTION public.set_updated_at_timestamp();


--
-- Name: users trg_users_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.set_updated_at_timestamp();


--
-- Name: bill_line_items bill_line_items_bill_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bill_line_items
    ADD CONSTRAINT bill_line_items_bill_id_fkey FOREIGN KEY (bill_id) REFERENCES public.bills(id) ON DELETE CASCADE;


--
-- Name: bills bills_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT bills_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: bills bills_meter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT bills_meter_id_fkey FOREIGN KEY (meter_id) REFERENCES public.meters(id);


--
-- Name: meter_readings meter_readings_meter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT meter_readings_meter_id_fkey FOREIGN KEY (meter_id) REFERENCES public.meters(id);


--
-- Name: meter_readings meter_readings_recorded_by_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT meter_readings_recorded_by_user_id_fkey FOREIGN KEY (recorded_by_user_id) REFERENCES public.users(id);


--
-- Name: meters meters_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meters
    ADD CONSTRAINT meters_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: notifications notifications_bill_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_bill_id_fkey FOREIGN KEY (bill_id) REFERENCES public.bills(id);


--
-- Name: notifications notifications_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: otp_tokens otp_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.otp_tokens
    ADD CONSTRAINT otp_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: payments payments_bill_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_bill_id_fkey FOREIGN KEY (bill_id) REFERENCES public.bills(id);


--
-- Name: tariff_tiers tariff_tiers_tariff_configuration_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tariff_tiers
    ADD CONSTRAINT tariff_tiers_tariff_configuration_id_fkey FOREIGN KEY (tariff_configuration_id) REFERENCES public.tariff_configurations(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict XxscBcrZ0fJDDir3klmd3eP8rTnKwJ3I2IdltUZWidaDmnDlZ5bdQ3Ekd8E0i6s

