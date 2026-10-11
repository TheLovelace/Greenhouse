-- CRUD D (Anahi): forma de pagamento obrigatoria e frete opcional.
-- Atualizacao de Estoque ao receber compra deve ser transacional no service.
CREATE TABLE compra_fornecedor (
    id BIGSERIAL PRIMARY KEY,
    funcionario_id BIGINT NOT NULL,
    fornecedor_id BIGINT NOT NULL,
    forma_pagamento_id BIGINT NOT NULL,
    data_compra TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PEDIDA',
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_frete NUMERIC(12,2),
    frete_incluso BOOLEAN NOT NULL DEFAULT FALSE,
    data_entrega_prevista DATE,
    observacao VARCHAR(1000),

    CONSTRAINT fk_compra_fornecedor_funcionario
        FOREIGN KEY (funcionario_id) REFERENCES funcionario(id),
    CONSTRAINT fk_compra_fornecedor_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id),
    CONSTRAINT fk_compra_fornecedor_forma_pagamento
        FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento(id),
    CONSTRAINT chk_compra_status
        CHECK (status IN ('PEDIDA', 'RECEBIDA', 'CANCELADA')),
    CONSTRAINT chk_compra_valor CHECK (valor_total >= 0),
    CONSTRAINT chk_compra_valor_frete
        CHECK (valor_frete IS NULL OR valor_frete >= 0)
);
CREATE INDEX idx_compra_fornecedor ON compra_fornecedor(fornecedor_id);
CREATE INDEX idx_compra_forma_pagamento ON compra_fornecedor(forma_pagamento_id);
