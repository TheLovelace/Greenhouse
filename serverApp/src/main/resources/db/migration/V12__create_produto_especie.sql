-- AUX B/C: relacionamento N:N de produtos com especies de plantas.
CREATE TABLE produto_especie (
    produto_id BIGINT NOT NULL,
    especie_id BIGINT NOT NULL,

    PRIMARY KEY (produto_id, especie_id),
    CONSTRAINT fk_produto_especie_produto
        FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE,
    CONSTRAINT fk_produto_especie_especie
        FOREIGN KEY (especie_id) REFERENCES especie(id) ON DELETE CASCADE
);
