package com.gstcopilot.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gstcopilot.backend.dto.DraftReplyResponseDto;
import com.gstcopilot.backend.dto.DraftReplyResponseDto.VerifiedCitation;
import com.gstcopilot.backend.dto.ReconciliationItemDto;
import com.gstcopilot.backend.dto.ReconciliationResponseDto;
import com.gstcopilot.backend.entity.DraftReply;
import com.gstcopilot.backend.entity.GstNotice;
import com.gstcopilot.backend.entity.LegalProvision;
import com.gstcopilot.backend.repository.DraftReplyRepository;
import com.gstcopilot.backend.repository.GstNoticeRepository;
import com.gstcopilot.backend.service.PiiMaskingService.MaskingSession;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * File 7: Privacy-First RAG Legal Reply Drafter (MySQL 8 + Spring AI ChatClient).
 * Pipeline:
 * 1. Load notice + deterministic SQL reconciliation summary from MySQL.
 * 2. Open request-scoped PiiMaskingService.MaskingSession to mask GSTINs, PANs, and entity names locally.
 * 3. Retrieve top-4 statutory provisions from MySqlLegalVectorStoreService.
 * 4. Prompt Spring AI ChatClient strictly using masked payload and requiring [CITATION_ID] tags.
 * 5. Unmask real identifiers locally in Java and verify every citation against MySQL.
 * 6. Persist DraftReply to MySQL `draft_replies`.
 */
@Service
public class RagDraftingService {

    private static final String SYSTEM_PROMPT = """
            You are a Senior Indian Chartered Accountant and Indirect Tax Counsel drafting a formal,
            portal-ready written submission in response to a GST notice (Form GST ASMT-10, DRC-01 / Section 74A, or REG-17).
            STRICT RULES:
            1. Preserve all privacy placeholders exactly as provided ([TAXPAYER_GSTIN], [VENDOR_GSTIN_1], [CLIENT_ENTITY], [PAN_MASKED]). Never alter or invent real GSTINs or names.
            2. Ground every legal argument ONLY in the retrieved statutory provisions supplied below, and cite them using exact square-bracket tags such as [CGST_SEC_16_2_AA] or [CBIC_CIR_183_2022].
            3. Do NOT fabricate section numbers, circulars, or case laws outside the supplied legal context.
            4. Structure the reply formally with: Subject & Reference (DIN, Form Type, Period), Preliminary Submissions, Reconciliation Summary (citing exact SQL figures provided), Ground-wise Legal Defense with [CITATION_ID] tags, and Prayer for dropping proceedings (ASMT-12 / REG-20 / dropping DRC-01).
            """;

    private final GstNoticeRepository noticeRepository;
    private final DraftReplyRepository draftReplyRepository;
    private final ReconciliationService reconciliationService;
    private final PiiMaskingService piiMaskingService;
    private final MySqlLegalVectorStoreService legalVectorStoreService;
    private final CitationVerificationService citationVerificationService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public RagDraftingService(
            GstNoticeRepository noticeRepository,
            DraftReplyRepository draftReplyRepository,
            ReconciliationService reconciliationService,
            PiiMaskingService piiMaskingService,
            MySqlLegalVectorStoreService legalVectorStoreService,
            CitationVerificationService citationVerificationService,
            ChatClient chatClient,
            ObjectMapper objectMapper) {
        this.noticeRepository = noticeRepository;
        this.draftReplyRepository = draftReplyRepository;
        this.reconciliationService = reconciliationService;
        this.piiMaskingService = piiMaskingService;
        this.legalVectorStoreService = legalVectorStoreService;
        this.citationVerificationService = citationVerificationService;
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DraftReplyResponseDto generateGroundedDraft(Long noticeId) {
        GstNotice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("GST Notice not found for id=" + noticeId));

        ReconciliationResponseDto reconciliation = reconciliationService.reconcile(noticeId);

        // Step 1: Request-scoped local PII masking before any external/LLM prompt assembly
        MaskingSession maskingSession = piiMaskingService.openSession(
                notice.getTaxpayerGstin(),
                notice.getTaxpayerName()
        );

        String rawContextBlock = buildRawCaseContext(notice, reconciliation);
        String maskedCaseContext = maskingSession.mask(rawContextBlock);

        // Step 2: Retrieve top-4 grounded laws from MySQL legal vector/full-text store
        String searchQuery = notice.getFormType() + " ITC mismatch GSTR-2B purchase register "
                + (reconciliation.missingIn2bCount() > 0 ? "supplier not filed GSTR-1 Circular 183" : "")
                + (notice.getFormType().equalsIgnoreCase("DRC-01") ? " Section 74A penalty" : "")
                + (notice.getFormType().equalsIgnoreCase("REG-17") ? " cancellation registration Rule 22" : "");

        List<LegalProvision> topLaws = legalVectorStoreService.retrieveTopK(searchQuery, notice.getFormType(), 4);

        String legalCorpusPrompt = topLaws.stream()
                .map(lp -> String.format(
                        "[%s] %s (%s): %s",
                        lp.getCitationId(),
                        lp.getTitle(),
                        lp.getSectionRef(),
                        lp.getFullText()
                ))
                .collect(Collectors.joining("\n\n"));

        String userPrompt = """
                --- SANITIZED CASE & RECONCILIATION CONTEXT ---
                %s

                --- RETRIEVED AUTHORITATIVE GST LEGAL PROVISIONS (TOP 4) ---
                %s

                Draft the complete formal GST reply now, citing applicable [CITATION_ID] tags from the list above.
                """.formatted(maskedCaseContext, legalCorpusPrompt);

        // Step 3: Call Spring AI ChatClient (Groq OpenAI-compatible or local Ollama in offline profile)
        String maskedDraft = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        // Step 4: Restore real GSTINs, PANs, and taxpayer names locally inside JVM
        String restoredDraft = maskingSession.unmask(maskedDraft);

        // Step 5: Verify every [CITATION_ID] against MySQL legal_provisions
        List<VerifiedCitation> verifiedCitations = citationVerificationService.verifyCitations(restoredDraft);

        // Step 6: Persist generated draft in MySQL
        DraftReply entity = new DraftReply();
        entity.setNotice(notice);
        entity.setDraftText(restoredDraft);
        entity.setVerifiedCitationsJson(toJsonSafe(verifiedCitations));
        entity.setMaskedTokensJson(toJsonSafe(maskingSession.getAuditLog()));
        draftReplyRepository.save(entity);

        notice.setStatus("DRAFTED");
        noticeRepository.save(notice);

        return new DraftReplyResponseDto(
                restoredDraft,
                verifiedCitations,
                maskingSession.getAuditLog()
        );
    }

    private String buildRawCaseContext(GstNotice notice, ReconciliationResponseDto recon) {
        StringBuilder sb = new StringBuilder();
        sb.append("Notice Form Type: ").append(notice.getFormType()).append("\n");
        sb.append("DIN / RFN: ").append(notice.getDin()).append("\n");
        sb.append("Taxpayer Legal Name: ").append(notice.getTaxpayerName()).append("\n");
        sb.append("Taxpayer GSTIN: ").append(notice.getTaxpayerGstin()).append("\n");
        sb.append("Tax Period: ").append(notice.getTaxPeriod()).append("\n");
        sb.append("Reply Due Date: ").append(notice.getDueDate()).append("\n");
        sb.append("Demanded ITC in Notice: INR ").append(notice.getDemandedItc()).append("\n\n");

        sb.append("SQL-First Reconciliation Totals (MySQL):\n");
        sb.append("- Total ITC per GSTR-2B: INR ").append(recon.totalGstr2bItc()).append("\n");
        sb.append("- Total ITC per Purchase Books: INR ").append(recon.totalBooksItc()).append("\n");
        sb.append("- Net Variance (Books - 2B): INR ").append(recon.netDifferenceItc()).append("\n");
        sb.append("- Counts: Matched=").append(recon.matchedCount())
                .append(", Missing in 2B=").append(recon.missingIn2bCount())
                .append(", Missing in Books=").append(recon.missingInBooksCount())
                .append(", Amount Mismatch=").append(recon.amountMismatchCount()).append("\n\n");

        sb.append("Discrepant Invoices (Annexure-A):\n");
        for (ReconciliationItemDto item : recon.items()) {
            if (!"MATCHED".equals(item.status())) {
                sb.append(String.format(
                        "- Vendor %s | Inv %s (%s) | 2B ITC: %s | Books ITC: %s | Diff: %s | Status: %s%n",
                        item.supplierGstin(),
                        item.invoiceNumber(),
                        item.invoiceDate(),
                        item.gstr2bItc(),
                        item.booksItc(),
                        item.differenceItc(),
                        item.status()
                ));
            }
        }

        if (notice.getRawNoticeText() != null && !notice.getRawNoticeText().isBlank()) {
            String excerpt = notice.getRawNoticeText().length() > 1600
                    ? notice.getRawNoticeText().substring(0, 1600)
                    : notice.getRawNoticeText();
            sb.append("\nNotice Text Excerpt:\n").append(excerpt).append("\n");
        }
        return sb.toString();
    }

    private String toJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}
