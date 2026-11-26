CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expira_em TIMESTAMPTZ NOT NULL,
    revogado_em TIMESTAMPTZ,

    CONSTRAINT fk_refresh_token_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_expira_em ON refresh_token(expira_em);
