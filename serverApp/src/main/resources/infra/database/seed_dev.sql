-- Greenhouse - Seed de desenvolvimento
-- Executar somente em ambiente de desenvolvimento/teste.
-- Idempotente: pode ser executado novamente sem duplicar os registros deste seed.

BEGIN;

-- 1. Categoria
INSERT INTO categoria (nome, descricao, ativo)
VALUES
    ('Plantas Ornamentais', 'Plantas destinadas à decoração de ambientes.', TRUE),
    ('Vasos', 'Vasos e recipientes para cultivo.', TRUE),
    ('Substratos', 'Substratos e materiais para cultivo.', TRUE)
ON CONFLICT DO NOTHING;

-- 2. Funcionário
INSERT INTO funcionario (nome, cpf, telefone, email, cargo, ativo)
VALUES
    ('Ana Admin', '11111111111', '11999990001', 'ana.admin@greenhouse.dev', 'Administradora', TRUE),
    ('Bruna Vendas', '22222222222', '11999990002', 'bruna.vendas@greenhouse.dev', 'Vendedora', TRUE),
    ('Carlos Estoque', '33333333333', '11999990003', 'carlos.estoque@greenhouse.dev', 'Estoquista', TRUE)
ON CONFLICT DO NOTHING;

-- 3. Usuário
-- senha_hash contém valores de seed apenas para popular a tabela.
-- Substitua por hashes válidos do mecanismo de autenticação quando o login for implementado/testado.
INSERT INTO usuario (funcionario_id, login, senha_hash, perfil, ativo, primeiro_acesso_feito)
SELECT f.id, v.login, v.senha_hash, v.perfil, TRUE, TRUE
FROM (
    VALUES
        ('11111111111', 'ana.admin', 'SEED_DEV_HASH_ANA', 'ADMIN'),
        ('22222222222', 'bruna.vendas', 'SEED_DEV_HASH_BRUNA', 'FUNCIONARIO'),
        ('33333333333', 'carlos.estoque', 'SEED_DEV_HASH_CARLOS', 'FUNCIONARIO')
) AS v(cpf, login, senha_hash, perfil)
JOIN funcionario f ON f.cpf = v.cpf
ON CONFLICT DO NOTHING;

-- 4. Cliente
INSERT INTO cliente (nome, telefone, whatsapp, cpf, email, ativo)
VALUES
    ('Marina Souza', '11988880001', TRUE, '44444444444', 'marina@cliente.dev', TRUE),
    ('Rafael Lima', '11988880002', TRUE, '55555555555', 'rafael@cliente.dev', TRUE),
    ('Camila Rocha', '11988880003', FALSE, '66666666666', 'camila@cliente.dev', TRUE)
ON CONFLICT DO NOTHING;

-- 5. Fornecedor
INSERT INTO fornecedor (nome, cnpj, telefone, email, contato, ativo)
VALUES
    ('Flora Distribuidora', '11111111000111', '1130001001', 'contato@flora.dev', 'Fernanda', TRUE),
    ('Verde Atacado', '22222222000122', '1130001002', 'contato@verde.dev', 'Ricardo', TRUE),
    ('Jardim Suprimentos', '33333333000133', '1130001003', 'contato@jardim.dev', 'Paula', TRUE)
ON CONFLICT DO NOTHING;

-- 6. Produto
INSERT INTO produto (codigo, nome, descricao, preco, ativo)
VALUES
    ('PROD-001', 'Orquídea Phalaenopsis', 'Orquídea ornamental em vaso.', 79.90, TRUE),
    ('PROD-002', 'Vaso Cerâmico Médio', 'Vaso cerâmico para plantas de pequeno e médio porte.', 39.90, TRUE),
    ('PROD-003', 'Substrato Universal 5L', 'Substrato pronto para uso geral.', 24.50, TRUE),
    ('PROD-004', 'Samambaia Americana', 'Samambaia ornamental para ambientes internos.', 49.90, TRUE)
ON CONFLICT DO NOTHING;

-- 7. Espécie
INSERT INTO especie (
    nome_popular, nome_cientifico, descricao,
    temperatura_min, temperatura_max,
    umidade_ar_min, umidade_ar_max,
    umidade_solo_min, umidade_solo_max,
    ativo
)
SELECT *
FROM (
    VALUES
        ('Orquídea Phalaenopsis', 'Phalaenopsis spp.', 'Orquídea ornamental tropical.', 18.00, 30.00, 50.00, 80.00, 40.00, 70.00, TRUE),
        ('Samambaia Americana', 'Nephrolepis exaltata', 'Samambaia de folhagem ornamental.', 16.00, 28.00, 55.00, 90.00, 45.00, 80.00, TRUE),
        ('Jiboia', 'Epipremnum aureum', 'Planta ornamental de fácil manutenção.', 18.00, 30.00, 40.00, 80.00, 35.00, 70.00, TRUE)
) AS v(
    nome_popular, nome_cientifico, descricao,
    temperatura_min, temperatura_max,
    umidade_ar_min, umidade_ar_max,
    umidade_solo_min, umidade_solo_max,
    ativo
)
WHERE NOT EXISTS (
    SELECT 1
    FROM especie e
    WHERE e.nome_popular = v.nome_popular
      AND COALESCE(e.nome_cientifico, '') = COALESCE(v.nome_cientifico, '')
);

-- 8. Desconto
INSERT INTO desconto (titulo, descricao, tipo, valor, data_inicio, data_fim, ativo)
SELECT *
FROM (
    VALUES
        ('Boas-vindas 10%', 'Desconto de desenvolvimento para novos clientes.', 'PERCENTUAL', 10.00, TIMESTAMPTZ '2026-01-01 00:00:00-03', TIMESTAMPTZ '2027-12-31 23:59:59-03', TRUE),
        ('Desconto R$ 15', 'Desconto fixo para testes.', 'VALOR_FIXO', 15.00, TIMESTAMPTZ '2026-01-01 00:00:00-03', TIMESTAMPTZ '2027-12-31 23:59:59-03', TRUE),
        ('Promoção 5%', 'Desconto percentual para testes de pedido.', 'PERCENTUAL', 5.00, TIMESTAMPTZ '2026-01-01 00:00:00-03', TIMESTAMPTZ '2027-12-31 23:59:59-03', TRUE)
) AS v(titulo, descricao, tipo, valor, data_inicio, data_fim, ativo)
WHERE NOT EXISTS (
    SELECT 1 FROM desconto d WHERE d.titulo = v.titulo
);

-- 9. Forma de pagamento
INSERT INTO forma_pagamento (nome, ativo)
VALUES
    ('PIX', TRUE),
    ('Cartão de Crédito', TRUE),
    ('Cartão de Débito', TRUE),
    ('Dinheiro', TRUE)
ON CONFLICT DO NOTHING;

-- 10. Dispositivo IoT
INSERT INTO dispositivo_iot (
    codigo, nome, localizacao, topico_mqtt,
    calibracao_seco, calibracao_umido, ativo
)
VALUES
    ('ESP32-001', 'Sensor Estufa 1', 'Bancada A', 'greenhouse/estufa1/sensores', 3200, 1400, TRUE),
    ('ESP32-002', 'Sensor Estufa 2', 'Bancada B', 'greenhouse/estufa2/sensores', 3150, 1450, TRUE),
    ('ESP32-003', 'Sensor Estufa 3', 'Bancada C', 'greenhouse/estufa3/sensores', 3250, 1500, TRUE)
ON CONFLICT DO NOTHING;

-- 11. Produto x Categoria
INSERT INTO produto_categoria (produto_id, categoria_id)
SELECT p.id, c.id
FROM (
    VALUES
        ('PROD-001', 'Plantas Ornamentais'),
        ('PROD-002', 'Vasos'),
        ('PROD-003', 'Substratos'),
        ('PROD-004', 'Plantas Ornamentais')
) AS v(codigo_produto, nome_categoria)
JOIN produto p ON p.codigo = v.codigo_produto
JOIN categoria c ON c.nome = v.nome_categoria
ON CONFLICT DO NOTHING;

-- 12. Produto x Espécie
INSERT INTO produto_especie (produto_id, especie_id)
SELECT p.id, e.id
FROM (
    VALUES
        ('PROD-001', 'Orquídea Phalaenopsis'),
        ('PROD-004', 'Samambaia Americana')
) AS v(codigo_produto, nome_especie)
JOIN produto p ON p.codigo = v.codigo_produto
JOIN especie e ON e.nome_popular = v.nome_especie
ON CONFLICT DO NOTHING;

-- 13. Imagem
INSERT INTO imagem (especie_id, produto_id, cloudinary_public_id, url_imagem, ordem)
SELECT e.id, NULL, v.public_id, v.url, v.ordem
FROM (
    VALUES
        ('Orquídea Phalaenopsis', 'seed/especie-orquidea', 'https://res.cloudinary.com/demo/image/upload/sample.jpg', 1),
        ('Samambaia Americana', 'seed/especie-samambaia', 'https://res.cloudinary.com/demo/image/upload/sample.jpg', 1)
) AS v(nome_especie, public_id, url, ordem)
JOIN especie e ON e.nome_popular = v.nome_especie
WHERE NOT EXISTS (
    SELECT 1 FROM imagem i WHERE i.cloudinary_public_id = v.public_id
);

INSERT INTO imagem (especie_id, produto_id, cloudinary_public_id, url_imagem, ordem)
SELECT NULL, p.id, v.public_id, v.url, v.ordem
FROM (
    VALUES
        ('PROD-001', 'seed/produto-orquidea', 'https://res.cloudinary.com/demo/image/upload/sample.jpg', 1),
        ('PROD-002', 'seed/produto-vaso', 'https://res.cloudinary.com/demo/image/upload/sample.jpg', 1)
) AS v(codigo_produto, public_id, url, ordem)
JOIN produto p ON p.codigo = v.codigo_produto
WHERE NOT EXISTS (
    SELECT 1 FROM imagem i WHERE i.cloudinary_public_id = v.public_id
);

-- 14. Pedido
INSERT INTO pedido (
    cliente_id, funcionario_id, desconto_id,
    modalidade, status,
    data_pedido, data_retirada_prevista, data_retirada,
    valor_desconto, valor_total, observacao
)
SELECT
    c.id,
    f.id,
    d.id,
    v.modalidade,
    v.status,
    v.data_pedido,
    v.data_retirada_prevista,
    v.data_retirada,
    v.valor_desconto,
    v.valor_total,
    v.observacao
FROM (
    VALUES
        ('44444444444', '22222222222', 'Boas-vindas 10%', 'IMEDIATA', 'CONCLUIDO', TIMESTAMPTZ '2026-10-01 10:00:00-03', NULL::timestamptz, TIMESTAMPTZ '2026-10-01 10:30:00-03', 7.99, 71.91, 'SEED_DEV_PEDIDO_1'),
        ('55555555555', '22222222222', NULL, 'AGENDADA', 'CONFIRMADO', TIMESTAMPTZ '2026-10-02 14:00:00-03', TIMESTAMPTZ '2026-10-05 15:00:00-03', NULL::timestamptz, 0.00, 64.40, 'SEED_DEV_PEDIDO_2'),
        ('66666666666', '11111111111', 'Promoção 5%', 'AGENDADA', 'ABERTO', TIMESTAMPTZ '2026-10-03 09:00:00-03', TIMESTAMPTZ '2026-10-06 11:00:00-03', NULL::timestamptz, 2.50, 47.40, 'SEED_DEV_PEDIDO_3')
) AS v(
    cpf_cliente, cpf_funcionario, titulo_desconto,
    modalidade, status, data_pedido, data_retirada_prevista, data_retirada,
    valor_desconto, valor_total, observacao
)
JOIN cliente c ON c.cpf = v.cpf_cliente
JOIN funcionario f ON f.cpf = v.cpf_funcionario
LEFT JOIN desconto d ON d.titulo = v.titulo_desconto
WHERE NOT EXISTS (
    SELECT 1 FROM pedido p WHERE p.observacao = v.observacao
);

-- 15. Itens do pedido
INSERT INTO pedido_item (pedido_id, produto_id, quantidade, preco_unitario)
SELECT pe.id, pr.id, v.quantidade, v.preco_unitario
FROM (
    VALUES
        ('SEED_DEV_PEDIDO_1', 'PROD-001', 1, 79.90),
        ('SEED_DEV_PEDIDO_2', 'PROD-002', 1, 39.90),
        ('SEED_DEV_PEDIDO_2', 'PROD-003', 1, 24.50),
        ('SEED_DEV_PEDIDO_3', 'PROD-004', 1, 49.90)
) AS v(observacao_pedido, codigo_produto, quantidade, preco_unitario)
JOIN pedido pe ON pe.observacao = v.observacao_pedido
JOIN produto pr ON pr.codigo = v.codigo_produto
WHERE NOT EXISTS (
    SELECT 1
    FROM pedido_item pi
    WHERE pi.pedido_id = pe.id
      AND pi.produto_id = pr.id
      AND pi.quantidade = v.quantidade
      AND pi.preco_unitario = v.preco_unitario
);

-- 16. Pagamento
INSERT INTO pagamento (pedido_id, forma_pagamento_id, valor, status, data_pagamento)
SELECT pe.id, fp.id, v.valor, v.status, v.data_pagamento
FROM (
    VALUES
        ('SEED_DEV_PEDIDO_1', 'PIX', 71.91, 'PAGO', TIMESTAMPTZ '2026-10-01 10:10:00-03'),
        ('SEED_DEV_PEDIDO_2', 'Cartão de Crédito', 64.40, 'PENDENTE', NULL::timestamptz),
        ('SEED_DEV_PEDIDO_3', 'Dinheiro', 47.40, 'PENDENTE', NULL::timestamptz)
) AS v(observacao_pedido, forma_pagamento, valor, status, data_pagamento)
JOIN pedido pe ON pe.observacao = v.observacao_pedido
JOIN forma_pagamento fp ON fp.nome = v.forma_pagamento
WHERE NOT EXISTS (
    SELECT 1
    FROM pagamento pg
    WHERE pg.pedido_id = pe.id
      AND pg.forma_pagamento_id = fp.id
      AND pg.valor = v.valor
);

-- 17. Compra com fornecedor
INSERT INTO compra_fornecedor (
    funcionario_id, fornecedor_id, data_compra, status, valor_total, observacao
)
SELECT f.id, fo.id, v.data_compra, v.status, v.valor_total, v.observacao
FROM (
    VALUES
        ('33333333333', '11111111000111', TIMESTAMPTZ '2026-09-28 09:00:00-03', 'RECEBIDA', 300.00, 'SEED_DEV_COMPRA_1'),
        ('33333333333', '22222222000122', TIMESTAMPTZ '2026-09-29 10:00:00-03', 'PEDIDA', 180.00, 'SEED_DEV_COMPRA_2'),
        ('11111111111', '33333333000133', TIMESTAMPTZ '2026-09-30 15:00:00-03', 'RECEBIDA', 250.00, 'SEED_DEV_COMPRA_3')
) AS v(cpf_funcionario, cnpj_fornecedor, data_compra, status, valor_total, observacao)
JOIN funcionario f ON f.cpf = v.cpf_funcionario
JOIN fornecedor fo ON fo.cnpj = v.cnpj_fornecedor
WHERE NOT EXISTS (
    SELECT 1 FROM compra_fornecedor cf WHERE cf.observacao = v.observacao
);

-- 18. Itens da compra
INSERT INTO compra_item (compra_id, produto_id, quantidade, valor_unitario)
SELECT cf.id, p.id, v.quantidade, v.valor_unitario
FROM (
    VALUES
        ('SEED_DEV_COMPRA_1', 'PROD-001', 5, 40.00),
        ('SEED_DEV_COMPRA_1', 'PROD-004', 5, 20.00),
        ('SEED_DEV_COMPRA_2', 'PROD-002', 10, 18.00),
        ('SEED_DEV_COMPRA_3', 'PROD-003', 20, 12.50)
) AS v(observacao_compra, codigo_produto, quantidade, valor_unitario)
JOIN compra_fornecedor cf ON cf.observacao = v.observacao_compra
JOIN produto p ON p.codigo = v.codigo_produto
WHERE NOT EXISTS (
    SELECT 1
    FROM compra_item ci
    WHERE ci.compra_id = cf.id
      AND ci.produto_id = p.id
      AND ci.quantidade = v.quantidade
      AND ci.valor_unitario = v.valor_unitario
);

-- 19. Estoque
INSERT INTO estoque (produto_id, quantidade_fisica, quantidade_reservada, quantidade_minima)
SELECT p.id, v.quantidade_fisica, v.quantidade_reservada, v.quantidade_minima
FROM (
    VALUES
        ('PROD-001', 12, 2, 3),
        ('PROD-002', 20, 4, 5),
        ('PROD-003', 35, 5, 10),
        ('PROD-004', 10, 1, 3)
) AS v(codigo_produto, quantidade_fisica, quantidade_reservada, quantidade_minima)
JOIN produto p ON p.codigo = v.codigo_produto
ON CONFLICT (produto_id) DO NOTHING;

-- 20. Movimentação de estoque
INSERT INTO movimentacao_estoque (
    produto_id, pedido_id, compra_id, funcionario_id,
    tipo, quantidade, observacao, data_hora
)
SELECT
    p.id,
    pe.id,
    cf.id,
    f.id,
    v.tipo,
    v.quantidade,
    v.observacao,
    v.data_hora
FROM (
    VALUES
        ('PROD-001', NULL, 'SEED_DEV_COMPRA_1', '33333333333', 'ENTRADA', 5, 'SEED_DEV_MOV_1', TIMESTAMPTZ '2026-09-28 11:00:00-03'),
        ('PROD-004', NULL, 'SEED_DEV_COMPRA_1', '33333333333', 'ENTRADA', 5, 'SEED_DEV_MOV_2', TIMESTAMPTZ '2026-09-28 11:05:00-03'),
        ('PROD-001', 'SEED_DEV_PEDIDO_1', NULL, '22222222222', 'SAIDA', 1, 'SEED_DEV_MOV_3', TIMESTAMPTZ '2026-10-01 10:30:00-03'),
        ('PROD-003', NULL, NULL, '33333333333', 'AJUSTE_ENTRADA', 2, 'SEED_DEV_MOV_4', TIMESTAMPTZ '2026-10-02 08:00:00-03')
) AS v(codigo_produto, observacao_pedido, observacao_compra, cpf_funcionario, tipo, quantidade, observacao, data_hora)
JOIN produto p ON p.codigo = v.codigo_produto
JOIN funcionario f ON f.cpf = v.cpf_funcionario
LEFT JOIN pedido pe ON pe.observacao = v.observacao_pedido
LEFT JOIN compra_fornecedor cf ON cf.observacao = v.observacao_compra
WHERE NOT EXISTS (
    SELECT 1 FROM movimentacao_estoque me WHERE me.observacao = v.observacao
);

-- 21. Leituras IoT
INSERT INTO leitura_iot (
    dispositivo_iot_id, temperatura, umidade_ar, umidade_solo, umidade_solo_raw, data_leitura
)
SELECT d.id, v.temperatura, v.umidade_ar, v.umidade_solo, v.umidade_solo_raw, v.data_leitura
FROM (
    VALUES
        ('ESP32-001', 24.50, 65.00, 58.00, 2100, TIMESTAMPTZ '2026-10-04 08:00:00-03'),
        ('ESP32-002', 27.10, 59.50, 46.00, 2350, TIMESTAMPTZ '2026-10-04 08:05:00-03'),
        ('ESP32-003', 22.80, 72.00, 67.00, 1900, TIMESTAMPTZ '2026-10-04 08:10:00-03')
) AS v(codigo_dispositivo, temperatura, umidade_ar, umidade_solo, umidade_solo_raw, data_leitura)
JOIN dispositivo_iot d ON d.codigo = v.codigo_dispositivo
WHERE NOT EXISTS (
    SELECT 1
    FROM leitura_iot li
    WHERE li.dispositivo_iot_id = d.id
      AND li.data_leitura = v.data_leitura
);

-- 22. Alertas
INSERT INTO alerta (
    dispositivo_iot_id, especie_id, titulo, mensagem,
    monitorar_temperatura, monitorar_umidade_ar, monitorar_umidade_solo,
    ativo
)
SELECT
    d.id,
    e.id,
    v.titulo,
    v.mensagem,
    v.monitorar_temperatura,
    v.monitorar_umidade_ar,
    v.monitorar_umidade_solo,
    TRUE
FROM (
    VALUES
        ('ESP32-001', 'Orquídea Phalaenopsis', 'Monitoramento Orquídea', 'Monitora temperatura e umidades da orquídea.', TRUE, TRUE, TRUE),
        ('ESP32-002', 'Samambaia Americana', 'Monitoramento Samambaia', 'Monitora a umidade da samambaia.', FALSE, TRUE, TRUE),
        ('ESP32-003', 'Jiboia', 'Monitoramento Jiboia', 'Monitora temperatura e umidade do solo da jiboia.', TRUE, FALSE, TRUE)
) AS v(codigo_dispositivo, nome_especie, titulo, mensagem, monitorar_temperatura, monitorar_umidade_ar, monitorar_umidade_solo)
JOIN dispositivo_iot d ON d.codigo = v.codigo_dispositivo
JOIN especie e ON e.nome_popular = v.nome_especie
WHERE NOT EXISTS (
    SELECT 1
    FROM alerta a
    WHERE a.dispositivo_iot_id = d.id
      AND a.especie_id = e.id
      AND a.titulo = v.titulo
);

COMMIT;

-- Resumo rápido após o seed
SELECT * FROM (
    SELECT 'categoria' AS tabela, COUNT(*) AS registros FROM categoria
    UNION ALL SELECT 'funcionario', COUNT(*) FROM funcionario
    UNION ALL SELECT 'usuario', COUNT(*) FROM usuario
    UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
    UNION ALL SELECT 'fornecedor', COUNT(*) FROM fornecedor
    UNION ALL SELECT 'produto', COUNT(*) FROM produto
    UNION ALL SELECT 'especie', COUNT(*) FROM especie
    UNION ALL SELECT 'desconto', COUNT(*) FROM desconto
    UNION ALL SELECT 'forma_pagamento', COUNT(*) FROM forma_pagamento
    UNION ALL SELECT 'dispositivo_iot', COUNT(*) FROM dispositivo_iot
    UNION ALL SELECT 'produto_categoria', COUNT(*) FROM produto_categoria
    UNION ALL SELECT 'produto_especie', COUNT(*) FROM produto_especie
    UNION ALL SELECT 'imagem', COUNT(*) FROM imagem
    UNION ALL SELECT 'pedido', COUNT(*) FROM pedido
    UNION ALL SELECT 'pedido_item', COUNT(*) FROM pedido_item
    UNION ALL SELECT 'pagamento', COUNT(*) FROM pagamento
    UNION ALL SELECT 'compra_fornecedor', COUNT(*) FROM compra_fornecedor
    UNION ALL SELECT 'compra_item', COUNT(*) FROM compra_item
    UNION ALL SELECT 'estoque', COUNT(*) FROM estoque
    UNION ALL SELECT 'movimentacao_estoque', COUNT(*) FROM movimentacao_estoque
    UNION ALL SELECT 'leitura_iot', COUNT(*) FROM leitura_iot
    UNION ALL SELECT 'alerta', COUNT(*) FROM alerta
) t
ORDER BY tabela;
