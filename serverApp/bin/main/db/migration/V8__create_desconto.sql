CREATE TABLE desconto (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    data_inicio TIMESTAMPTZ,
    data_fim TIMESTAMPTZ,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_desconto_tipo
        CHECK (tipo IN ('PERCENTUAL', 'VALOR_FIXO')),

    CONSTRAINT chk_desconto_valor
        CHECK (
            (tipo = 'PERCENTUAL' AND valor > 0 AND valor <= 100)
            OR
            (tipo = 'VALOR_FIXO' AND valor > 0)
        ),

    CONSTRAINT chk_desconto_periodo
        CHECK (
            data_inicio IS NULL
            OR data_fim IS NULL
            OR data_fim >= data_inicio
        )
);