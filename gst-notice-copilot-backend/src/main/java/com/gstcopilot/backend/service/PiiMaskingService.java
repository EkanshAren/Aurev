package com.gstcopilot.backend.service;

import com.gstcopilot.backend.dto.DraftReplyResponseDto.MaskedTokenAudit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * File 6: Local Java PII Masking Interceptor.
 * Creates a request-scoped MaskingSession that anonymizes GSTINs ([TAXPAYER_GSTIN], [VENDOR_GSTIN_1]),
 * PANs ([PAN_MASKED]), and taxpayer entity names ([CLIENT_ENTITY]) strictly inside the JVM
 * before any prompt leaves the server, records an auditLog, and restores real identifiers locally.
 */
@Service
public class PiiMaskingService {

    private static final Pattern GSTIN_PATTERN =
            Pattern.compile("\\b[0-3][0-9][A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]\\b");

    private static final Pattern PAN_PATTERN =
            Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]\\b");

    public MaskingSession openSession(String taxpayerGstin, String taxpayerName) {
        return new MaskingSession(taxpayerGstin, taxpayerName);
    }

    /**
     * Request-scoped, non-shared masking session. Dispose after a single /draft request completes.
     */
    public static final class MaskingSession {

        private final String taxpayerGstin;
        private final String taxpayerName;
        private final Map<String, String> realToPlaceholder = new LinkedHashMap<>();
        private final Map<String, String> placeholderToReal = new LinkedHashMap<>();
        private final List<MaskedTokenAudit> auditLog = new ArrayList<>();
        private int vendorCounter = 0;
        private int panCounter = 0;

        MaskingSession(String taxpayerGstin, String taxpayerName) {
            this.taxpayerGstin = taxpayerGstin != null ? taxpayerGstin.trim().toUpperCase(Locale.ROOT) : "";
            this.taxpayerName = taxpayerName != null ? taxpayerName.trim() : "";

            if (!this.taxpayerGstin.isBlank()) {
                registerMapping(this.taxpayerGstin, "[TAXPAYER_GSTIN]", "TAXPAYER_GSTIN");
            }
            if (!this.taxpayerName.isBlank() && this.taxpayerName.length() >= 3) {
                registerMapping(this.taxpayerName, "[CLIENT_ENTITY]", "CLIENT_ENTITY");
            }
        }

        public String mask(String input) {
            if (input == null || input.isBlank()) {
                return "";
            }

            String sanitized = input;

            // 1. Replace client entity name (case-insensitive) first to prevent partial overlaps
            if (!taxpayerName.isBlank() && taxpayerName.length() >= 3) {
                sanitized = sanitized.replaceAll(
                        "(?i)" + Pattern.quote(taxpayerName),
                        Matcher.quoteReplacement("[CLIENT_ENTITY]")
                );
            }

            // 2. Replace all 15-character GSTINs (taxpayer vs numbered vendor GSTINs)
            Matcher gstinMatcher = GSTIN_PATTERN.matcher(sanitized);
            StringBuilder gstinBuffer = new StringBuilder();
            while (gstinMatcher.find()) {
                String found = gstinMatcher.group().toUpperCase(Locale.ROOT);
                String placeholder = resolveGstinPlaceholder(found);
                gstinMatcher.appendReplacement(gstinBuffer, Matcher.quoteReplacement(placeholder));
            }
            gstinMatcher.appendTail(gstinBuffer);
            sanitized = gstinBuffer.toString();

            // 3. Replace standalone 10-character Indian PAN numbers
            Matcher panMatcher = PAN_PATTERN.matcher(sanitized);
            StringBuilder panBuffer = new StringBuilder();
            while (panMatcher.find()) {
                String foundPan = panMatcher.group().toUpperCase(Locale.ROOT);
                String placeholder = resolvePanPlaceholder(foundPan);
                panMatcher.appendReplacement(panBuffer, Matcher.quoteReplacement(placeholder));
            }
            panMatcher.appendTail(panBuffer);

            return panBuffer.toString();
        }

        public String unmask(String llmOutput) {
            if (llmOutput == null || llmOutput.isBlank()) {
                return "";
            }
            String restored = llmOutput;
            for (Map.Entry<String, String> entry : placeholderToReal.entrySet()) {
                restored = restored.replace(entry.getKey(), entry.getValue());
            }
            return restored;
        }

        public List<MaskedTokenAudit> getAuditLog() {
            return Collections.unmodifiableList(auditLog);
        }

        private String resolveGstinPlaceholder(String gstin) {
            if (realToPlaceholder.containsKey(gstin)) {
                return realToPlaceholder.get(gstin);
            }
            if (gstin.equals(taxpayerGstin)) {
                registerMapping(gstin, "[TAXPAYER_GSTIN]", "TAXPAYER_GSTIN");
                return "[TAXPAYER_GSTIN]";
            }
            vendorCounter++;
            String placeholder = "[VENDOR_GSTIN_" + vendorCounter + "]";
            registerMapping(gstin, placeholder, "VENDOR_GSTIN");
            return placeholder;
        }

        private String resolvePanPlaceholder(String pan) {
            if (realToPlaceholder.containsKey(pan)) {
                return realToPlaceholder.get(pan);
            }
            panCounter++;
            String placeholder = panCounter == 1 ? "[PAN_MASKED]" : "[PAN_MASKED_" + panCounter + "]";
            registerMapping(pan, placeholder, "PAN");
            return placeholder;
        }

        private void registerMapping(String realValue, String placeholder, String category) {
            realToPlaceholder.put(realValue, placeholder);
            placeholderToReal.put(placeholder, realValue);
            auditLog.add(new MaskedTokenAudit(placeholder, category, redactPreview(realValue)));
        }

        private String redactPreview(String realValue) {
            if (realValue == null || realValue.length() <= 4) {
                return "****";
            }
            return realValue.substring(0, 2) + "***" + realValue.substring(realValue.length() - 2);
        }
    }
}
