-- CRUD E (Julia): uma unica forma de pagamento por pedido.
-- Cancelamento, reserva e baixa de estoque sao tratados no service.
CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT,
    funcionario_id BIGINT NOT NULL,
    desconto_id BIGINT,
    forma_pagamento_id BIGINT NOT NULL,
    modalidade VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    data_pedido TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    data_retirada_prevista TIMESTAMPTZ,
    data_retirada TIMESTAMPTZ,
    valor_desconto NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    observacao VARCHAR(1000),
    embalagem_presente BOOLEAN NOT NULL DEFAULT FALSE,
    numero_volumes INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionario(id),
    CONSTRAINT fk_pedido_desconto FOREIGN KEY (desconto_id) REFERENCES desconto(id),
    CONSTRAINT fk_pedido_forma_pagamento
        FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento(id),
    CONSTRAINT chk_pedido_modalidade
        CHECK (modalidade IN ('IMEDIATA', 'AGENDADA')),
    CONSTRAINT chk_pedido_agendamento
        CHECK (modalidade <> 'AGENDADA' OR data_retirada_prevista IS NOT NULL),
    CONSTRAINT chk_pedido_status
        CHECK (status IN ('ABERTO', 'CONFIRMADO', 'PRONTO_RETIRADA',
                         'CONCLUIDO', 'CANCELADO')),
    CONSTRAINT chk_pedido_valor_desconto CHECK (valor_desconto >= 0),
    CONSTRAINT chk_pedido_valor_total CHECK (valor_total >= 0),
    CONSTRAINT chk_pedido_numero_volumes CHECK (numero_volumes >= 1)
);
CREATE INDEX idx_pedido_cliente ON pedido(cliente_id);
CREATE INDEX idx_pedido_forma_pagamento ON pedido(forma_pagamento_id);
CREATE INDEX idx_pedido_funcionario ON pedido(funcionario_id);
CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_pedido_data ON pedido(data_pedido);
CREATE INDEX idx_pedido_retirada ON pedido(data_retirada_prevista);
