-- AUX C: cadastro tecnico dos dispositivos para receber leituras e configurar alertas.
CREATE TABLE dispositivo_iot (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(150) NOT NULL,
    localizacao VARCHAR(200),
    topico_mqtt VARCHAR(255) NOT NULL UNIQUE,
    calibracao_seco INTEGER,
    calibracao_umido INTEGER,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);
