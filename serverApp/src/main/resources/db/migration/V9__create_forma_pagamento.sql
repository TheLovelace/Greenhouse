-- CRUD A (Luna): catalogo compartilhado entre Pedido e Compra de fornecedor.
-- Esta tabela NAO registra transacoes de pagamento.
CREATE TABLE forma_pagamento (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    descricao VARCHAR(250),
    limite_parcelas INTEGER NOT NULL DEFAULT 1,
    prazo_estorno_dias INTEGER,

    CONSTRAINT chk_forma_pagamento_limite_parcelas CHECK (limite_parcelas >= 1),
    CONSTRAINT chk_forma_pagamento_prazo_estorno CHECK (
        prazo_estorno_dias IS NULL OR prazo_estorno_dias >= 0
    )
);
