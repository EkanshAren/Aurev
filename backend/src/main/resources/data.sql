-- ==============================================================================
-- GST Notice Copilot — Authoritative Indian GST Legal Corpus Seed (MySQL 8)
-- Uses INSERT IGNORE for idempotent restarts.
-- ==============================================================================

INSERT IGNORE INTO legal_provisions
    (citation_id, title, section_ref, provision_type, applicable_forms, summary, full_text)
VALUES
(
    'CGST_SEC_16_2_AA',
    'Eligibility for ITC based on GSTR-2B communication',
    'Section 16(2)(aa) of the CGST Act, 2017',
    'STATUTE',
    'ASMT-10,DRC-01',
    'Input tax credit is available where invoice or debit note details furnished by the supplier in GSTR-1/IFF have been communicated to the recipient in Form GSTR-2B.',
    'Section 16(2)(aa) of the Central Goods and Services Tax Act, 2017 (effective 01.01.2022) mandates that details of the invoice or debit note referred to in clause (a) have been furnished by the supplier in the statement of outward supplies (GSTR-1/IFF) and such details have been communicated to the recipient in Form GSTR-2B under Section 37. Where timing differences arise due to quarterly QRMP filing or subsequent period reporting, reconciliation statements establish bona fide compliance.'
),
(
    'CGST_SEC_61_ASMT10',
    'Scrutiny of Returns and Explanation in Form GST ASMT-11',
    'Section 61 of the CGST Act, 2017 read with Rule 99',
    'STATUTE',
    'ASMT-10',
    'Proper officer may scrutinize returns to verify correctness and issue Form GST ASMT-10; if explanation in ASMT-11 is found acceptable, no further action shall be taken.',
    'Under Section 61(1) and 61(2) of the CGST Act, 2017 read with Rule 99 of the CGST Rules, 2017, the proper officer issues notice in Form GST ASMT-10 informing the registered person of discrepancies noticed during return scrutiny. Where the registered person furnishes an acceptable explanation in Form GST ASMT-11 within thirty days, the proper officer shall inform the taxpayer in Form GST ASMT-12 and no further proceedings under Section 73, 74, or 74A shall be initiated.'
),
(
    'CGST_SEC_74A_FA2024',
    'Unified Determination of Tax Not Paid or Excess ITC Availed',
    'Section 74A of the CGST Act, 2017 (Finance (No. 2) Act, 2024)',
    'STATUTE',
    'DRC-01',
    'Unified show-cause mechanism from FY 2024-25 onwards distinguishing non-fraud cases from fraud/wilful misstatement, requiring specific foundational evidence for extended penalty.',
    'Section 74A of the CGST Act, 2017, inserted by the Finance (No. 2) Act, 2024 applicable from Financial Year 2024-25 onwards, consolidates adjudication of tax short paid or ITC wrongly availed. Under Section 74A(5)(i) and (ii), higher penalty for fraud, wilful misstatement, or suppression of facts cannot be invoked mechanically for mere GSTR-2B versus Books mismatches without positive evidence of mens rea.'
),
(
    'CBIC_CIR_183_2022',
    'Procedure for Dealing with GSTR-2A/2B vs GSTR-3B Mismatches',
    'CBIC Circular No. 183/15/2022-GST dated 27.12.2022',
    'CIRCULAR',
    'ASMT-10,DRC-01',
    'Allows bona fide recipients to substantiate ITC claims through supplier certificates or CA/CMA certificates where supplier defaulted in GSTR-1 reporting but discharged tax in GSTR-3B.',
    'CBIC Circular No. 183/15/2022-GST clarifies that for bona fide discrepancies between Form GSTR-3B and auto-populated statements, the proper officer shall verify possession of valid tax invoices, receipt of goods/services, payment of consideration, and supplier tax discharge via CA/CMA certificate (where difference exceeds Rs. 5 lakh) or supplier self-certificate (up to Rs. 5 lakh).'
),
(
    'CBIC_CIR_193_2023',
    'Clarification on ITC Discrepancies for Subsequent Periods',
    'CBIC Circular No. 193/05/2023-GST dated 17.07.2023',
    'CIRCULAR',
    'ASMT-10,DRC-01',
    'Extends verification methodology of Circular 183/15/2022-GST to period 01.04.2019 to 31.12.2021 subject to Rule 36(4) limits and confirms procedural verification before denial.',
    'CBIC Circular No. 193/05/2023-GST reiterates that genuine input tax credit backed by tax invoices, e-way bills, bank payment proofs, and physical receipt of goods/services cannot be summarily disallowed without applying statutory verification procedures and granting reasonable opportunity of hearing.'
),
(
    'HC_CAL_SUNCRAFT_2023',
    'Suncraft Energy Pvt. Ltd. v. Assistant Commissioner of State Tax',
    'Calcutta High Court (2023) — SLP Dismissed by Supreme Court',
    'CASE_LAW',
    'ASMT-10,DRC-01',
    'Demand cannot be raised against a bona fide purchasing dealer solely for supplier GSTR-1 non-reporting without first proceeding against the defaulting supplier.',
    'In Suncraft Energy Pvt. Ltd. v. Assistant Commissioner of State Tax (MAT 1218 of 2023, upheld upon dismissal of Revenue SLP by the Hon''ble Supreme Court in December 2023), the Court held that before reversing ITC in the hands of a bona fide recipient who holds valid tax invoices and paid consideration including GST through banking channels, the Department must first proceed against the defaulting supplier unless collusion or missing dealer status is proved.'
),
(
    'HC_KER_DIYA_2023',
    'Diya Agencies v. State Tax Officer',
    'Kerala High Court (2023) WP(C) No. 29769 of 2023',
    'CASE_LAW',
    'ASMT-10,DRC-01',
    'ITC cannot be denied merely because a transaction does not reflect in GSTR-2A/2B if the taxpayer substantiates genuineness of the purchase and payment of tax.',
    'In M/s Diya Agencies v. State Tax Officer, the Hon''ble Kerala High Court ruled that mere non-reflection of an invoice in GSTR-2A/2B is not a conclusive ground to deny Input Tax Credit. Where the taxpayer furnishes tax invoices, payment proofs, and delivery records, the assessing authority must examine evidence independently.'
),
(
    'CGST_SEC_29_RULE_22_REG17',
    'Show Cause Notice for Cancellation of Registration (Form GST REG-17)',
    'Section 29(2) of the CGST Act, 2017 read with Rule 22',
    'STATUTE',
    'REG-17',
    'Requires specific, reasoned grounds in REG-17 and mandates dropping cancellation proceedings in Form GST REG-20 when returns are regularized or explanation in REG-18 is satisfactory.',
    'Under Section 29(2) of the CGST Act, 2017 read with Rule 22 of the CGST Rules, 2017, cancellation of GST registration is a drastic measure affecting civil and business livelihood rights under Article 19(1)(g). Cryptic show-cause notices in Form GST REG-17 lacking specific factual particulars violate principles of natural justice; upon filing pending returns or furnishing satisfactory reply in Form GST REG-18, proceedings must be dropped via Form GST REG-20.'
);
