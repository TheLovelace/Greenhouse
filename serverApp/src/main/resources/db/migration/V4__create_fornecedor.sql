-- CRUD D (Anahi): fornecedor; CNPJ opcional, 14 digitos sem pontuacao.
CREATE TABLE fornecedor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(150),
    contato VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    prazo_medio_entrega_dias INTEGER,

    CONSTRAINT chk_fornecedor_cnpj_formato
        CHECK (cnpj IS NULL OR cnpj ~ '^[0-9]{14}$'),
    CONSTRAINT chk_fornecedor_prazo_entrega
        CHECK (prazo_medio_entrega_dias IS NULL OR prazo_medio_entrega_dias >= 0)
);
