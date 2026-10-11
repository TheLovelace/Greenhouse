-- AUX C: leitura automatica recebida do dispositivo. Sem formulario CRUD proprio.
CREATE TABLE leitura_iot (
    id BIGSERIAL PRIMARY KEY,
    dispositivo_iot_id BIGINT NOT NULL,
    temperatura NUMERIC(5,2) NOT NULL,
    umidade_ar NUMERIC(5,2) NOT NULL,
    umidade_solo NUMERIC(5,2) NOT NULL,
    umidade_solo_raw INTEGER NOT NULL,
    data_leitura TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_leitura_dispositivo
        FOREIGN KEY (dispositivo_iot_id) REFERENCES dispositivo_iot(id) ON DELETE CASCADE,
    CONSTRAINT chk_leitura_umidade_ar CHECK (umidade_ar BETWEEN 0 AND 100),
    CONSTRAINT chk_leitura_umidade_solo CHECK (umidade_solo BETWEEN 0 AND 100),
    CONSTRAINT chk_leitura_umidade_solo_raw CHECK (umidade_solo_raw >= 0)
);
CREATE INDEX idx_leitura_dispositivo ON leitura_iot(dispositivo_iot_id);
CREATE INDEX idx_leitura_data ON leitura_iot(data_leitura);
