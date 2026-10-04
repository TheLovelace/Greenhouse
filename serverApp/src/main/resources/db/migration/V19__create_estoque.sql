CREATE TABLE estoque (
    produto_id BIGINT PRIMARY KEY,
    quantidade_fisica INTEGER NOT NULL DEFAULT 0,
    quantidade_reservada INTEGER NOT NULL DEFAULT 0,
    quantidade_minima INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_estoque_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id),

    CONSTRAINT chk_estoque_fisico
        CHECK (quantidade_fisica >= 0),

    CONSTRAINT chk_estoque_reservado
        CHECK (quantidade_reservada >= 0),

    CONSTRAINT chk_estoque_minimo
        CHECK (quantidade_minima >= 0),

    CONSTRAINT chk_estoque_reserva
        CHECK (quantidade_reservada <= quantidade_fisica)
);