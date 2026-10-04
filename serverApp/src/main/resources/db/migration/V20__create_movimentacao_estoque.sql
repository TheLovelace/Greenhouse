CREATE TABLE movimentacao_estoque (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    pedido_id BIGINT,
    compra_id BIGINT,
    funcionario_id BIGINT,
    tipo VARCHAR(30) NOT NULL,
    quantidade INTEGER NOT NULL,
    observacao TEXT,
    data_hora TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_movimentacao_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id),

    CONSTRAINT fk_movimentacao_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedido(id),

    CONSTRAINT fk_movimentacao_compra
        FOREIGN KEY (compra_id)
        REFERENCES compra_fornecedor(id),

    CONSTRAINT fk_movimentacao_funcionario
        FOREIGN KEY (funcionario_id)
        REFERENCES funcionario(id),

    CONSTRAINT chk_movimentacao_tipo
        CHECK (
            tipo IN (
                'ENTRADA',
                'SAIDA',
                'AJUSTE_ENTRADA',
                'AJUSTE_SAIDA',
                'PERDA'
            )
        ),

    CONSTRAINT chk_movimentacao_quantidade
        CHECK (quantidade > 0)
);

CREATE INDEX idx_movimentacao_produto
    ON movimentacao_estoque(produto_id);

CREATE INDEX idx_movimentacao_data
    ON movimentacao_estoque(data_hora);