-- CRUD C (Amanda): configuracao de alertas vinculados a dispositivo e especie.
-- A efetiva verificacao das leituras e envio/exibicao de alertas cabe ao backend.
CREATE TABLE alerta (
    id BIGSERIAL PRIMARY KEY,
    dispositivo_iot_id BIGINT NOT NULL,
    especie_id BIGINT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensagem VARCHAR(1000),
    monitorar_temperatura BOOLEAN NOT NULL DEFAULT FALSE,
    monitorar_umidade_ar BOOLEAN NOT NULL DEFAULT FALSE,
    monitorar_umidade_solo BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    prioridade INTEGER NOT NULL DEFAULT 2,

    CONSTRAINT fk_alerta_dispositivo
        FOREIGN KEY (dispositivo_iot_id) REFERENCES dispositivo_iot(id),
    CONSTRAINT fk_alerta_especie
        FOREIGN KEY (especie_id) REFERENCES especie(id),
    CONSTRAINT chk_alerta_monitoramento
        CHECK (monitorar_temperatura OR monitorar_umidade_ar OR monitorar_umidade_solo),
    CONSTRAINT chk_alerta_prioridade CHECK (prioridade BETWEEN 1 AND 3)
);
