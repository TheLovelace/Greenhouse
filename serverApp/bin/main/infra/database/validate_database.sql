-- Greenhouse - Auditoria final de migrations / schema
-- Pode ser executado no Neon SQL Editor ou DBeaver.

-- =========================================================
-- 1. HISTÓRICO DO FLYWAY
-- Esperado: V1 até V23, todas com success = true.
-- =========================================================
SELECT
    installed_rank,
    version,
    description,
    type,
    installed_by,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;

-- Resumo do histórico
SELECT
    COUNT(*) FILTER (WHERE version IS NOT NULL AND success) AS migrations_aplicadas,
    COUNT(*) FILTER (WHERE NOT success) AS migrations_com_erro,
    MAX(CASE WHEN version ~ '^[0-9]+$' THEN version::INTEGER END) AS ultima_versao
FROM flyway_schema_history;

-- Verifica especificamente se V1..V23 estão presentes e bem-sucedidas.
WITH esperadas AS (
    SELECT generate_series(1, 23) AS versao
), aplicadas AS (
    SELECT version::INTEGER AS versao, success
    FROM flyway_schema_history
    WHERE version ~ '^[0-9]+$'
)
SELECT
    e.versao,
    CASE
        WHEN a.versao IS NULL THEN 'FALTANDO'
        WHEN a.success THEN 'OK'
        ELSE 'ERRO'
    END AS status
FROM esperadas e
LEFT JOIN aplicadas a USING (versao)
ORDER BY e.versao;

-- Verifica a flag de primeiro acesso adicionada em V23.
SELECT
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'usuario'
              AND column_name = 'primeiro_acesso_feito'
              AND data_type = 'boolean'
              AND is_nullable = 'NO'
              AND column_default = 'false'
        )
        THEN 'OK'
        ELSE 'FALTANDO/INCORRETO'
    END AS primeiro_acesso_feito;

-- =========================================================
-- 2. TABELAS ESPERADAS
-- Esperado: todas com status OK.
-- =========================================================
WITH esperadas(tabela) AS (
    VALUES
        ('categoria'),
        ('funcionario'),
        ('usuario'),
        ('cliente'),
        ('fornecedor'),
        ('produto'),
        ('especie'),
        ('desconto'),
        ('forma_pagamento'),
        ('dispositivo_iot'),
        ('produto_categoria'),
        ('produto_especie'),
        ('imagem'),
        ('pedido'),
        ('pedido_item'),
        ('pagamento'),
        ('compra_fornecedor'),
        ('compra_item'),
        ('estoque'),
        ('movimentacao_estoque'),
        ('leitura_iot'),
        ('alerta'),
        ('flyway_schema_history')
)
SELECT
    e.tabela,
    CASE WHEN t.table_name IS NOT NULL THEN 'OK' ELSE 'FALTANDO' END AS status
FROM esperadas e
LEFT JOIN information_schema.tables t
    ON t.table_schema = 'public'
   AND t.table_name = e.tabela
ORDER BY e.tabela;

-- =========================================================
-- 3. CHECK CONSTRAINTS ESPERADOS
-- Esperado: todos com status OK.
-- =========================================================
WITH esperados(constraint_name) AS (
    VALUES
        ('chk_funcionario_cpf_formato'),
        ('chk_usuario_perfil'),
        ('chk_cliente_cpf_formato'),
        ('chk_fornecedor_cnpj_formato'),
        ('chk_produto_preco'),
        ('chk_especie_temperatura'),
        ('chk_especie_umidade_ar'),
        ('chk_especie_umidade_solo'),
        ('chk_desconto_tipo'),
        ('chk_desconto_valor'),
        ('chk_desconto_periodo'),
        ('chk_imagem_origem'),
        ('chk_imagem_ordem'),
        ('chk_pedido_modalidade'),
        ('chk_pedido_agendamento'),
        ('chk_pedido_status'),
        ('chk_pedido_valor_desconto'),
        ('chk_pedido_valor_total'),
        ('chk_pedido_item_quantidade'),
        ('chk_pedido_item_preco'),
        ('chk_pagamento_valor'),
        ('chk_pagamento_status'),
        ('chk_compra_status'),
        ('chk_compra_valor'),
        ('chk_compra_item_quantidade'),
        ('chk_compra_item_valor'),
        ('chk_estoque_fisico'),
        ('chk_estoque_reservado'),
        ('chk_estoque_minimo'),
        ('chk_estoque_reserva'),
        ('chk_movimentacao_tipo'),
        ('chk_movimentacao_quantidade'),
        ('chk_leitura_umidade_ar'),
        ('chk_leitura_umidade_solo'),
        ('chk_leitura_umidade_solo_raw'),
        ('chk_alerta_monitoramento')
), existentes AS (
    SELECT conname AS constraint_name
    FROM pg_constraint
    WHERE contype = 'c'
      AND connamespace = 'public'::regnamespace
)
SELECT
    e.constraint_name,
    CASE WHEN x.constraint_name IS NOT NULL THEN 'OK' ELSE 'FALTANDO' END AS status
FROM esperados e
LEFT JOIN existentes x USING (constraint_name)
ORDER BY e.constraint_name;

-- =========================================================
-- 4. FOREIGN KEYS
-- Lista as FKs efetivamente existentes para inspeção.
-- =========================================================
SELECT
    tc.table_name,
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS tabela_referenciada,
    ccu.column_name AS coluna_referenciada
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
 AND tc.constraint_schema = kcu.constraint_schema
JOIN information_schema.constraint_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
 AND ccu.constraint_schema = tc.constraint_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.constraint_schema = 'public'
ORDER BY tc.table_name, tc.constraint_name, kcu.ordinal_position;

-- =========================================================
-- 5. ÍNDICES CRIADOS EXPLICITAMENTE NAS MIGRATIONS
-- =========================================================
WITH esperados(index_name) AS (
    VALUES
        ('idx_produto_nome'),
        ('idx_produto_categoria_categoria'),
        ('idx_imagem_produto'),
        ('idx_imagem_especie'),
        ('idx_pedido_cliente'),
        ('idx_pedido_funcionario'),
        ('idx_pedido_status'),
        ('idx_pedido_data'),
        ('idx_pedido_retirada'),
        ('idx_pedido_item_pedido'),
        ('idx_pedido_item_produto'),
        ('idx_pagamento_pedido'),
        ('idx_compra_fornecedor'),
        ('idx_compra_item_produto'),
        ('idx_movimentacao_produto'),
        ('idx_movimentacao_data'),
        ('idx_leitura_dispositivo'),
        ('idx_leitura_data')
), existentes AS (
    SELECT indexname AS index_name
    FROM pg_indexes
    WHERE schemaname = 'public'
)
SELECT
    e.index_name,
    CASE WHEN x.index_name IS NOT NULL THEN 'OK' ELSE 'FALTANDO' END AS status
FROM esperados e
LEFT JOIN existentes x USING (index_name)
ORDER BY e.index_name;

-- =========================================================
-- 6. RESULTADO FINAL RESUMIDO
-- Esperado: 23 migrations, versão 23 e zero falhas.
-- =========================================================
SELECT
    CASE
        WHEN COUNT(*) FILTER (WHERE version ~ '^[0-9]+$' AND success) = 23
         AND COUNT(*) FILTER (WHERE NOT success) = 0
         AND MAX(CASE WHEN version ~ '^[0-9]+$' THEN version::INTEGER END) = 23
        THEN 'OK - migrations V1 a V23 aplicadas com sucesso'
        ELSE 'ATENCAO - revisar flyway_schema_history'
    END AS resultado
FROM flyway_schema_history;
