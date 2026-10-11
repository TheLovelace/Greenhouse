-- CRUD C (Amanda): especie e parametros de referencia para as leituras de IoT.
CREATE TABLE especie (
    id BIGSERIAL PRIMARY KEY,
    nome_popular VARCHAR(150) NOT NULL,
    nome_cientifico VARCHAR(150),
    descricao VARCHAR(2000),
    temperatura_min NUMERIC(5,2),
    temperatura_max NUMERIC(5,2),
    umidade_ar_min NUMERIC(5,2),
    umidade_ar_max NUMERIC(5,2),
    umidade_solo_min NUMERIC(5,2),
    umidade_solo_max NUMERIC(5,2),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    intervalo_rega_dias INTEGER,

    CONSTRAINT chk_especie_temperatura CHECK (
        temperatura_min IS NULL OR temperatura_max IS NULL
        OR temperatura_min <= temperatura_max
    ),
    CONSTRAINT chk_especie_umidade_ar CHECK (
        (umidade_ar_min IS NULL OR umidade_ar_min BETWEEN 0 AND 100)
        AND (umidade_ar_max IS NULL OR umidade_ar_max BETWEEN 0 AND 100)
        AND (umidade_ar_min IS NULL OR umidade_ar_max IS NULL
             OR umidade_ar_min <= umidade_ar_max)
    ),
    CONSTRAINT chk_especie_umidade_solo CHECK (
        (umidade_solo_min IS NULL OR umidade_solo_min BETWEEN 0 AND 100)
        AND (umidade_solo_max IS NULL OR umidade_solo_max BETWEEN 0 AND 100)
        AND (umidade_solo_min IS NULL OR umidade_solo_max IS NULL
             OR umidade_solo_min <= umidade_solo_max)
    ),
    CONSTRAINT chk_especie_intervalo_rega CHECK (
        intervalo_rega_dias IS NULL OR intervalo_rega_dias >= 1
    )
);
