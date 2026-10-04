CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    whatsapp BOOLEAN NOT NULL DEFAULT FALSE,
    cpf VARCHAR(11) UNIQUE,
    email VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_cliente_cpf_formato
        CHECK (cpf IS NULL OR cpf ~ '^[0-9]{11}$')
);