-- AUX D: itens gerenciados dentro do CRUD Compra de fornecedor.
CREATE TABLE compra_item (
    id BIGSERIAL PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_compra_item_compra
        FOREIGN KEY (compra_id) REFERENCES compra_fornecedor(id) ON DELETE CASCADE,
    CONSTRAINT fk_compra_item_produto
        FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT chk_compra_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_compra_item_valor CHECK (valor_unitario >= 0)
);
CREATE INDEX idx_compra_item_produto ON compra_item(produto_id);
