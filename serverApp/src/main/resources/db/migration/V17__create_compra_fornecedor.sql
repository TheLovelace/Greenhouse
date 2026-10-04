CREATE TABLE compra_fornecedor (
    id BIGSERIAL PRIMARY KEY,
    funcionario_id BIGINT NOT NULL,
    fornecedor_id BIGINT NOT NULL,
    data_compra TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PEDIDA',
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    observacao TEXT,

    CONSTRAINT fk_compra_fornecedor_funcionario
        FOREIGN KEY (funcionario_id)
        REFERENCES funcionario(id),

    CONSTRAINT fk_compra_fornecedor_fornecedor
        FOREIGN KEY (fornecedor_id)
        REFERENCES fornecedor(id),

    CONSTRAINT chk_compra_status
        CHECK (status IN ('PEDIDA', 'RECEBIDA', 'CANCELADA')),

    CONSTRAINT chk_compra_valor
        CHECK (valor_total >= 0)
);

CREATE INDEX idx_compra_fornecedor
    ON compra_fornecedor(fornecedor_id);