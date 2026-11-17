package com.aurev.backend.controller;

import com.aurev.backend.dto.ReconciliationResponseDto;
import com.aurev.backend.service.GstrJsonParserService;
import com.aurev.backend.service.ReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin(origins = "*")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final GstrJsonParserService jsonParserService;

    public ReconciliationController(ReconciliationService reconciliationService,
                                    GstrJsonParserService jsonParserService) {
        this.reconciliationService = reconciliationService;
        this.jsonParserService = jsonParserService;
    }

    @PostMapping("/{noticeId}/reconcile")
    public ResponseEntity<ReconciliationResponseDto> reconcile(
            @PathVariable String noticeId,
            @RequestParam(value = "gstr2bFile", required = false) MultipartFile gstr2bFile,
            @RequestParam(value = "ledgerFile", required = false) MultipartFile ledgerFile) throws IOException {

        ReconciliationResponseDto initial = reconciliationService.reconcileNotice(noticeId);

        if (gstr2bFile != null && !gstr2bFile.isEmpty()) {
            jsonParserService.ingestGstr2bJson(initial.clientId(), gstr2bFile.getInputStream());
        }
        if (ledgerFile != null && !ledgerFile.isEmpty()) {
            jsonParserService.ingestPurchaseLedgerJson(initial.clientId(), ledgerFile.getInputStream());
        }

        ReconciliationResponseDto finalResult = (gstr2bFile != null || ledgerFile != null)
                ? reconciliationService.reconcileNotice(noticeId)
                : initial;

        return ResponseEntity.ok(finalResult);
    }

    @GetMapping("/health/reconciliation")
    public ResponseEntity> health() {
        return ResponseEntity.ok(Map.of("module", "db-reconciliation", "status", "UP"));
    }
}
