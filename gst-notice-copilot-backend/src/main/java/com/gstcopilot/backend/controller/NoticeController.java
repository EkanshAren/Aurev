package com.gstcopilot.backend.controller;

import com.gstcopilot.backend.dto.DraftReplyResponseDto;
import com.gstcopilot.backend.dto.NoticeUploadResponseDto;
import com.gstcopilot.backend.dto.ParsedNoticeDto;
import com.gstcopilot.backend.dto.ReconciliationResponseDto;
import com.gstcopilot.backend.entity.GstNotice;
import com.gstcopilot.backend.repository.GstNoticeRepository;
import com.gstcopilot.backend.service.DocxExportService;
import com.gstcopilot.backend.service.GstrJsonParserService;
import com.gstcopilot.backend.service.NoticePdfParserService;
import com.gstcopilot.backend.service.RagDraftingService;
import com.gstcopilot.backend.service.ReconciliationService;
import java.io.IOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * File 8: Core REST Controller exposing locked endpoints:
 * - POST /api/notices/upload
 * - GET  /api/notices/{id}/reconcile
 * - POST /api/notices/{id}/draft
 * - GET  /api/notices/{id}/export-docx
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticePdfParserService pdfParserService;
    private final GstrJsonParserService gstrJsonParserService;
    private final GstNoticeRepository noticeRepository;
    private final ReconciliationService reconciliationService;
    private final RagDraftingService ragDraftingService;
    private final DocxExportService docxExportService;

    public NoticeController(
            NoticePdfParserService pdfParserService,
            GstrJsonParserService gstrJsonParserService,
            GstNoticeRepository noticeRepository,
            ReconciliationService reconciliationService,
            RagDraftingService ragDraftingService,
            DocxExportService docxExportService) {
        this.pdfParserService = pdfParserService;
        this.gstrJsonParserService = gstrJsonParserService;
        this.noticeRepository = noticeRepository;
        this.reconciliationService = reconciliationService;
        this.ragDraftingService = ragDraftingService;
        this.docxExportService = docxExportService;
    }

    /**
     * Locked Endpoint 1: POST /api/notices/upload
     * Accepts multipart/form-data parts: noticePdf, gstr2bJson, purchaseJson.
     * Returns {"noticeId": 1, "din": "202608...", "gstin": "29AAB...", "demandedItc": 145000.00}.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<NoticeUploadResponseDto> uploadNotice(
            @RequestPart("noticePdf") MultipartFile noticePdf,
            @RequestPart(value = "gstr2bJson", required = false) MultipartFile gstr2bJson,
            @RequestPart(value = "purchaseJson", required = false) MultipartFile purchaseJson) throws IOException {

        ParsedNoticeDto parsed = pdfParserService.parseNoticePdf(noticePdf.getBytes());

        GstNotice notice = new GstNotice();
        notice.setDin(parsed.din());
        notice.setTaxpayerGstin(parsed.gstin());
        notice.setTaxpayerName(parsed.taxpayerName());
        notice.setFormType(parsed.formType());
        notice.setTaxPeriod(parsed.taxPeriod());
        notice.setDueDate(parsed.dueDate());
        notice.setDemandedItc(parsed.demandedItc());
        notice.setRawNoticeText(parsed.rawText());
        notice.setStatus("UPLOADED");

        GstNotice saved = noticeRepository.save(notice);

        if (gstr2bJson != null && !gstr2bJson.isEmpty()) {
            gstrJsonParserService.parseAndSaveGstr2b(saved, gstr2bJson.getBytes());
        }
        if (purchaseJson != null && !purchaseJson.isEmpty()) {
            gstrJsonParserService.parseAndSavePurchaseRegister(saved, purchaseJson.getBytes());
        }

        return ResponseEntity.ok(new NoticeUploadResponseDto(
                saved.getId(),
                saved.getDin(),
                saved.getTaxpayerGstin(),
                saved.getDemandedItc()
        ));
    }

    /**
     * Locked Endpoint 2: GET /api/notices/{id}/reconcile
     * Runs MySQL SQL-First reconciliation and returns ReconciliationResponseDto.
     */
    @GetMapping("/{id}/reconcile")
    public ResponseEntity<ReconciliationResponseDto> reconcileNotice(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reconciliationService.reconcile(id));
    }

    /**
     * Locked Endpoint 3: POST /api/notices/{id}/draft
     * Masks PII locally, retrieves top-4 GST laws from MySQL, drafts via Spring AI,
     * unmasks locally, and verifies all [CITATION_ID] tags.
     */
    @PostMapping("/{id}/draft")
    public ResponseEntity<DraftReplyResponseDto> draftReply(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ragDraftingService.generateGroundedDraft(id));
    }

    /**
     * Export portal-ready Word (.docx) reply with Annexure-A reconciliation table.
     */
    @GetMapping("/{id}/export-docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable("id") Long id) {
        byte[] docxBytes = docxExportService.exportReplyDocx(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("GST_Notice_Reply_" + id + ".docx")
                .build());
        return ResponseEntity.ok().headers(headers).body(docxBytes);
    }
}
