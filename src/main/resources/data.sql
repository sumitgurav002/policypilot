-- Sample insurance data used by the agent's tools. Safe to run on every startup.
INSERT INTO policies (policy_number, holder_name, holder_email, holder_phone, policy_type, sum_insured, premium_amount, premium_due_date, status) VALUES
 ('POL-1001', 'Asha Kulkarni',  'asha.kulkarni@example.com',  '9876543210', 'HOME',   5000000.00, 12500.00, DATE '2026-11-15', 'ACTIVE'),
 ('POL-1002', 'Rahul Deshmukh', 'rahul.deshmukh@example.com', '9823456781', 'MOTOR',   800000.00,  9800.00, DATE '2026-10-20', 'ACTIVE'),
 ('POL-1003', 'Neha Joshi',     'neha.joshi@example.com',     '9765432109', 'HEALTH', 1000000.00, 18400.00, DATE '2026-12-01', 'ACTIVE'),
 ('POL-1004', 'Vikram Patil',   'vikram.patil@example.com',   '9988776655', 'HOME',   3500000.00,  8900.00, DATE '2026-09-30', 'LAPSED'),
 ('POL-1005', 'Priya Shinde',   'priya.shinde@example.com',   '9123456780', 'MOTOR',   650000.00,  7600.00, DATE '2027-01-10', 'ACTIVE')
ON CONFLICT (policy_number) DO NOTHING;

INSERT INTO claims (claim_id, policy_number, claim_type, amount, status, filed_on, last_update) VALUES
 ('CLM-1021', 'POL-1001', 'WATER_DAMAGE',  85000.00, 'APPROVED',     DATE '2026-08-02', 'Approved; payment released on 2026-08-20'),
 ('CLM-1022', 'POL-1002', 'ACCIDENT',     120000.00, 'UNDER_REVIEW', DATE '2026-09-14', 'Surveyor visit completed; awaiting garage estimate'),
 ('CLM-1023', 'POL-1001', 'THEFT',         45000.00, 'DOCUMENTS_PENDING', DATE '2026-09-28', 'Police FIR copy not yet received'),
 ('CLM-1024', 'POL-1003', 'HOSPITALISATION', 210000.00, 'SETTLED',   DATE '2026-07-11', 'Cashless claim settled with hospital'),
 ('CLM-1025', 'POL-1005', 'ACCIDENT',      30000.00, 'REJECTED',     DATE '2026-09-01', 'Driver did not hold a valid licence at time of accident')
ON CONFLICT (claim_id) DO NOTHING;
