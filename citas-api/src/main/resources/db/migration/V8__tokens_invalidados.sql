-- Tokens revocados por logout. Se guarda el hash (no el token) y su expiración para poder purgarlos.
CREATE TABLE tokens_invalidados (
    token_hash VARCHAR(64) PRIMARY KEY,
    expira_en  TIMESTAMPTZ NOT NULL
);
