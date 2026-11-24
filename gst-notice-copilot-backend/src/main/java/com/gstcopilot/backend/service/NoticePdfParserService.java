package com.gstcopilot.backend.service;

import com.gstcopilot.backend.dto.ParsedNoticeDto;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

/**
 * File 5: Apache PDFBox 3.0.8 notice extractor and Indian GSTIN / DIN regex parser.
 * Strictly uses org.apache.pdfbox.Loader.loadPDF(byte[]).
 */
@Service
public class NoticePdfParserService {

    // Standard 15-character Indian GSTIN regex: 2-digit state code + 10-char PAN + 1 entity + Z + checksum
    private static final Pattern GSTIN_PATTERN =
            Pattern.compile("\\b([0-3][0-9][A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z])\\b");

    // CBIC / State GST Document Identification Number (DIN / RFN), typically 16-20 alphanumeric/numeric chars
    private static final Pattern DIN_LABELED_PATTERN =
            Pattern.compile("(?i)(?:DIN|Document\\s+Identification\\s+Number|RFN)\\s*[:#\\-]?\\s*([A-Z0-9]{15,22})");

    private static final Pattern DIN_NUMERIC_FALLBACK_PATTERN =
            Pattern.compile("\\b(20\\d{14,18})\\b");

    private static final Pattern FORM_TYPE_PATTERN =
            Pattern.compile("(?i)\\b(ASMT[-\\s]?10|DRC[-\\s]?01A?|REG[-\\s]?17)\\b");

    private static final Pattern TAX_PERIOD_PATTERN =
            Pattern.compile("(?i)(?:Tax\\s*Period|Financial\\s*Year|F\\.?Y\\.?|Period)\\s*[:\\-]?\\s*([A-Za-z0-9\\s/\\-]{4,32})");

    private static final Pattern DUE_DATE_PATTERN =
            Pattern.compile("(?i)(?:Due\\s*Date|Reply\\s*(?:by|on\\s*or\\s*before)|Date\\s*of\\s*Compliance)\\s*[:\\-]?\\s*(\\d{2}[-/.]\\d{2}[-/.]\\d{4})");

    private static final Pattern TAXPAYER_NAME_PATTERN =
            Pattern.compile("(?i)(?:Legal\\s*Name|Trade\\s*Name|Name\\s*of\\s*(?:Taxpayer|Registered\\s*Person)|M/s\\.?\\s*)\\s*[:\\-]?\\s*([A-Za-z0-9&.,()\\-\\s]{3,90})");

    private static final Pattern DEMANDED_ITC_PATTERN =
            Pattern.compile("(?i)(?:Demanded\\s*ITC|Excess\\s*ITC|Disputed\\s*ITC|Total\\s*Tax\\s*Demand|Discrepancy\\s*Amount|ITC\\s*Mismatch)\\s*[:\\-]?\\s*(?:Rs\\.?|INR|₹)?\\s*([0-9,]+(?:\\.\\d{1,2})?)");

    public ParsedNoticeDto parseNoticePdf(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("Uploaded noticePdf file is empty.");
        }

        String extractedText = extractTextWithPdfBox3(pdfBytes);
        return parseExtractedText(extractedText);
    }

    private String extractTextWithPdfBox3(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to parse notice PDF using Apache PDFBox 3.0.8: " + ex.getMessage(), ex);
        }
    }

    public ParsedNoticeDto parseExtractedText(String rawText) {
        String normalized = rawText == null ? "" : rawText.replace("\r\n", "\n");

        String din = extractDin(normalized);
        String gstin = extractGstin(normalized);
        String formType = extractFormType(normalized);
        String taxPeriod = extractFirstGroup(TAX_PERIOD_PATTERN, normalized, "FY 2024-25").trim();
        String dueDate = extractFirstGroup(DUE_DATE_PATTERN, normalized, "Within 30 days");
        String taxpayerName = extractTaxpayerName(normalized);
        BigDecimal demandedItc = extractDemandedItc(normalized);

        return new ParsedNoticeDto(
                din,
                gstin,
                taxpayerName,
                formType,
                cleanSingleLine(taxPeriod),
                dueDate,
                demandedItc,
                normalized
        );
    }

    private String extractDin(String text) {
        Matcher labeled = DIN_LABELED_PATTERN.matcher(text);
        if (labeled.find()) {
            return labeled.group(1).toUpperCase(Locale.ROOT);
        }
        Matcher fallback = DIN_NUMERIC_FALLBACK_PATTERN.matcher(text);
        if (fallback.find()) {
            return fallback.group(1);
        }
        return "DIN-UNSPECIFIED";
    }

    private String extractGstin(String text) {
        Matcher matcher = GSTIN_PATTERN.matcher(text.toUpperCase(Locale.ROOT));
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "00AAAAA0000A1Z5";
    }

    private String extractFormType(String text) {
        Matcher matcher = FORM_TYPE_PATTERN.matcher(text);
        if (matcher.find()) {
            String raw = matcher.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", "-");
            if (raw.startsWith("ASMT")) {
                return "ASMT-10";
            }
            if (raw.startsWith("DRC")) {
                return "DRC-01";
            }
            if (raw.startsWith("REG")) {
                return "REG-17";
            }
        }
        if (text.toUpperCase(Locale.ROOT).contains("74A")) {
            return "DRC-01";
        }
        return "ASMT-10";
    }

    private String extractTaxpayerName(String text) {
        Matcher matcher = TAXPAYER_NAME_PATTERN.matcher(text);
        if (matcher.find()) {
            String candidate = cleanSingleLine(matcher.group(1));
            if (!candidate.isBlank()) {
                return candidate;
            }
        }
        return "Registered Taxpayer";
    }

    private BigDecimal extractDemandedItc(String text) {
        Matcher matcher = DEMANDED_ITC_PATTERN.matcher(text);
        if (matcher.find()) {
            String numeric = matcher.group(1).replace(",", "").trim();
            try {
                return new BigDecimal(numeric).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String extractFirstGroup(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return fallback;
    }

    private String cleanSingleLine(String value) {
        if (value == null) {
            return "";
        }
        int newlineIdx = value.indexOf('\n');
        String single = newlineIdx >= 0 ? value.substring(0, newlineIdx) : value;
        return single.replaceAll("\\s+", " ").trim();
    }
}
