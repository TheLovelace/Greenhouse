-- CRUD B (Isabella): prazo de PREPARO em minutos (duracao, nao horario TIME).
CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE,
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(1000),
    preco NUMERIC(12,2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    prazo_preparo_minutos INTEGER,

    CONSTRAINT chk_produto_preco CHECK (preco >= 0),
    CONSTRAINT chk_produto_prazo_preparo
        CHECK (prazo_preparo_minutos IS NULL OR prazo_preparo_minutos >= 0)
);
CREATE INDEX idx_produto_nome ON produto(nome);
