CREATE TABLE fornecedor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(150),
    contato VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_fornecedor_cnpj_formato
        CHECK (cnpj IS NULL OR cnpj ~ '^[0-9]{14}$')
);