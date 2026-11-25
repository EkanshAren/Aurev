package com.gstcopilot.backend.service;

import com.gstcopilot.backend.dto.ReconciliationItemDto;
import com.gstcopilot.backend.dto.ReconciliationResponseDto;
import com.gstcopilot.backend.entity.DraftReply;
import com.gstcopilot.backend.entity.GstNotice;
import com.gstcopilot.backend.repository.DraftReplyRepository;
import com.gstcopilot.backend.repository.GstNoticeRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates portal-ready Word (.docx) reply with itemized Annexure-A reconciliation table
 * using Apache POI 6.0.0.
 */
@Service
public class DocxExportService {

    private final GstNoticeRepository noticeRepository;
    private final DraftReplyRepository draftReplyRepository;
    private final ReconciliationService reconciliationService;

    public DocxExportService(
            GstNoticeRepository noticeRepository,
            DraftReplyRepository draftReplyRepository,
            ReconciliationService reconciliationService) {
        this.noticeRepository = noticeRepository;
        this.draftReplyRepository = draftReplyRepository;
        this.reconciliationService = reconciliationService;
    }

    @Transactional(readOnly = true)
    public byte[] exportReplyDocx(Long noticeId) {
        GstNotice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("GST Notice not found for id=" + noticeId));

        DraftReply latestDraft = draftReplyRepository.findFirstByNoticeIdOrderByCreatedAtDesc(noticeId)
                .orElseThrow(() -> new IllegalStateException("No drafted reply found for notice id=" + noticeId));

        ReconciliationResponseDto recon = reconciliationService.reconcile(noticeId);

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(13);
            titleRun.setText("WRITTEN SUBMISSION IN RESPONSE TO FORM GST " + notice.getFormType());

            XWPFParagraph meta = document.createParagraph();
            XWPFRun metaRun = meta.createRun();
            metaRun.setFontSize(10);
            metaRun.setText("DIN: " + notice.getDin() + "  |  GSTIN: " + notice.getTaxpayerGstin()
                    + "  |  Period: " + notice.getTaxPeriod());

            for (String line : latestDraft.getDraftText().split("\\R")) {
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();
                r.setFontSize(11);
                r.setText(line);
            }

            XWPFParagraph annexHeading = document.createParagraph();
            annexHeading.setPageBreak(true);
            XWPFRun annexRun = annexHeading.createRun();
            annexRun.setBold(true);
            annexRun.setFontSize(12);
            annexRun.setText("ANNEXURE-A: ITEMIZED GSTR-2B VS PURCHASE REGISTER RECONCILIATION");

            XWPFTable table = document.createTable(1, 7);
            String[] headers = {"Supplier GSTIN", "Invoice No.", "Date", "GSTR-2B ITC", "Books ITC", "Variance", "Status"};
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.getCell(i).setText(headers[i]);
            }

            for (ReconciliationItemDto item : recon.items()) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(item.supplierGstin());
                row.getCell(1).setText(item.invoiceNumber());
                row.getCell(2).setText(item.invoiceDate() != null ? item.invoiceDate() : "");
                row.getCell(3).setText(item.gstr2bItc().toPlainString());
                row.getCell(4).setText(item.booksItc().toPlainString());
                row.getCell(5).setText(item.differenceItc().toPlainString());
                row.getCell(6).setText(item.status());
            }

            document.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to build .docx export: " + ex.getMessage(), ex);
        }
    }
}
