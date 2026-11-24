# Aurev — AI-Powered GST Notice Reconciliation & Legal Reply Copilot

<p align="center">

**Simplifying GST Notice Analysis, ITC Reconciliation & Legal Response Drafting.**

An intelligent workspace designed to help businesses and tax professionals analyze GST notices, identify ITC mismatches, and prepare structured responses.

</p>

---

## About Aurev

Aurev is an AI-powered GST compliance assistant designed to simplify the complex process of handling GST notices.

Businesses and Chartered Accountants often spend significant time reviewing GST notices, comparing purchase records with GSTR-2B data, identifying Input Tax Credit (ITC) mismatches, and preparing legal responses.

Aurev aims to bring these workflows into one unified platform through document processing, automated reconciliation, AI-assisted drafting, and data privacy auditing.

## The Problem

Handling GST notices manually can be time-consuming and error-prone.

- Manual comparison of purchase registers and GSTR-2B data.
- Difficulty identifying invoice-level ITC mismatches.
- Time-consuming preparation of legal replies.
- Challenges in organizing supporting documents and references.
- Risk of exposing sensitive taxpayer information.

## Our Solution

Aurev provides a centralized workspace to:

- Upload GST notices and supporting financial documents.
- Analyze invoice-level differences between purchase records and GSTR-2B.
- Identify matched, mismatched, and partially matched invoices.
- Generate structured, editable legal reply drafts.
- Organize relevant legal references and citations for verification.
- Maintain a PII masking and audit workflow.

## Key Features

### 1. GST Document Upload
Upload the documents required for GST notice analysis:

- GST Notice (PDF)
- GSTR-2B Data (JSON)
- Purchase Register (JSON)

### 2. ITC Reconciliation
Compare purchase register data against GSTR-2B and identify:

- Matched invoices
- Mismatched invoices
- Partial matches
- ITC differences

### 3. AI-Assisted Legal Reply Drafting
Generate structured GST notice response drafts that can be reviewed and edited before use.

### 4. Legal References & Citations
Organize relevant circulars and supporting references to help users verify the legal basis of a response.

### 5. PII Audit & Privacy
Support the identification and masking of sensitive taxpayer information through an audit workflow.

### 6. Interactive Dashboard
A modern workspace for viewing reconciliation summaries, financial differences, uploaded documents, and draft responses.

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React.js, JavaScript, HTML5, CSS3 |
| Build Tool | Vite |
| Backend | Java 21, Spring Boot |
| Database | MySQL |
| AI Integration | Spring AI |
| Document Processing | Apache PDFBox, Apache POI |
| Version Control | Git & GitHub |

## System Architecture

```text
             USER
               |
               v
        AUREV DASHBOARD
          React + Vite
               |
               v
         SPRING BOOT API
               |
       +-------+-------+
       |       |       |
       v       v       v
    Document  ITC     AI Reply
    Processing Reconciliation Drafting
       |       |       |
       +-------+-------+
               |
               v
            MySQL
               |
               v
       GST Analysis Results
```

## Project Structure

```text
Aurev/
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── data/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── main.jsx
│   ├── index.html
│   └── package.json
│
├── backend/
│   └── src/
│
├── .gitignore
└── README.md
```

*The structure may evolve as development progresses.*

## Getting Started

### Prerequisites

Install the following:

- Node.js and npm
- Java JDK 21
- MySQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/EkanshAren/Aurev.git
```

```bash
cd Aurev
```

### 2. Set Up the Frontend

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the local URL displayed in your terminal, usually:

```text
http://localhost:5173
```

### 3. Set Up the Backend

Navigate to the backend directory:

```bash
cd backend
```

Configure your MySQL database and application properties.

Run the Spring Boot application using your IDE or Maven.

*Backend API configuration and database setup may require project-specific environment variables.*

## API Endpoints

The planned API contract includes:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/notices/upload` | Upload GST documents |
| GET | `/api/notices/{id}/reconcile` | Retrieve reconciliation results |
| POST | `/api/notices/{id}/draft` | Create or save reply drafts |
| GET | `/api/notices/{id}/export` | Export the reply document |

## Project Goals

- Reduce manual GST reconciliation effort.
- Improve visibility into ITC mismatches.
- Simplify GST notice response preparation.
- Improve document organization and review workflows.
- Support responsible handling of sensitive financial information.

## Important Disclaimer

Aurev is a software project intended to assist with GST document analysis and response preparation. Its outputs, calculations, legal references, and draft responses require verification by a qualified tax professional before submission. It does not replace professional legal or tax advice.

## Future Enhancements

- Advanced AI-powered document understanding.
- Automated invoice extraction from PDF documents.
- Improved invoice matching and mismatch detection.
- AI-powered legal response refinement.
- Downloadable DOCX and PDF reports.
- Enhanced document security and privacy controls.

## Contributing

Contributions, suggestions, and feedback are welcome.

1. Fork the repository.
2. Create a new feature branch.
3. Make your changes.
4. Commit your changes.
5. Submit a Pull Request.

## License

A license has not yet been specified. Please check the repository for licensing information before reusing or distributing this project.

---

<p align="center">

**Aurev — Making GST Compliance Smarter.**

Built with the vision of simplifying complex compliance workflows through technology.

</p>
