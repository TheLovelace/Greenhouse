-- Calibração inicial do sketch. Preserva a calibração e o estado de um cadastro existente.
INSERT INTO dispositivo_iot (codigo, nome, localizacao, topico_mqtt, calibracao_seco, calibracao_umido, ativo)
VALUES ('GH-ESP32-ESTUFA1', 'ESP32 Estufa 1', 'Estufa 1', 'greenhouse/estufa1/leitura', 3200, 1200, TRUE)
ON CONFLICT (topico_mqtt) DO NOTHING;
