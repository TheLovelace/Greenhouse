CREATE TABLE funcionario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(150) UNIQUE,
    cargo VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_funcionario_cpf_formato
        CHECK (cpf ~ '^[0-9]{11}$')
);