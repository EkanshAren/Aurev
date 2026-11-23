
import React, { useState } from "react";
import { mockData } from "./data/mockData";

const USE_MOCK_DATA = true;

const money = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
  }).format(Number(value || 0));

function Icon({ name }) {
  return <i className={`bi bi-${name}`} aria-hidden="true" />;
}

function Sidebar({ active, onNavigate }) {
  const links = [
    { id: "dashboard", label: "Dashboard", icon: "grid-1x2-fill" },
    { id: "upload", label: "Upload Documents", icon: "cloud-arrow-up" },
    { id: "reconciliation", label: "Reconciliation", icon: "arrow-left-right" },
    { id: "draft", label: "Legal Draft", icon: "file-earmark-text" },
    { id: "audit", label: "PII Audit Log", icon: "shield-lock" }
  ];

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-icon"><Icon name="shield-check" /></div>
        <span className="brand-name">aur<span>ev</span>.</span>
      </div>

      <div className="nav-label">Workspace</div>

      <nav>
        {links.map((link) => (
          <button
            key={link.id}
            className={`nav-item ${active === link.id ? "active" : ""}`}
            onClick={() => onNavigate(link.id)}
          >
            <Icon name={link.icon} />
            <span>{link.label}</span>
          </button>
        ))}
      </nav>

      <div className="sidebar-bottom">
        <div className="profile">
          <div className="avatar">A</div>
          <div>
            <strong>Aurev Workspace</strong>
            <small>Professional Account</small>
          </div>
        </div>
      </div>
    </aside>
  );
}

function Topbar() {
  return (
    <header className="topbar">
      <div className="breadcrumb">
        Workspace <span>/</span> <strong>GST Intelligence</strong>
      </div>
      <div className="topbar-right">
        <span className="system-status">
          <span className="status-dot" /> System ready
        </span>
        <button className="icon-button" aria-label="Notifications">
          <Icon name="bell" />
        </button>
        <button className="icon-button" aria-label="Settings">
          <Icon name="gear" />
        </button>
      </div>
    </header>
  );
}

function StatCard({ title, value, icon, note }) {
  return (
    <article className="glass-card stat-card">
      <div className="stat-top">
        <span>{title}</span>
        <div className="stat-icon"><Icon name={icon} /></div>
      </div>
      <div className="stat-value">{money(value)}</div>
      <div className="stat-note">{note}</div>
    </article>
  );
}

function UploadCard({ title, description, icon, accept, file, onChange }) {
  return (
    <div className="upload-card">
      <div className="upload-icon"><Icon name={icon} /></div>
      <h3>{title}</h3>
      <p>{description}</p>
      <label className="file-picker">
        <Icon name="paperclip" />
        <span>{file ? file.name : "Choose file"}</span>
        <input type="file" accept={accept}
          onChange={(event) => onChange(event.target.files?.[0] || null)} />
      </label>
    </div>
  );
}

function Reconciliation({ rows, onReconcile }) {
  return (
    <section id="reconciliation" className="glass-card section-card">
      <div className="section-heading">
        <div>
          <h2>Invoice Reconciliation</h2>
          <p>Compare purchase register with GSTR-2B records.</p>
        </div>
        <button className="primary-button" onClick={onReconcile}>
          <Icon name="arrow-repeat" /> Reconcile
        </button>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Supplier</th>
              <th>Invoice</th>
              <th>Books Amount</th>
              <th>GSTR-2B</th>
              <th>Variance</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row, index) => (
              <tr key={`${row.invoice}-${index}`}>
                <td>{row.supplier}</td>
                <td>{row.invoice}</td>
                <td>{money(row.books)}</td>
                <td>{money(row.gstr2b)}</td>
                <td>{money(row.variance)}</td>
                <td>
                  <span className={`status-badge ${row.status.toLowerCase()}`}>
                    {row.status}
                  </span>
                </td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr><td colSpan="6" className="empty-cell">
                No reconciliation records yet.
              </td></tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function App() {
  const [active, setActive] = useState("dashboard");
  const [files, setFiles] = useState({
    noticePdf: null,
    gstr2bJson: null,
    purchaseJson: null
  });

  const [notice, setNotice] = useState(mockData.notice);
  const [kpis, setKpis] = useState(mockData.kpis);
  const [rows, setRows] = useState(mockData.reconciliation);
  const [draft, setDraft] = useState(mockData.draft);
  const [citations] = useState(mockData.citations);
  const [audit] = useState(mockData.audit);
  const [auditOpen, setAuditOpen] = useState(false);
  const [selectedCitation, setSelectedCitation] = useState(null);
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  const [animationKey, setAnimationKey] = useState(0);

  const navigate = (section) => {
    setActive(section);
    setAnimationKey((key) => key + 1);

    if (section === "dashboard") {
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }

    document.getElementById(section)?.scrollIntoView({
      behavior: "smooth",
      block: "start"
    });
  };

  const showMessage = (text) => {
    setMessage(text);
    window.setTimeout(() => setMessage(""), 4000);
  };

  const handleFile = (key, file) => {
    if (!file) return;

    const allowed = {
      noticePdf: [".pdf"],
      gstr2bJson: [".json"],
      purchaseJson: [".json"]
    };

    const valid = allowed[key].some((extension) =>
      file.name.toLowerCase().endsWith(extension)
    );

    if (!valid) {
      showMessage(`Please select a valid ${allowed[key].join(" or ")} file.`);
      return;
    }

    setFiles((previous) => ({ ...previous, [key]: file }));
  };

  const handleUpload = async () => {
    if (!files.noticePdf || !files.gstr2bJson || !files.purchaseJson) {
      showMessage("Please select all three documents first.");
      return;
    }

    setBusy(true);

    try {
      if (USE_MOCK_DATA) {
        await new Promise((resolve) => setTimeout(resolve, 900));
        setNotice(mockData.notice);
        setKpis(mockData.kpis);
        setRows(mockData.reconciliation);
        setDraft(mockData.draft);
        showMessage("Demo analysis loaded successfully.");
      } else {
        const formData = new FormData();
        formData.append("noticePdf", files.noticePdf);
        formData.append("gstr2bJson", files.gstr2bJson);
        formData.append("purchaseJson", files.purchaseJson);

        const response = await fetch("/api/notices/upload", {
          method: "POST",
          body: formData
        });

        if (!response.ok) throw new Error("Upload failed");

        const result = await response.json();
        setNotice((previous) => ({ ...previous, ...result }));
        showMessage("Documents uploaded successfully.");
      }
    } catch (error) {
      showMessage(error.message || "Something went wrong.");
    } finally {
      setBusy(false);
    }
  };

  const handleReconcile = async () => {
    if (USE_MOCK_DATA) {
      setRows(mockData.reconciliation);
      setKpis(mockData.kpis);
      showMessage("Demo reconciliation refreshed.");
      return;
    }

    try {
      const response = await fetch(
        `/api/notices/${notice.noticeId}/reconcile`
      );
      if (!response.ok) throw new Error("Reconciliation request failed");

      const result = await response.json();
      setRows(result.reconciliation || result.rows || []);
      if (result.kpis) setKpis(result.kpis);
      showMessage("Reconciliation updated.");
    } catch (error) {
      showMessage(error.message);
    }
  };

  const handleExport = async () => {
    if (USE_MOCK_DATA) {
      showMessage("Connect the backend Word exporter to download a DOCX.");
      return;
    }

    try {
      const response = await fetch(
        `/api/notices/${notice.noticeId}/export`
      );
      if (!response.ok) throw new Error("Export failed");

      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "GST_Notice_Reply.docx";
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      showMessage(error.message);
    }
  };

  const handleSaveDraft = async () => {
    if (USE_MOCK_DATA) {
      showMessage("Draft saved in this demo session.");
      return;
    }

    try {
      const response = await fetch(
        `/api/notices/${notice.noticeId}/draft`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            draftText: draft,
            verifiedCitations: citations.map((item) => item.id),
            maskedTokens: audit.map((item) => item.masked)
          })
        }
      );

      if (!response.ok) throw new Error("Could not save draft");
      showMessage("Draft saved successfully.");
    } catch (error) {
      showMessage(error.message);
    }
  };

  return (
    <div className="app-shell">
      <div className="ambient ambient-one" />
      <div className="ambient ambient-two" />

      <Sidebar active={active} onNavigate={navigate} />

      <div className="main-area">
        <Topbar />

        <main className="dashboard" key={animationKey}>
          <section className="welcome-row" id="dashboard">
            <div>
              <div className="eyebrow">
                <Icon name="stars" /> AI-POWERED GST INTELLIGENCE
              </div>
              <h1>GST Notice Workspace</h1>
              <p className="page-description">
                Analyze notices, reconcile invoices and prepare
                legal responses in one intelligent workspace.
              </p>
            </div>

            <button className="primary-button"
              onClick={() => navigate("upload")}>
              <Icon name="plus-lg" /> New Analysis
            </button>
          </section>

          <section className="stats-grid">
            <StatCard title="Demanded ITC"
              value={kpis.demandedItc}
              icon="currency-rupee"
              note="Total ITC mentioned in notice" />

            <StatCard title="Explained Mismatch"
              value={kpis.explainedMismatch}
              icon="check2-circle"
              note="Amount explained through reconciliation" />

            <StatCard title="Net Gap"
              value={kpis.netGap}
              icon="exclamation-circle"
              note="Remaining unexplained difference" />
          </section>

          <section id="upload" className="section-block">
            <div className="section-heading">
              <div>
                <h2>Document Intelligence</h2>
                <p>Upload your documents to begin analysis.</p>
              </div>
              <span className="section-tag">
                <Icon name="lock-fill" /> Secure upload
              </span>
            </div>

            <div className="upload-grid">
              <UploadCard
                title="GST Notice PDF"
                description="Upload the GST department notice."
                icon="file-earmark-pdf"
                accept=".pdf,application/pdf"
                file={files.noticePdf}
                onChange={(file) => handleFile("noticePdf", file)}
              />

              <UploadCard
                title="GSTR-2B Data"
                description="Upload your GSTR-2B purchase data."
                icon="file-earmark-code"
                accept=".json,application/json"
                file={files.gstr2bJson}
                onChange={(file) => handleFile("gstr2bJson", file)}
              />

              <UploadCard
                title="Purchase Register"
                description="Upload your purchase register."
                icon="receipt"
                accept=".json,application/json"
                file={files.purchaseJson}
                onChange={(file) => handleFile("purchaseJson", file)}
              />
            </div>

            <div className="upload-footer">
              <span>PDF and JSON documents supported.</span>
              <button className="primary-button" onClick={handleUpload}
                disabled={busy}>
                <Icon name="magic" />
                {busy ? "Analyzing..." : "Analyze Documents"}
              </button>
            </div>
          </section>

          <section className="overview-grid">
            <div className="glass-card section-card">
              <div className="section-heading">
                <div>
                  <h2>Notice Overview</h2>
                  <p>Information extracted from the notice.</p>
                </div>
                <span className="section-tag">AI extracted</span>
              </div>

              <div className="detail-grid">
                <div className="detail-item">
                  <small>Notice Type</small>
                  <strong>{notice.noticeType}</strong>
                </div>
                <div className="detail-item">
                  <small>Notice ID</small>
                  <strong>{notice.noticeId}</strong>
                </div>
                <div className="detail-item">
                  <small>DIN Number</small>
                  <strong>{notice.noticeDIN}</strong>
                </div>
                <div className="detail-item">
                  <small>GSTIN</small>
                  <strong>{notice.noticeGSTIN}</strong>
                </div>
              </div>
            </div>

            <div className="glass-card section-card">
              <div className="section-heading">
                <div>
                  <h2>Reconciliation Summary</h2>
                  <p>Review invoice matching progress.</p>
                </div>
                <span className="section-tag">Analysis</span>
              </div>

              <div className="summary-value">{rows.length}</div>
              <p className="stat-note">Invoice records</p>

              <button className="secondary-button"
                onClick={() => navigate("reconciliation")}>
                View reconciliation <Icon name="arrow-up-right" />
              </button>
            </div>
          </section>

          <Reconciliation rows={rows} onReconcile={handleReconcile} />

          <section id="draft" className="glass-card section-card">
            <div className="section-heading">
              <div>
                <h2>AI Legal Reply Draft</h2>
                <p>Review and edit your proposed legal response.</p>
              </div>
              <span className="section-tag">
                <Icon name="stars" /> AI generated demo
              </span>
            </div>

            <textarea
              className="draft-editor"
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              spellCheck="true"
            />

            <div className="section-heading citation-heading">
              <div>
                <h2>Verified Citations</h2>
                <p>References associated with the draft.</p>
              </div>
            </div>

            <div className="citation-list">
              {citations.map((citation) => (
                <button key={citation.id}
                  className="citation-chip"
                  onClick={() => setSelectedCitation(citation)}>
                  <Icon name="patch-check" /> {citation.title}
                </button>
              ))}
            </div>

            <div className="draft-actions">
              <button className="secondary-button" onClick={handleSaveDraft}>
                <Icon name="save" /> Save Draft
              </button>
              <button className="primary-button" onClick={handleExport}>
                <Icon name="file-earmark-word" /> Export Word Document
              </button>
            </div>
          </section>

          <section id="audit" className="glass-card section-card">
            <div className="section-heading">
              <div>
                <h2>Privacy & PII Audit</h2>
                <p>Review sensitive information masking records.</p>
              </div>
              <span className="section-tag">
                <Icon name="shield-check" /> Privacy layer
              </span>
            </div>

            <button className="secondary-button"
              onClick={() => setAuditOpen(!auditOpen)}>
              <Icon name={auditOpen ? "eye-slash" : "eye"} />
              {auditOpen ? "Hide Audit Log" : "View Audit Log"}
            </button>

            {auditOpen && (
              <div className="audit-list">
                {audit.map((item) => (
                  <div className="audit-row" key={item.masked}>
                    <span>{item.field}</span>
                    <span className="masked-value">{item.masked}</span>
                  </div>
                ))}
              </div>
            )}
          </section>

          <footer className="footer">
            <span>© 2026 Aurev. Intelligent GST Notice Copilot.</span>
            <span>Built for smarter compliance workflows.</span>
          </footer>
        </main>
      </div>

      {message && (
        <div className="toast-message" role="status">
          <Icon name="info-circle" /> {message}
          <button onClick={() => setMessage("")} aria-label="Dismiss">
            <Icon name="x-lg" />
          </button>
        </div>
      )}

      {selectedCitation && (
        <div className="modal-backdrop"
          onClick={() => setSelectedCitation(null)}>
          <div className="citation-modal"
            role="dialog"
            aria-modal="true"
            onClick={(event) => event.stopPropagation()}>
            <button className="modal-close"
              onClick={() => setSelectedCitation(null)}
              aria-label="Close">
              <Icon name="x-lg" />
            </button>
            <div className="eyebrow">LEGAL REFERENCE</div>
            <h2>{selectedCitation.title}</h2>
            <p>{selectedCitation.description}</p>
            <p className="legal-warning">
              Demo reference only. Confirm the official text, applicability
              and current legal status before using this citation in a reply.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
