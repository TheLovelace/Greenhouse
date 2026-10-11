-- AUX E: itens do pedido, gerenciados no fluxo do CRUD Pedido.
CREATE TABLE pedido_item (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_pedido_item_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_item_produto
        FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT chk_pedido_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_pedido_item_preco CHECK (preco_unitario >= 0)
);
CREATE INDEX idx_pedido_item_pedido ON pedido_item(pedido_id);
CREATE INDEX idx_pedido_item_produto ON pedido_item(produto_id);
