CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT,
    funcionario_id BIGINT NOT NULL,
    desconto_id BIGINT,

    modalidade VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',

    data_pedido TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    data_retirada_prevista TIMESTAMPTZ,
    data_retirada TIMESTAMPTZ,

    valor_desconto NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,

    observacao TEXT,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id),

    CONSTRAINT fk_pedido_funcionario
        FOREIGN KEY (funcionario_id)
        REFERENCES funcionario(id),

    CONSTRAINT fk_pedido_desconto
        FOREIGN KEY (desconto_id)
        REFERENCES desconto(id),

    CONSTRAINT chk_pedido_modalidade
        CHECK (modalidade IN ('IMEDIATA', 'AGENDADA')),

    CONSTRAINT chk_pedido_agendamento
        CHECK (
            modalidade <> 'AGENDADA'
            OR data_retirada_prevista IS NOT NULL
        ),

    CONSTRAINT chk_pedido_status
        CHECK (
            status IN (
                'ABERTO',
                'CONFIRMADO',
                'PRONTO_RETIRADA',
                'CONCLUIDO',
                'CANCELADO'
            )
        ),

    CONSTRAINT chk_pedido_valor_desconto
        CHECK (valor_desconto >= 0),

    CONSTRAINT chk_pedido_valor_total
        CHECK (valor_total >= 0)
);

CREATE INDEX idx_pedido_cliente
    ON pedido(cliente_id);

CREATE INDEX idx_pedido_funcionario
    ON pedido(funcionario_id);

CREATE INDEX idx_pedido_status
    ON pedido(status);

CREATE INDEX idx_pedido_data
    ON pedido(data_pedido);

CREATE INDEX idx_pedido_retirada
    ON pedido(data_retirada_prevista);