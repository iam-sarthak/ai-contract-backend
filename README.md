# AI Contract Intelligence Platform

A production-quality, backend-only AI Contract Intelligence Platform built with **Java 21**, **Spring Boot 3**, **PostgreSQL 15 + pgvector**, and **Google Gemini** via LangChain4j.

## Features

- JWT Authentication (ADMIN, LAWYER, USER roles)
- Contract upload (PDF/DOCX) with async processing
- Text extraction, chunking, and embedding storage (pgvector)
- AI-powered contract summary, clause extraction, and risk analysis
- RAG-based contract chat
- Semantic search across all contracts
- AI contract comparison

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.4, Maven |
| Database | PostgreSQL 15 + pgvector |
| AI | LangChain4j, Google Gemini (`gemini-2.0-flash`, `text-embedding-004`) |
| Documents | Apache PDFBox, Apache POI |
| Security | Spring Security, JWT |
| API Docs | SpringDoc OpenAPI |

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 15 with pgvector extension
- Google Gemini API key

### PostgreSQL 15 + pgvector Setup (macOS)

```bash
brew install postgresql@15
brew services start postgresql@15

# Install pgvector for PostgreSQL 15 (build from source if brew formula links to wrong version)
brew install pgvector || true
export PG_CONFIG=/opt/homebrew/opt/postgresql@15/bin/pg_config

if [ ! -f /opt/homebrew/opt/postgresql@15/share/postgresql@15/extension/vector.control ]; then
  cd /tmp
  rm -rf pgvector
  git clone --branch v0.8.0 https://github.com/pgvector/pgvector.git
  cd pgvector
  make OPTFLAGS=""
  make install
fi

# Create database
psql postgres -c "CREATE DATABASE contractai;"
psql contractai -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

Verify the extension is available:

```bash
psql contractai -c "SHOW server_version;"
ls /opt/homebrew/opt/postgresql@15/share/postgresql@15/extension/vector.control
```

## Configuration

Copy `.env.example` to `.env`, then **export** the variables (Spring Boot does not load `.env` automatically):

```bash
set -a && source .env && set +a
```

Or export manually:

```bash
export GEMINI_API_KEY=your_key
export JWT_SECRET=change-me-to-a-256-bit-secret-key-for-production-use
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

## Run

**Java 21 is required.** Maven may default to Java 17 on macOS — set `JAVA_HOME` first:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # should show 21.x
```

Then start the app:

```bash
set -a && source .env && set +a   # load .env into shell
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |

### Contracts
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/contracts/upload` | Upload PDF/DOCX |
| GET | `/api/contracts` | List contracts (paginated) |
| GET | `/api/contracts/{id}` | Get contract details |
| DELETE | `/api/contracts/{id}` | Delete contract |
| POST | `/api/contracts/compare` | Compare two contracts |

### Analysis
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/contracts/{id}/clauses` | Extracted clauses |
| GET | `/api/contracts/{id}/risk` | Risk analysis |
| GET | `/api/contracts/{id}/summary` | Contract summary |
| POST | `/api/chat/{contractId}` | RAG chat |
| POST | `/api/search` | Semantic search |

## Quick Start (curl)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"lawyer@example.com","password":"password123","firstName":"Jane","lastName":"Doe"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"lawyer@example.com","password":"password123"}' | jq -r .token)

# Upload contract
curl -X POST http://localhost:8080/api/contracts/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@contract.pdf"

# Poll status until COMPLETED
curl http://localhost:8080/api/contracts/1 \
  -H "Authorization: Bearer $TOKEN"

# Chat with contract
curl -X POST http://localhost:8080/api/chat/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"What are the termination conditions?"}'
```

## Architecture

```
com.contractai
├── config/          # App, JWT, OpenAPI, async config
├── security/        # JWT filter, SecurityConfig
├── auth/            # Register, login
├── contract/        # Upload, CRUD, processing
├── document/        # PDF/DOCX extraction, chunking
├── ai/              # Gemini services, vector store, RAG
├── clause/          # Clause extraction
├── risk/            # Risk analysis
├── summary/         # Contract summary
├── chat/            # RAG chat
└── search/          # Semantic search
```

## License

MIT
# ai-contract-backend
