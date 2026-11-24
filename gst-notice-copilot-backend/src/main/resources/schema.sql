-- ==============================================================================
-- GST Notice Copilot — MySQL 8.x 3NF Schema (InnoDB / utf8mb4)
-- Migrated from PostgreSQL + pgvector to MySQL 8.x with JSON embeddings & FULLTEXT
-- ==============================================================================

CREATE TABLE IF NOT EXISTS gst_notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    din VARCHAR(32) NOT NULL,
    taxpayer_gstin VARCHAR(15) NOT NULL,
    taxpayer_name VARCHAR(255),
    form_type VARCHAR(32) NOT NULL COMMENT 'ASMT-10, DRC-01, or REG-17',
    tax_period VARCHAR(64),
    due_date VARCHAR(32),
    demanded_itc DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    raw_notice_text LONGTEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'UPLOADED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notices_din (din),
    INDEX idx_notices_gstin (taxpayer_gstin),
    INDEX idx_notices_form_type (form_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gstr2b_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    supplier_gstin VARCHAR(15) NOT NULL,
    supplier_name VARCHAR(255),
    invoice_number VARCHAR(64) NOT NULL,
    invoice_date VARCHAR(32),
    taxable_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    igst DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    cgst DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    sgst DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_itc DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    itc_availability VARCHAR(16) DEFAULT 'Y',
    CONSTRAINT fk_gstr2b_notice FOREIGN KEY (notice_id)
        REFERENCES gst_notices (id) ON DELETE CASCADE,
    UNIQUE KEY uq_gstr2b_notice_inv (notice_id, supplier_gstin, invoice_number),
    INDEX idx_gstr2b_notice_supplier (notice_id, supplier_gstin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS purchase_register_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    supplier_gstin VARCHAR(15) NOT NULL,
    supplier_name VARCHAR(255),
    invoice_number VARCHAR(64) NOT NULL,
    invoice_date VARCHAR(32),
    taxable_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    igst DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    cgst DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    sgst DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_itc DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_purchase_notice FOREIGN KEY (notice_id)
        REFERENCES gst_notices (id) ON DELETE CASCADE,
    UNIQUE KEY uq_purchase_notice_inv (notice_id, supplier_gstin, invoice_number),
    INDEX idx_purchase_notice_supplier (notice_id, supplier_gstin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS legal_provisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    citation_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    section_ref VARCHAR(128) NOT NULL,
    provision_type VARCHAR(64) NOT NULL COMMENT 'STATUTE, CIRCULAR, CASE_LAW, RULE',
    applicable_forms VARCHAR(128) NOT NULL COMMENT 'Comma-separated: ASMT-10,DRC-01,REG-17',
    summary TEXT NOT NULL,
    full_text LONGTEXT NOT NULL,
    embedding_json JSON NULL COMMENT 'Float array embedding replacing pgvector column',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_legal_citation_id (citation_id),
    FULLTEXT KEY ft_legal_corpus (title, section_ref, summary, full_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS draft_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    draft_text LONGTEXT NOT NULL,
    verified_citations_json JSON NOT NULL,
    masked_tokens_json JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_draft_notice FOREIGN KEY (notice_id)
        REFERENCES gst_notices (id) ON DELETE CASCADE,
    INDEX idx_draft_notice_created (notice_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
