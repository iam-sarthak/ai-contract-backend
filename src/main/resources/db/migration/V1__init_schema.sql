CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE contracts (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    file_path    VARCHAR(500) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'UPLOADING',
    summary      TEXT,
    risk_score   INTEGER,
    uploaded_by  BIGINT NOT NULL REFERENCES users(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE contract_chunks (
    id          BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    chunk_text  TEXT NOT NULL,
    embedding   VECTOR(768),
    metadata    JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE clauses (
    id          BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    type        VARCHAR(50) NOT NULL,
    text        TEXT NOT NULL,
    confidence  DOUBLE PRECISION NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE risks (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    severity        VARCHAR(20) NOT NULL,
    description     TEXT NOT NULL,
    recommendation  TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_history (
    id          BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    question    TEXT NOT NULL,
    answer      TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE contract_comparisons (
    id              BIGSERIAL PRIMARY KEY,
    contract_a_id   BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    contract_b_id   BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    report          TEXT NOT NULL,
    created_by      BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contracts_uploaded_by ON contracts(uploaded_by);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contract_chunks_contract_id ON contract_chunks(contract_id);
CREATE INDEX idx_clauses_contract_id ON clauses(contract_id);
CREATE INDEX idx_risks_contract_id ON risks(contract_id);
CREATE INDEX idx_chat_history_contract_id ON chat_history(contract_id);

CREATE INDEX idx_contract_chunks_embedding ON contract_chunks
    USING hnsw (embedding vector_cosine_ops);
