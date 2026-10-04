CREATE TABLE imagem (
    id BIGSERIAL PRIMARY KEY,
    especie_id BIGINT,
    produto_id BIGINT,
    cloudinary_public_id TEXT NOT NULL,
    url_imagem TEXT NOT NULL,
    ordem INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_imagem_especie
        FOREIGN KEY (especie_id)
        REFERENCES especie(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_imagem_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_imagem_origem
        CHECK (
            (especie_id IS NOT NULL AND produto_id IS NULL)
            OR
            (especie_id IS NULL AND produto_id IS NOT NULL)
        ),

    CONSTRAINT chk_imagem_ordem
        CHECK (ordem > 0)
);

CREATE INDEX idx_imagem_produto
    ON imagem(produto_id);

CREATE INDEX idx_imagem_especie
    ON imagem(especie_id);