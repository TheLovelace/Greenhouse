-- CRUD B (Isabella): categoria como etiqueta/tag de busca.
-- Data gerada por padrao, mas pode ser informada/editada pelo formulario.
CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(1000) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    observacao VARCHAR(300),
    data_cadastro DATE NOT NULL DEFAULT CURRENT_DATE
);
