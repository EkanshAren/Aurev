# GST Notice Copilot — Java 21 + Spring Boot 4.1.2 + MySQL 8 Backend

Base Package: `com.gstcopilot.backend`
Database: **MySQL 8.x (InnoDB / utf8mb4)** — fully migrated from PostgreSQL & `pgvector`.

## What Changed from PostgreSQL to MySQL 8
1. **JDBC & Hibernate**: Replaced `org.postgresql:postgresql` with `com.mysql:mysql-connector-j` and `org.hibernate.dialect.MySQLDialect`.
2. **FULL OUTER JOIN Emulation**: MySQL 8 does not support native `FULL OUTER JOIN`. `PurchaseRegisterInvoiceRepository.reconcileInvoicesByNoticeId` implements an exact, parameterized `LEFT JOIN ... UNION ALL ... LEFT JOIN ... WHERE p.id IS NULL` query to deliver 100% SQL-first invoice matching.
3. **Legal Vector Retrieval Without `pgvector`**: Removed `spring-ai-starter-vector-store-pgvector` and introduced `MySqlLegalVectorStoreService`. Embeddings are stored in `legal_provisions.embedding_json` (`JSON` column) with in-JVM cosine similarity when an `EmbeddingModel` is active, plus a native MySQL `FULLTEXT` (`MATCH ... AGAINST`) fallback for zero-embedding cloud chat setups.
4. **Data Types & Bootstrap**: Uses `BIGINT AUTO_INCREMENT`, `DECIMAL(15,2)`, `LONGTEXT`, `JSON`, `InnoDB`, and idempotent `INSERT IGNORE` in `data.sql`.

## Quick Start (Local MySQL 8)
```bash
# 1. Export credentials & Groq API key (never hardcode secrets)
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DB=gst_notice_copilot
export MYSQL_USER=root
export MYSQL_PASSWORD=your_mysql_password
export GROQ_API_KEY=your_groq_api_key

# 2. Run Spring Boot (online Groq profile)
cd backend
mvn spring-boot:run

# 3. Or run with Offline Local Ollama (llama3.2 on localhost:11434)
mvn spring-boot:run -Dspring-boot.run.profiles=offline
```

## Locked REST Endpoints
- `POST /api/notices/upload` (`multipart/form-data`: `noticePdf`, `gstr2bJson`, `purchaseJson`)
- `GET /api/notices/{id}/reconcile`
- `POST /api/notices/{id}/draft`
- `GET /api/notices/{id}/export-docx`
