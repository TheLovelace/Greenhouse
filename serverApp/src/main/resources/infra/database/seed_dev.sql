-- GREENHOUSE | AV1 | Seed ficticio para Flyway V1-V19 (19 tabelas)
-- AMBIENTE: Neon migration-dev SOMENTE. Nao executar em producao.
-- Execucao: apos aplicar com sucesso todas as 19 migrations.
-- Pode rodar novamente: registros sao identificados por chaves/etiquetas de seed,
-- e os INSERTs possuem ON CONFLICT ou NOT EXISTS para evitar duplicacao.
-- Nao usa TRUNCATE, DELETE, DROP ou ALTER.
-- Usuarios de seed estao DESATIVADOS e nao tem senha de acesso utilizavel.
-- As regras de negocio do backend (baixa de estoque, calculo de totais,
-- geracao de alerta) NAO sao acionadas por INSERTs diretos no banco.

BEGIN;

-- 01/19: funcionario (CPF ficticio; validacao apenas de formato SQL).
INSERT INTO funcionario (nome, cpf, telefone, email, cargo, ativo, data_de_nascimento)
VALUES
 ('SEED | Ana Administradora', '90000000001', '11999900001', 'ghseed.admin@example.invalid', 'ADMINISTRACAO', TRUE, DATE '1996-04-12'),
 ('SEED | Bruna Atendimento', '90000000002', '11999900002', 'ghseed.vendas@example.invalid', 'ATENDIMENTO', TRUE, DATE '1999-09-21'),
 ('SEED | Caio Estoque', '90000000003', '11999900003', 'ghseed.estoque@example.invalid', 'ESTOQUE', TRUE, DATE '1995-01-30')
ON CONFLICT DO NOTHING;

-- 02/19: usuario. Senhas intencionalmente NAO sao hashes validos; contas inativas.
INSERT INTO usuario (funcionario_id, login, senha_hash, perfil, ativo, primeiro_acesso_feito)
SELECT f.id, v.login, '!SEED_LOGIN_DESABILITADO!', v.perfil, FALSE, FALSE
FROM (VALUES
 ('90000000001', 'ghseed.admin', 'ADMIN'),
 ('90000000002', 'ghseed.vendas', 'FUNCIONARIO'),
 ('90000000003', 'ghseed.estoque', 'FUNCIONARIO')
) AS v(cpf, login, perfil)
JOIN funcionario f ON f.cpf = v.cpf
ON CONFLICT DO NOTHING;

-- 03/19: cliente. O quarto pedido demonstrara venda sem cliente identificado.
INSERT INTO cliente (nome, telefone, whatsapp, cpf, email, ativo, data_nascimento)
VALUES
 ('SEED | Marina Souza', '11988881001', TRUE, '90000000011', 'ghseed.marina@example.invalid', TRUE, DATE '2001-03-15'),
 ('SEED | Rafael Lima', '11988881002', TRUE, '90000000012', 'ghseed.rafael@example.invalid', TRUE, DATE '1998-07-09'),
 ('SEED | Camila Rocha', '11988881003', FALSE, '90000000013', 'ghseed.camila@example.invalid', TRUE, DATE '2000-11-22')
ON CONFLICT DO NOTHING;

-- 04/19: fornecedor.
INSERT INTO fornecedor (nome, cnpj, telefone, email, contato, ativo, prazo_medio_entrega_dias)
VALUES
 ('SEED | Flora Distribuidora', '90000000000001', '1130001001', 'ghseed.flora@example.invalid', 'Fernanda', TRUE, 5),
 ('SEED | Verde Atacado', '90000000000002', '1130001002', 'ghseed.verde@example.invalid', 'Ricardo', TRUE, 8),
 ('SEED | Jardim Suprimentos', '90000000000003', '1130001003', 'ghseed.jardim@example.invalid', 'Paula', TRUE, 3)
ON CONFLICT DO NOTHING;

-- 05/19: categoria (data_cadastro default CURRENT_DATE).
INSERT INTO categoria (nome, descricao, ativo, observacao)
VALUES
 ('SEED | Plantas ornamentais', 'Categoria ficticia para catalogo de plantas.', TRUE, 'Popular vitrine AV1'),
 ('SEED | Vasos', 'Recipientes e vasos para cultivo.', TRUE, 'Acessorios de cultivo'),
 ('SEED | Substratos', 'Misturas para cultivo de plantas.', TRUE, 'Insumos')
ON CONFLICT DO NOTHING;

-- 06/19: produto, com codigo e prazo_preparo_minutos.
INSERT INTO produto (codigo, nome, descricao, preco, ativo, prazo_preparo_minutos)
VALUES
 ('GH-SEED-001', 'SEED | Orquidea Phalaenopsis', 'Orquidea ornamental em vaso.', 79.90, TRUE, 30),
 ('GH-SEED-002', 'SEED | Vaso Ceramico Medio', 'Vaso para plantas de medio porte.', 39.90, TRUE, 10),
 ('GH-SEED-003', 'SEED | Substrato Universal 5L', 'Substrato pronto para cultivo.', 24.50, TRUE, 5),
 ('GH-SEED-004', 'SEED | Samambaia Americana', 'Planta ornamental para ambientes internos.', 49.90, TRUE, 20)
ON CONFLICT DO NOTHING;

-- 07/19: especie (sem UNIQUE no modelo; identificacao pelo nome_cientifico).
INSERT INTO especie (
 nome_popular, nome_cientifico, descricao,
 temperatura_min, temperatura_max, umidade_ar_min, umidade_ar_max,
 umidade_solo_min, umidade_solo_max, ativo, intervalo_rega_dias
)
SELECT v.nome_popular, v.nome_cientifico, v.descricao,
 v.temp_min, v.temp_max, v.ar_min, v.ar_max, v.solo_min, v.solo_max, TRUE, v.rega
FROM (VALUES
 ('SEED | Orquidea Phalaenopsis', 'GH-SEED-Phalaenopsis', 'Planta tropical.', 18.00, 30.00, 50.00, 80.00, 40.00, 70.00, 5),
 ('SEED | Samambaia Americana', 'GH-SEED-Nephrolepis', 'Folhagem ornamental.', 16.00, 28.00, 55.00, 90.00, 45.00, 80.00, 3),
 ('SEED | Jiboia', 'GH-SEED-Epipremnum', 'Planta resistente.', 18.00, 30.00, 40.00, 80.00, 35.00, 70.00, 7)
) AS v(nome_popular, nome_cientifico, descricao, temp_min, temp_max, ar_min, ar_max, solo_min, solo_max, rega)
WHERE NOT EXISTS (SELECT 1 FROM especie e WHERE e.nome_cientifico = v.nome_cientifico);

-- 08/19: desconto (tipo, percentual/valor, datas, quantidade_minima_itens).
INSERT INTO desconto (titulo, descricao, tipo, valor, data_inicio, data_fim, ativo, quantidade_minima_itens)
SELECT v.titulo, v.descricao, v.tipo, v.valor, v.inicio, v.fim, TRUE, v.min_itens
FROM (VALUES
 ('SEED | Boas-vindas 10%', 'Desconto promocional ficticio.', 'PERCENTUAL', 10.00, TIMESTAMPTZ '2026-01-01 00:00:00-03', TIMESTAMPTZ '2028-12-31 23:59:59-03', 1),
 ('SEED | Desconto R$ 15', 'Desconto fixo ficticio.', 'VALOR_FIXO', 15.00, TIMESTAMPTZ '2026-01-01 00:00:00-03', TIMESTAMPTZ '2028-12-31 23:59:59-03', 2)
) AS v(titulo, descricao, tipo, valor, inicio, fim, min_itens)
WHERE NOT EXISTS (SELECT 1 FROM desconto d WHERE d.titulo = v.titulo);

-- 09/19: forma_pagamento; compartilha opcoes entre Pedido e Compra.
INSERT INTO forma_pagamento (nome, ativo, descricao, limite_parcelas, prazo_estorno_dias)
VALUES
 ('PIX', TRUE, 'Pagamento por transferencia instantanea.', 1, 1),
 ('Cartao de Credito', TRUE, 'Pagamento com parcelamento.', 12, 30),
 ('Cartao de Debito', TRUE, 'Pagamento no debito.', 1, 7),
 ('Dinheiro', TRUE, 'Pagamento em especie.', 1, 0)
ON CONFLICT DO NOTHING;

-- 10/19: dispositivo_iot.
INSERT INTO dispositivo_iot (codigo, nome, localizacao, topico_mqtt, calibracao_seco, calibracao_umido, ativo)
VALUES
 ('GH-SEED-IOT-001', 'SEED | Sensor Estufa A', 'Estufa A', 'greenhouse/seed/estufa-a/sensor', 3200, 1400, TRUE),
 ('GH-SEED-IOT-002', 'SEED | Sensor Estufa B', 'Estufa B', 'greenhouse/seed/estufa-b/sensor', 3150, 1450, TRUE)
ON CONFLICT DO NOTHING;

-- 11/19: produto_categoria; pode haver mais de uma categoria por produto.
INSERT INTO produto_categoria (produto_id, categoria_id)
SELECT p.id, c.id
FROM (VALUES
 ('GH-SEED-001', 'SEED | Plantas ornamentais'),
 ('GH-SEED-002', 'SEED | Vasos'),
 ('GH-SEED-003', 'SEED | Substratos'),
 ('GH-SEED-004', 'SEED | Plantas ornamentais')
) AS v(produto_codigo, categoria_nome)
JOIN produto p ON p.codigo = v.produto_codigo
JOIN categoria c ON c.nome = v.categoria_nome
ON CONFLICT DO NOTHING;

-- 12/19: produto_especie.
INSERT INTO produto_especie (produto_id, especie_id)
SELECT p.id, e.id
FROM (VALUES
 ('GH-SEED-001', 'GH-SEED-Phalaenopsis'),
 ('GH-SEED-004', 'GH-SEED-Nephrolepis')
) AS v(produto_codigo, especie_cientifica)
JOIN produto p ON p.codigo = v.produto_codigo
JOIN especie e ON e.nome_cientifico = v.especie_cientifica
ON CONFLICT DO NOTHING;

-- 13/19: pedido. observacao e marcador unico de seed (sem UNIQUE no esquema).
-- Totais ilustrativos conferem com os respectivos itens; logica do backend nao roda aqui.
INSERT INTO pedido (
 cliente_id, funcionario_id, desconto_id, forma_pagamento_id, modalidade, status,
 data_pedido, data_retirada_prevista, data_retirada, valor_desconto, valor_total,
 observacao, embalagem_presente, numero_volumes
)
SELECT c.id, f.id, d.id, fp.id, v.modalidade, v.status,
 v.data_pedido, v.prevista, v.retirada, v.desconto_valor, v.total,
 v.marcador, v.embalagem, v.volumes
FROM (VALUES
 ('90000000011', '90000000002', 'SEED | Boas-vindas 10%', 'PIX', 'IMEDIATA', 'CONCLUIDO', TIMESTAMPTZ '2026-10-01 10:00:00-03', NULL::timestamptz, TIMESTAMPTZ '2026-10-01 10:30:00-03', 7.99, 71.91, '[GH-SEED] PEDIDO-001', TRUE, 1),
 ('90000000012', '90000000002', NULL, 'Cartao de Credito', 'AGENDADA', 'CONFIRMADO', TIMESTAMPTZ '2026-10-02 14:00:00-03', TIMESTAMPTZ '2026-10-15 15:00:00-03', NULL::timestamptz, 0.00, 64.40, '[GH-SEED] PEDIDO-002', FALSE, 2),
 (NULL, '90000000001', NULL, 'Dinheiro', 'IMEDIATA', 'ABERTO', TIMESTAMPTZ '2026-10-03 09:00:00-03', NULL::timestamptz, NULL::timestamptz, 0.00, 49.90, '[GH-SEED] PEDIDO-003 SEM CLIENTE', FALSE, 1)
) AS v(cpf_cliente, cpf_func, desconto_titulo, forma, modalidade, status, data_pedido, prevista, retirada, desconto_valor, total, marcador, embalagem, volumes)
JOIN funcionario f ON f.cpf = v.cpf_func
JOIN forma_pagamento fp ON fp.nome = v.forma
LEFT JOIN cliente c ON c.cpf = v.cpf_cliente
LEFT JOIN desconto d ON d.titulo = v.desconto_titulo
WHERE NOT EXISTS (SELECT 1 FROM pedido p WHERE p.observacao = v.marcador);

-- 14/19: pedido_item.
INSERT INTO pedido_item (pedido_id, produto_id, quantidade, preco_unitario)
SELECT pe.id, pr.id, v.qtd, v.valor
FROM (VALUES
 ('[GH-SEED] PEDIDO-001', 'GH-SEED-001', 1, 79.90),
 ('[GH-SEED] PEDIDO-002', 'GH-SEED-002', 1, 39.90),
 ('[GH-SEED] PEDIDO-002', 'GH-SEED-003', 1, 24.50),
 ('[GH-SEED] PEDIDO-003 SEM CLIENTE', 'GH-SEED-004', 1, 49.90)
) AS v(pedido_marcador, produto_codigo, qtd, valor)
JOIN pedido pe ON pe.observacao = v.pedido_marcador
JOIN produto pr ON pr.codigo = v.produto_codigo
WHERE NOT EXISTS (
 SELECT 1 FROM pedido_item i WHERE i.pedido_id = pe.id AND i.produto_id = pr.id
);

-- 15/19: compra_fornecedor. Total da compra 001 inclui frete de 25.00.
INSERT INTO compra_fornecedor (
 funcionario_id, fornecedor_id, forma_pagamento_id, data_compra, status,
 valor_total, valor_frete, frete_incluso, data_entrega_prevista, observacao
)
SELECT f.id, forn.id, fp.id, v.data_compra, v.status, v.total,
 v.frete, v.frete_incluso, v.prevista, v.marcador
FROM (VALUES
 ('90000000003', '90000000000001', 'PIX', TIMESTAMPTZ '2026-10-01 09:00:00-03', 'PEDIDA', 320.00, 25.00, TRUE, DATE '2026-10-20', '[GH-SEED] COMPRA-001'),
 ('90000000003', '90000000000002', 'Cartao de Credito', TIMESTAMPTZ '2026-10-02 10:00:00-03', 'RECEBIDA', 210.00, NULL::numeric, FALSE, DATE '2026-10-09', '[GH-SEED] COMPRA-002'),
 ('90000000001', '90000000000003', 'PIX', TIMESTAMPTZ '2026-10-03 11:00:00-03', 'CANCELADA', 90.00, 0.00, FALSE, NULL::date, '[GH-SEED] COMPRA-003')
) AS v(cpf_func, cnpj_forn, forma, data_compra, status, total, frete, frete_incluso, prevista, marcador)
JOIN funcionario f ON f.cpf = v.cpf_func
JOIN fornecedor forn ON forn.cnpj = v.cnpj_forn
JOIN forma_pagamento fp ON fp.nome = v.forma
WHERE NOT EXISTS (SELECT 1 FROM compra_fornecedor cf WHERE cf.observacao = v.marcador);

-- 16/19: compra_item. Soma de itens: 295, 210 e 90.
INSERT INTO compra_item (compra_id, produto_id, quantidade, valor_unitario)
SELECT c.id, p.id, v.qtd, v.valor
FROM (VALUES
 ('[GH-SEED] COMPRA-001', 'GH-SEED-001', 10, 29.50),
 ('[GH-SEED] COMPRA-002', 'GH-SEED-003', 20, 10.50),
 ('[GH-SEED] COMPRA-003', 'GH-SEED-002', 5, 18.00)
) AS v(compra_marcador, produto_codigo, qtd, valor)
JOIN compra_fornecedor c ON c.observacao = v.compra_marcador
JOIN produto p ON p.codigo = v.produto_codigo
WHERE NOT EXISTS (SELECT 1 FROM compra_item i WHERE i.compra_id = c.id AND i.produto_id = p.id);

-- 17/19: estoque. Estado inicial ficticio, nao calculado automaticamente a partir dos pedidos.
INSERT INTO estoque (
 produto_id, quantidade_fisica, quantidade_reservada, quantidade_minima, localizacao, bloqueado
)
SELECT p.id, v.fisico, v.reservado, v.minimo, v.localizacao, v.bloqueado
FROM (VALUES
 ('GH-SEED-001', 30, 1, 5, 'Estante A-01', FALSE),
 ('GH-SEED-002', 15, 0, 3, 'Estante B-02', FALSE),
 ('GH-SEED-003', 100, 2, 20, 'Deposito C-03', FALSE),
 ('GH-SEED-004', 20, 0, 4, 'Estante A-04', TRUE)
) AS v(produto_codigo, fisico, reservado, minimo, localizacao, bloqueado)
JOIN produto p ON p.codigo = v.produto_codigo
ON CONFLICT (produto_id) DO NOTHING;

-- 18/19: leitura_iot. Datas fixas para que o seed seja repetivel.
INSERT INTO leitura_iot (
 dispositivo_iot_id, temperatura, umidade_ar, umidade_solo, umidade_solo_raw, data_leitura
)
SELECT d.id, v.temperatura, v.umidade_ar, v.umidade_solo, v.raw, v.instante
FROM (VALUES
 ('GH-SEED-IOT-001', 24.50, 62.00, 55.00, 1980, TIMESTAMPTZ '2026-10-10 08:00:00-03'),
 ('GH-SEED-IOT-001', 29.20, 48.00, 29.00, 2800, TIMESTAMPTZ '2026-10-10 09:00:00-03'),
 ('GH-SEED-IOT-002', 21.60, 70.00, 64.00, 1750, TIMESTAMPTZ '2026-10-10 10:00:00-03')
) AS v(dispositivo_codigo, temperatura, umidade_ar, umidade_solo, raw, instante)
JOIN dispositivo_iot d ON d.codigo = v.dispositivo_codigo
WHERE NOT EXISTS (
 SELECT 1 FROM leitura_iot l WHERE l.dispositivo_iot_id = d.id AND l.data_leitura = v.instante
);

-- 19/19: alerta de configuracao; eventos/disparos nao sao uma tabela neste modelo.
INSERT INTO alerta (
 dispositivo_iot_id, especie_id, titulo, mensagem,
 monitorar_temperatura, monitorar_umidade_ar, monitorar_umidade_solo, ativo, prioridade
)
SELECT d.id, e.id, v.titulo, v.mensagem, v.temp, v.ar, v.solo, TRUE, v.prioridade
FROM (VALUES
 ('GH-SEED-IOT-001', 'GH-SEED-Phalaenopsis', 'SEED | Alerta de umidade baixa', 'Conferir irrigacao da estufa.', FALSE, FALSE, TRUE, 1),
 ('GH-SEED-IOT-002', 'GH-SEED-Nephrolepis', 'SEED | Alerta temperatura alta', 'Monitorar temperatura do ambiente.', TRUE, FALSE, FALSE, 2)
) AS v(dispositivo_codigo, especie_cientifica, titulo, mensagem, temp, ar, solo, prioridade)
JOIN dispositivo_iot d ON d.codigo = v.dispositivo_codigo
JOIN especie e ON e.nome_cientifico = v.especie_cientifica
WHERE NOT EXISTS (SELECT 1 FROM alerta a WHERE a.titulo = v.titulo);

COMMIT;

-- Resumo de registros do seed: consultas sem alteracoes.
SELECT 'funcionario' AS tabela, COUNT(*) AS registros_seed FROM funcionario WHERE cpf LIKE '9000000000%'
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario WHERE login LIKE 'ghseed.%'
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente WHERE cpf LIKE '9000000001%'
UNION ALL SELECT 'fornecedor', COUNT(*) FROM fornecedor WHERE cnpj LIKE '9000000000000%'
UNION ALL SELECT 'categoria', COUNT(*) FROM categoria WHERE nome LIKE 'SEED | %'
UNION ALL SELECT 'produto', COUNT(*) FROM produto WHERE codigo LIKE 'GH-SEED-%'
UNION ALL SELECT 'especie', COUNT(*) FROM especie WHERE nome_cientifico LIKE 'GH-SEED-%'
UNION ALL SELECT 'desconto', COUNT(*) FROM desconto WHERE titulo LIKE 'SEED | %'
UNION ALL SELECT 'forma_pagamento', COUNT(*) FROM forma_pagamento WHERE nome IN ('PIX','Cartao de Credito','Cartao de Debito','Dinheiro')
UNION ALL SELECT 'dispositivo_iot', COUNT(*) FROM dispositivo_iot WHERE codigo LIKE 'GH-SEED-IOT-%'
UNION ALL SELECT 'produto_categoria', COUNT(*) FROM produto_categoria pc JOIN produto p ON p.id=pc.produto_id WHERE p.codigo LIKE 'GH-SEED-%'
UNION ALL SELECT 'produto_especie', COUNT(*) FROM produto_especie pe JOIN produto p ON p.id=pe.produto_id WHERE p.codigo LIKE 'GH-SEED-%'
UNION ALL SELECT 'pedido', COUNT(*) FROM pedido WHERE observacao LIKE '[GH-SEED] PEDIDO-%'
UNION ALL SELECT 'pedido_item', COUNT(*) FROM pedido_item pi JOIN pedido p ON p.id=pi.pedido_id WHERE p.observacao LIKE '[GH-SEED] PEDIDO-%'
UNION ALL SELECT 'compra_fornecedor', COUNT(*) FROM compra_fornecedor WHERE observacao LIKE '[GH-SEED] COMPRA-%'
UNION ALL SELECT 'compra_item', COUNT(*) FROM compra_item ci JOIN compra_fornecedor c ON c.id=ci.compra_id WHERE c.observacao LIKE '[GH-SEED] COMPRA-%'
UNION ALL SELECT 'estoque', COUNT(*) FROM estoque e JOIN produto p ON p.id=e.produto_id WHERE p.codigo LIKE 'GH-SEED-%'
UNION ALL SELECT 'leitura_iot', COUNT(*) FROM leitura_iot l JOIN dispositivo_iot d ON d.id=l.dispositivo_iot_id WHERE d.codigo LIKE 'GH-SEED-IOT-%'
UNION ALL SELECT 'alerta', COUNT(*) FROM alerta WHERE titulo LIKE 'SEED | %'
ORDER BY tabela;
