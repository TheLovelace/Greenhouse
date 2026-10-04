CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    funcionario_id BIGINT NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_usuario_funcionario
        FOREIGN KEY (funcionario_id)
        REFERENCES funcionario(id),

    CONSTRAINT chk_usuario_perfil
        CHECK (perfil IN ('ADMIN', 'FUNCIONARIO'))
);