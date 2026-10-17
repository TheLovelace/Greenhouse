-- AUX B: relacionamento N:N de produtos com etiquetas/categorias.
CREATE TABLE produto_categoria (
    produto_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,

    PRIMARY KEY (produto_id, categoria_id),
    CONSTRAINT fk_produto_categoria_produto
        FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE,
    CONSTRAINT fk_produto_categoria_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE CASCADE
);
CREATE INDEX idx_produto_categoria_categoria ON produto_categoria(categoria_id);
