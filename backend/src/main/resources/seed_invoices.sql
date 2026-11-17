INSERT IGNORE INTO ca_firms (id, firm_name, icai_reg_no, email)
VALUES (1, 'Mehta & Associates Chartered Accountants', 'ICAI-098412N', 'audit@mehtaca.in');

INSERT IGNORE INTO clients (id, ca_firm_id, legal_name, gstin, state_code)
VALUES (1, 1, 'Apex Precision Components Pvt Ltd', '07AABCA4512F1Z9', '07');

INSERT IGNORE INTO gst_notices (id, client_id, notice_type, din_number, tax_period, section_invoked, demand_amount, due_date, status)
VALUES ('NOTICE-2026-001', 1, 'DRC-01', 'DIN20260700918273', 'FY 2023-24', 'Section 73(1) r/w Section 16(2)', 485000.00, '2026-12-15', 'UPLOADED');

-- Matched Invoices
INSERT IGNORE INTO gstr2b_invoices (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_available, filing_status) VALUES
(1, '07AAHCS8821M1Z2', 'SteelKraft India Ltd', 'SK/23-24/101', '2023-07-12', 200000.00, 0.00, 18000.00, 18000.00, TRUE, 'FILED'),
(1, '27AABCT3518Q1ZV', 'TechVolt Automation Pvt Ltd', 'TV/23-24/412', '2023-08-19', 350000.00, 63000.00, 0.00, 0.00, TRUE, 'FILED'),
(1, '09AAFCN7102K1Z4', 'NorthStar Packaging Corp', 'NS/23-24/088', '2023-09-05', 120000.00, 21600.00, 0.00, 0.00, TRUE, 'FILED');

INSERT IGNORE INTO purchase_ledger (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_claimed) VALUES
(1, '07AAHCS8821M1Z2', 'SteelKraft India Ltd', 'SK/23-24/101', '2023-07-12', 200000.00, 0.00, 18000.00, 18000.00, 36000.00),
(1, '27AABCT3518Q1ZV', 'TechVolt Automation Pvt Ltd', 'TV/23-24/412', '2023-08-19', 350000.00, 63000.00, 0.00, 0.00, 63000.00),
(1, '09AAFCN7102K1Z4', 'NorthStar Packaging Corp', 'NS/23-24/088', '2023-09-05', 120000.00, 21600.00, 0.00, 0.00, 21600.00);

-- Mismatch 1: MISSING_IN_2B (Supplier failed to file GSTR-1)
INSERT IGNORE INTO purchase_ledger (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_claimed) VALUES
(1, '06AADCR9012L1Z8', 'Rapid Logistics Haryana', 'RL/23-24/774', '2023-10-14', 450000.00, 81000.00, 0.00, 0.00, 81000.00),
(1, '07AAGCM5410P1Z1', 'Metro Industrial Tools', 'MIT/23-24/309', '2023-11-22', 250000.00, 0.00, 22500.00, 22500.00, 45000.00);

-- Mismatch 2: VALUE_MISMATCH (Tax claimed in books exceeds GSTR-2B)
INSERT IGNORE INTO gstr2b_invoices (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_available, filing_status) VALUES
(1, '24AABCP6290R1Z5', 'Prime Polymers Gujarat', 'PP/23-24/519', '2023-12-03', 500000.00, 90000.00, 0.00, 0.00, TRUE, 'FILED');

INSERT IGNORE INTO purchase_ledger (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_claimed) VALUES
(1, '24AABCP6290R1Z5', 'Prime Polymers Gujarat', 'PP/23-24/519', '2023-12-03', 650000.00, 117000.00, 0.00, 0.00, 117000.00);

-- Mismatch 3: BLOCKED_OR_INELIGIBLE (ITC marked unavailable in 2B)
INSERT IGNORE INTO gstr2b_invoices (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_available, filing_status) VALUES
(1, '07AAACK9901B1Z7', 'Capital Fleet Motors', 'CFM/23-24/045', '2024-01-18', 300000.00, 0.00, 27000.00, 27000.00, FALSE, 'FILED');

INSERT IGNORE INTO purchase_ledger (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_claimed) VALUES
(1, '07AAACK9901B1Z7', 'Capital Fleet Motors', 'CFM/23-24/045', '2024-01-18', 300000.00, 0.00, 27000.00, 27000.00, 54000.00);

-- Mismatch 4: MISSING_IN_BOOKS (Present in 2B but unrecorded in Purchase Ledger)
INSERT IGNORE INTO gstr2b_invoices (client_id, supplier_gstin, supplier_name, invoice_number, invoice_date, taxable_value, igst, cgst, sgst, itc_available, filing_status) VALUES
(1, '29AABCS1144D1Z3', 'Southern Cloud Systems', 'SCS/23-24/991', '2024-02-11', 180000.00, 32400.00, 0.00, 0.00, TRUE, 'FILED');
