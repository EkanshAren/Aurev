CREATE TABLE IF NOT EXISTS ca_firms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    firm_name VARCHAR(150) NOT NULL,
    icai_reg_no VARCHAR(30) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ca_firm_id BIGINT NOT NULL,
    legal_name VARCHAR(200) NOT NULL,
    gstin VARCHAR(15) UNIQUE NOT NULL,
    state_code VARCHAR(2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_clients_firm FOREIGN KEY (ca_firm_id) REFERENCES ca_firms(id)
);

CREATE TABLE IF NOT EXISTS gst_notices (
    id VARCHAR(36) PRIMARY KEY,
    client_id BIGINT NOT NULL,
    notice_type VARCHAR(30) NOT NULL,
    din_number VARCHAR(40) UNIQUE,
    tax_period VARCHAR(20) NOT NULL,
    section_invoked VARCHAR(60) NOT NULL,
    demand_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    due_date DATE,
    raw_extracted_text TEXT,
    masked_text TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'UPLOADED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notices_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS gstr2b_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    supplier_gstin VARCHAR(15) NOT NULL,
    supplier_name VARCHAR(200),
    invoice_number VARCHAR(50) NOT NULL,
    invoice_date DATE NOT NULL,
    taxable_value DECIMAL(12, 2) NOT NULL,
    igst DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cgst DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    sgst DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    itc_available BOOLEAN NOT NULL DEFAULT TRUE,
    filing_status VARCHAR(20) NOT NULL DEFAULT 'FILED',
    UNIQUE KEY uk_2b_invoice (client_id, supplier_gstin, invoice_number),
    CONSTRAINT fk_2b_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS purchase_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    supplier_gstin VARCHAR(15) NOT NULL,
    supplier_name VARCHAR(200),
    invoice_number VARCHAR(50) NOT NULL,
    invoice_date DATE NOT NULL,
    taxable_value DECIMAL(12, 2) NOT NULL,
    igst DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cgst DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    sgst DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    itc_claimed DECIMAL(12, 2) NOT NULL,
    UNIQUE KEY uk_books_invoice (client_id, supplier_gstin, invoice_number),
    CONSTRAINT fk_books_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS legal_citations (
    citation_id VARCHAR(64) PRIMARY KEY,
    act_or_circular VARCHAR(120) NOT NULL,
    section_ref VARCHAR(80) NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    effective_from DATE
);
