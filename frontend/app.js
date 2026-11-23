
/*
  GST NOTICE COPILOT
  File 4: frontend/app.js

  Locked API endpoints:
  POST /api/notices/upload
  GET  /api/notices/{id}/reconcile
  POST /api/notices/{id}/draft
  GET  /api/notices/{id}/export
*/

const USE_MOCK_DATA = true;

let currentNoticeId = null;
let currentNotice = null;
let currentReconciliation = null;

// --------------------------------------------------
// DOM HELPERS
// --------------------------------------------------

const $ = (id) => document.getElementById(id);

function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;"
  })[char]);
}

function formatINR(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value) || 0);
}

function showAlert(message) {
  const container = $("alertContainer");
  if (!container) return;

  const alert = document.createElement("div");
  alert.className = "app-alert";
  alert.textContent = message;

  container.appendChild(alert);

  window.setTimeout(() => alert.remove(), 4000);
}

function setWorkflowStatus(message) {
  const status = $("workflowStatus");
  if (status) status.textContent = message;
}

function setButtonLoading(button, loading, normalText) {
  if (!button) return;

  button.disabled = loading;
  button.innerHTML = loading
    ? `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Please wait...`
    : normalText;
}

async function apiRequest(url, options = {}) {
  const response = await fetch(url, options);

  if (!response.ok) {
    let message = `Request failed (${response.status})`;

    try {
      const error = await response.json();
      message = error.message || error.error || message;
    } catch {
      // Keep the default message if the server response isn't JSON.
    }

    throw new Error(message);
  }

  return response;
}

async function apiJson(url, options = {}) {
  const response = await apiRequest(url, options);
  return response.json();
}

// --------------------------------------------------
// FILE SELECTION
// --------------------------------------------------

const fileInputs = [
  {
    id: "noticePdf",
    nameId: "noticeFileName",
    defaultText: "ASMT-10 / DRC-01 / REG-17",
    extensions: [".pdf"]
  },
  {
    id: "gstr2bJson",
    nameId: "gstrFileName",
    defaultText: "GST portal purchase data",
    extensions: [".json"]
  },
  {
    id: "purchaseJson",
    nameId: "purchaseFileName",
    defaultText: "Books / purchase register",
    extensions: [".json"]
  }
];

fileInputs.forEach((item) => {
  const input = $(item.id);
  const nameElement = $(item.nameId);

  if (!input || !nameElement) return;

  input.addEventListener("change", () => {
    const file = input.files[0];

    if (!file) {
      nameElement.textContent = item.defaultText;
      return;
    }

    const validExtension = item.extensions.some((extension) =>
      file.name.toLowerCase().endsWith(extension)
    );

    if (!validExtension) {
      input.value = "";
      nameElement.textContent = item.defaultText;
      showAlert(`Please select a valid ${item.extensions.join(" or ")} file.`);
      return;
    }

    nameElement.textContent = file.name;
  });
});

function getSelectedFiles() {
  return {
    noticePdf: $("noticePdf")?.files[0],
    gstr2bJson: $("gstr2bJson")?.files[0],
    purchaseJson: $("purchaseJson")?.files[0]
  };
}

function validateFiles(files) {
  if (!files.noticePdf || !files.gstr2bJson || !files.purchaseJson) {
    showAlert("Please select all three required documents first.");
    return false;
  }

  return true;
}

// --------------------------------------------------
// NOTICE HEADER
// --------------------------------------------------

function renderNotice(notice) {
  currentNotice = notice;
  currentNoticeId = notice.noticeId;

  $("noticeType").textContent = notice.noticeType || "GST NOTICE";
  $("noticeDIN").textContent = notice.din || "Not available";
  $("noticeGSTIN").textContent = notice.gstin || "Not available";
  $("noticeId").textContent = notice.noticeId ?? "Not generated";

  $("demandedItc").textContent = formatINR(notice.demandedItc);
}

// --------------------------------------------------
// KPI CARDS
// --------------------------------------------------

function renderKPIs(data) {
  $("demandedItc").textContent = formatINR(data.demandedItc);
  $("explainedMismatch").textContent = formatINR(data.explainedMismatch);
  $("netGap").textContent = formatINR(data.netGap);
}

// --------------------------------------------------
// RECONCILIATION TABLE
// --------------------------------------------------

function getStatusBadge(status) {
  const normalized = String(status || "").toUpperCase();

  if (normalized === "MATCHED" || normalized === "MATCH") {
    return '<span class="match-badge">MATCHED</span>';
  }

  if (normalized === "PARTIAL") {
    return '<span class="partial-badge">PARTIAL</span>';
  }

  return '<span class="mismatch-badge">MISMATCH</span>';
}

function renderReconciliation(data) {
  currentReconciliation = data;

  renderKPIs(data);

  const rows = Array.isArray(data.rows) ? data.rows : [];

  $("rowCount").textContent =
    `${rows.length} invoice record${rows.length === 1 ? "" : "s"}`;

  $("reconciliationRows").innerHTML = rows.length
    ? rows.map((row) => {
        const variance = Number(row.variance) || 0;
        const varianceClass = variance === 0
          ? "variance-zero"
          : "variance-positive";

        return `
          <tr>
            <td>
              <span class="supplier-name">
                ${escapeHTML(row.supplierName || row.supplierGstin || "Supplier")}
              </span>
              <span class="invoice-number">
                ${escapeHTML(row.invoiceNo || "—")}
              </span>
            </td>

            <td class="amount-cell">
              ${formatINR(row.booksItc)}
            </td>

            <td class="amount-cell">
              ${formatINR(row.gstr2bItc)}
            </td>

            <td class="amount-cell ${varianceClass}">
              ${formatINR(variance)}
            </td>

            <td>${getStatusBadge(row.status)}</td>
          </tr>
        `;
      }).join("")
    : `
      <tr>
        <td colspan="5" class="text-center py-4 text-muted">
          No invoice records available.
        </td>
      </tr>
    `;
}

// --------------------------------------------------
// CITATION BADGES AND MODAL
// --------------------------------------------------

function renderCitations(citations) {
  const container = $("citationBadges");

  if (!container) return;

  container.innerHTML = "";

  citations.forEach((citation) => {
    const button = document.createElement("button");

    button.type = "button";
    button.className = "citation-badge";
    button.textContent = citation.id;
    button.addEventListener("click", () => openCitation(citation));

    container.appendChild(button);
  });
}

function openCitation(citation) {
  $("citationTitle").textContent = citation.title || citation.id;
  $("citationText").textContent =
    citation.text || "Verified statutory text is not available.";

  const modalElement = $("citationModal");

  if (window.bootstrap && modalElement) {
    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
    modal.show();
  } else {
    showAlert("Bootstrap modal could not be loaded.");
  }
}

// --------------------------------------------------
// PII MASKING AUDIT LOG
// --------------------------------------------------

function renderAuditLog(items) {
  const container = $("auditRows");

  if (!container) return;

  container.innerHTML = items.map((item) => `
    <tr>
      <td>${escapeHTML(item.field)}</td>
      <td>${escapeHTML(item.original)}</td>
      <td class="masked-value">${escapeHTML(item.masked)}</td>
      <td><span class="match-badge">${escapeHTML(item.status)}</span></td>
    </tr>
  `).join("");
}

function setupAuditToggle() {
  const toggle = $("auditToggle");
  const content = $("auditContent");

  if (!toggle || !content) return;

  toggle.addEventListener("click", () => {
    const expanded = toggle.getAttribute("aria-expanded") === "true";

    toggle.setAttribute("aria-expanded", String(!expanded));
    content.hidden = expanded;
  });
}

// --------------------------------------------------
// MOCK MODE
// --------------------------------------------------

function loadMockCase() {
  renderNotice(MOCK_DATA.notice);
  renderReconciliation(MOCK_DATA.reconciliation);
  renderCitations(MOCK_DATA.citations);
  renderAuditLog(MOCK_DATA.audit);

  $("draftEditor").textContent = MOCK_DATA.draft.draftText;

  setWorkflowStatus("Demo data loaded");
}

async function uploadMockCase() {
  // In mock mode, the selected files are checked for presence.
  // Their contents are not uploaded or processed.
  loadMockCase();

  showAlert("Demo case loaded. No files were sent to a server.");
}

// --------------------------------------------------
// 1. UPLOAD NOTICE
// POST /api/notices/upload
// --------------------------------------------------

async function uploadNotice() {
  const files = getSelectedFiles();

  if (!validateFiles(files)) return;

  const button = $("uploadBtn");
  const originalText =
    '<i class="bi bi-arrow-up-circle"></i> Upload & Analyse';

  try {
    setButtonLoading(button, true, originalText);
    setWorkflowStatus("Uploading documents...");

    if (USE_MOCK_DATA) {
      await uploadMockCase();
      return;
    }

    const formData = new FormData();

    formData.append("noticePdf", files.noticePdf);
    formData.append("gstr2bJson", files.gstr2bJson);
    formData.append("purchaseJson", files.purchaseJson);

    const result = await apiJson("/api/notices/upload", {
      method: "POST",
      body: formData
    });

    renderNotice(result);
    setWorkflowStatus("Documents uploaded");

    showAlert("Documents uploaded successfully.");

    await reconcileNotice();
  } catch (error) {
    console.error("Upload error:", error);
    setWorkflowStatus("Upload failed");
    showAlert(error.message || "Unable to upload documents.");
  } finally {
    setButtonLoading(button, false, originalText);
  }
}

// --------------------------------------------------
// 2. RECONCILE INVOICES
// GET /api/notices/{id}/reconcile
// --------------------------------------------------

async function reconcileNotice() {
  if (!currentNoticeId) {
    if (USE_MOCK_DATA) {
      renderReconciliation(MOCK_DATA.reconciliation);
      return;
    }

    showAlert("Upload a notice before reconciling.");
    return;
  }

  const button = $("reconcileBtn");
  const originalText = '<i class="bi bi-arrow-repeat"></i> Reconcile';

  try {
    setButtonLoading(button, true, originalText);
    setWorkflowStatus("Reconciling invoices...");

    if (USE_MOCK_DATA) {
      renderReconciliation(MOCK_DATA.reconciliation);
      setWorkflowStatus("Demo reconciliation complete");
      return;
    }

    const result = await apiJson(
      `/api/notices/${encodeURIComponent(currentNoticeId)}/reconcile`
    );

    renderReconciliation(result);

    setWorkflowStatus("Reconciliation complete");
    showAlert("Invoice reconciliation completed.");
  } catch (error) {
    console.error("Reconciliation error:", error);
    setWorkflowStatus("Reconciliation failed");
    showAlert(error.message || "Unable to reconcile invoices.");
  } finally {
    setButtonLoading(button, false, originalText);
  }
}

// --------------------------------------------------
// 3. SAVE LEGAL DRAFT
// POST /api/notices/{id}/draft
// --------------------------------------------------

async function saveDraft() {
  if (!currentNoticeId) {
    throw new Error("Upload a notice before saving the draft.");
  }

  const payload = {
    draftText: $("draftEditor").innerText,
    verifiedCitations: MOCK_DATA.draft.verifiedCitations,
    maskedTokens: MOCK_DATA.draft.maskedTokens
  };

  if (USE_MOCK_DATA) {
    return payload;
  }

  return apiJson(
    `/api/notices/${encodeURIComponent(currentNoticeId)}/draft`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    }
  );
}

// --------------------------------------------------
// 4. EXPORT WORD DOCUMENT
// GET /api/notices/{id}/export
// --------------------------------------------------

async function exportWordDocument() {
  const button = $("exportBtn");
  const originalText =
    '<i class="bi bi-file-earmark-word"></i> Download Portal-Ready .docx';

  try {
    if (!currentNoticeId) {
      showAlert("Upload a notice before exporting the reply.");
      return;
    }

    setButtonLoading(button, true, originalText);

    if (USE_MOCK_DATA) {
      showAlert("DOCX export requires the Spring Boot export API. Disable mock mode when it is ready.");
      return;
    }

    setWorkflowStatus("Saving legal reply...");

    await saveDraft();

    setWorkflowStatus("Generating Word document...");

    const response = await apiRequest(
      `/api/notices/${encodeURIComponent(currentNoticeId)}/export`
    );

    const blob = await response.blob();

    if (!blob.size) {
      throw new Error("The server returned an empty Word document.");
    }

    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = downloadUrl;
    link.download = "GST_Notice_Reply.docx";

    document.body.appendChild(link);
    link.click();
    link.remove();

    URL.revokeObjectURL(downloadUrl);

    setWorkflowStatus("Word document downloaded");
    showAlert("Your Word document has been downloaded.");
  } catch (error) {
    console.error("Export error:", error);
    setWorkflowStatus("Export failed");
    showAlert(error.message || "Unable to export the Word document.");
  } finally {
    setButtonLoading(button, false, originalText);
  }
}

// --------------------------------------------------
// EVENT LISTENERS
// --------------------------------------------------

function setupEventListeners() {
  $("uploadBtn")?.addEventListener("click", uploadNotice);

  $("reconcileBtn")?.addEventListener("click", reconcileNotice);

  $("exportBtn")?.addEventListener("click", exportWordDocument);
}

// --------------------------------------------------
// APPLICATION STARTUP
// --------------------------------------------------

function initializeApp() {
  setupEventListeners();
  setupAuditToggle();

  if (USE_MOCK_DATA) {
    loadMockCase();
  } else {
    setWorkflowStatus("Awaiting documents");
  }

  console.log("GST Copilot frontend initialized.");
}

document.addEventListener("DOMContentLoaded", initializeApp);
