CREATE TABLE pagamento (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    forma_pagamento_id BIGINT NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_pagamento TIMESTAMPTZ,

    CONSTRAINT fk_pagamento_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedido(id),

    CONSTRAINT fk_pagamento_forma_pagamento
        FOREIGN KEY (forma_pagamento_id)
        REFERENCES forma_pagamento(id),

    CONSTRAINT chk_pagamento_valor
        CHECK (valor > 0),

    CONSTRAINT chk_pagamento_status
        CHECK (status IN ('PENDENTE', 'PAGO', 'CANCELADO'))
);

CREATE INDEX idx_pagamento_pedido
    ON pagamento(pedido_id);