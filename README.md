# Studily — AI-Powered Study Assistant

Turn lecture notes into **summaries, flashcards, and MCQ quizzes** in seconds.
Built with Java EE (JSP + Servlets), PostgreSQL (Supabase), JDBC, Apache Tomcat, and the Groq API.

## ✨ Features

| Area | What you get |
| --- | --- |
| **Auth** | Register, login, logout, BCrypt hashing, session fixation protection, route protection via filter |
| **Upload** | PDF (PDFBox text extraction, magic-byte + size validation) or pasted text |
| **AI** | Groq chat completions → strict JSON → summary, key concepts, definitions, exam tips, flashcards, MCQs |
| **Flashcards** | 3D flip, shuffle, keyboard navigation, "mark known" progress |
| **Quiz** | Difficulty filter, countdown timer with auto-submit, instant feedback, AI explanations, saved attempts |
| **Dashboard** | Live MySQL-backed stats: notes, flashcards, average score, study streak, recent activity |
| **History** | Every note and quiz attempt, one page |

## 🏗 Architecture (MVC)

```
src/main/java/com/studily/
├── controller/   Servlets (thin — validate, call services, forward to JSP)
├── dao/          All SQL via PreparedStatement
├── model/        POJOs
├── service/      AIService (Groq), PDFService (PDFBox), AuthService, DashboardService
├── filter/       AuthFilter (route protection), FlashFilter (one-shot messages)
└── util/         DBConnection, AppConfig, Validators, Flash
```

Views live in `src/main/webapp/WEB-INF/views/` (direct access impossible; everything forwards
through servlets). Business logic never lives inside JSP.

## 🚀 Setup

### 1. Database (Supabase)

1. Create a project at https://supabase.com (free tier works).
2. Open **SQL Editor** in the Supabase dashboard, paste `database/studily_schema_postgres.sql`, and click **Run**.
3. Copy your connection details from **Project Settings → Database → Connection string (URI)**.

### 2. Configuration

Edit `src/main/resources/application.properties` — **this is the only place you enter credentials**:

```properties
db.url=jdbc:postgresql://db.YOUR-PROJECT-REF.supabase.co:5432/postgres
db.user=postgres
db.password=YOUR_SUPABASE_DB_PASSWORD
groq.api.key=YOUR_GROQ_API_KEY
```

- `db.url` — from Supabase settings; keep the `jdbc:postgresql://` prefix (replace `postgresql://` in their URI with `jdbc:postgresql://`)
- `db.user` — `postgres` (or the role shown in Supabase settings)
- `db.password` — the DB password you set when creating the project
- `groq.api.key` — free key at https://console.groq.com/keys

> **Tip:** for a demo/review you can rebuild with your values in place. For real deployments, inject these as environment variables instead of committing them.

### 3. Build & Deploy

```bash
mvn clean package
cp target/studily.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

Open http://localhost:8080/studily/

**Requirements:** JDK 22, Maven 3.9+, Tomcat 10.1+ (Supabase hosts the database remotely, so no local DB install needed).

> PDFs are stored under `studily-uploads/` (override with the `STUDILY_UPLOAD_DIR` env var).

## 🔒 Security

- Every SQL statement is a `PreparedStatement`
- PostgreSQL (Supabase) with SSL enforced by default
- BCrypt (cost 12) password hashing
- Server-side validation for every input (email regex, password strength, PDF magic bytes, size caps)
- Session fixation rotation on login, `HttpOnly` cookies, cookie-only tracking
- AuthFilter guards all private routes; note queries are always ownership-scoped (`noteId AND userId`)

## 🧠 AI Pipeline

1. PDF → text (PDFBox) or sanitized pasted text (persisted to Supabase Postgres)
2. Strict JSON-only prompt to Groq (`response_format: json_object`)
3. Gson parse with fence/trailing-text tolerance
4. Retry once on transient network failures; friendly errors for 401/429/5xx
5. Results persisted: summary JSON on `notes`, rows in `flashcards` and `mcqs`

## 🗺 Roadmap

DOCX support · AI chat with notes · OCR for scanned PDFs · voice revision · study groups · mobile app
