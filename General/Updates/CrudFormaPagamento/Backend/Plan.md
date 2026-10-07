# Plan

## TL;DR

Adicionar no `serverApp` as operações de criação, listagem, detalhe, atualização parcial e remoção lógica de formas de pagamento, protegidas por qualquer JWT válido, sem autorização por perfil. Reutilizar `forma_pagamento` já criada em `V9`; não adicionar migration.

## Steps

- [x] Confirmar o contrato já disponível no schema: `id`, `nome` (`VARCHAR(50) NOT NULL UNIQUE`) e `ativo` (`BOOLEAN NOT NULL DEFAULT TRUE`), e preservar a FK de `pagamento.forma_pagamento_id`.
- [x] Criar `entity/FormaPagamento.kt` e `repository/FormaPagamentoRepository.kt`, seguindo o fluxo `entity → repository Exposed → usecase → controller` documentado para CRUDs. Mapear exatamente as colunas do banco.
- [x] Criar `usecase/FormaPagamentoUseCase.kt`: validar IDs positivos, campos aceitos e limites do schema; rejeitar nome vazio/em branco; implementar criação, listagem, detalhe e PATCH parcial. A listagem inclui ativos e inativos.
- [x] Implementar `DELETE /forma-pagamento/{id}` como inativação (`ativo=false`), não exclusão física: a tabela `pagamento` referencia a forma de pagamento e o CRUD de `Desconto` preserva registros relacionados usando DELETE lógico.
- [x] Criar `controller/FormaPagamentoController.kt`, mantendo a convenção de controller fino e respostas de sucesso equivalentes ao CRUD de referência (`201`, `200` e `204`).
- [x] Documentar as cinco operações e os schemas em `resources/openapi/forma-pagamento.yml`; adicionar tag, paths e schema em `resources/openapi.yaml` e permitir o nome do fragmento em `OpenApiController`. Documentar Bearer JWT para todas as operações.
- [x] Criar fixture e testes de integração em `shared/src/jvmTest/.../integration/fixture` e `.../integration/formaPagamento`, cobrindo operações, validações, dados ativos/inativos e limpeza dos registros.
- [x] Criar teste MVC em `serverApp/src/test/kotlin/org/greenhouse/security` verificando que sem token (e com token inválido) as rotas retornam `401`, e que tokens JWT válidos de `ADMIN` e `FUNCIONARIO` autorizam as cinco operações.
- [ ] Executar as validações específicas descritas abaixo; não alterar nem executar migrations como parte desta track.

## Arquivos relevantes

### Criar

- `serverApp/src/main/kotlin/org/greenhouse/entity/FormaPagamento.kt`
- `serverApp/src/main/kotlin/org/greenhouse/repository/FormaPagamentoRepository.kt`
- `serverApp/src/main/kotlin/org/greenhouse/usecase/FormaPagamentoUseCase.kt`
- `serverApp/src/main/kotlin/org/greenhouse/controller/FormaPagamentoController.kt`
- `serverApp/src/main/resources/openapi/forma-pagamento.yml`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/fixture/FormaPagamentoFixture.kt`
- Testes `TestFormaPagamentoPost`, `TestFormaPagamentoGet`, `TestFormaPagamentoPatch` e teste de exclusão/inativação, no diretório de integração de formas de pagamento.
- `serverApp/src/test/kotlin/org/greenhouse/security/FormaPagamentoSecurityTest.kt`

### Alterar

- `serverApp/src/main/resources/openapi.yaml` — tag, paths e referência do schema novo.
- `serverApp/src/main/kotlin/org/greenhouse/controller/OpenApiController.kt` — whitelist do novo fragmento.

### Consultar, sem alteração prevista

- `serverApp/src/main/resources/db/migration/V9__create_forma_pagamento.sql` — schema fonte da tabela.
- `serverApp/src/main/resources/db/migration/V16__create_pagamento.sql` — FK que torna necessária a preservação de formas em uso.
- `serverApp/src/main/resources/infra/database/seed_dev.sql` — formas de pagamento já incluídas no seed.
- `serverApp/src/main/resources/infra/database/validate_database.sql` — tabela já consta entre as esperadas.
- `docs/architecture/crud_template.md`, `CategoriaController`, `CategoriaRepository`, `CategoriaUseCase` e OpenAPI de Categoria — referência de CRUD.
- `DescontoUseCase`, `DescontoRepository` e OpenAPI de Desconto — referência de inativação para preservar vínculos.
- `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService` e `FuncionarioSecurityTest` — autenticação e teste MVC existentes.
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/categoria` e `integration/fixture/CategoriaFixture.kt` — padrão atual de testes de integração com banco.
- `DatabaseConfig` e `GlobalExceptionHandler` — tratamento atual de erros de banco e HTTP; não alterar genericamente sem decisão explícita.

## Como verificar

- `./gradlew testDesktop` — roda os testes JVM de integração do `shared`; requer `DATABASE_URL` de teste e tabela já migrada no banco-alvo.
- `./gradlew :serverApp:test` — valida os testes MVC de segurança e a configuração do controller.
- `./gradlew ktlintCheck` — valida estilo Kotlin; distinguir eventuais violações preexistentes.
- Revisar a especificação OpenAPI resultante e confirmar que todos os métodos do recurso exigem `BearerAuth`, sem exigir um perfil específico.
- Não executar build nem migration durante esta etapa de planejamento. Antes da execução da track, confirmar disponibilidade e estado do banco de teste sem expor credenciais.
- Validação executada com autorização: `.\gradlew.bat :serverApp:test` passou (22 testes); `.\gradlew.bat :shared:compileTestKotlinJvm` passou.
- `testDesktop` não foi executado porque `DATABASE_URL` não está configurada neste ambiente; os testes de integração exigem banco de teste.
- `ktlintCheck` e as verificações direcionadas reportaram violações em arquivos existentes fora deste CRUD; nenhuma violação foi reportada nos novos arquivos FormaPagamento.

## Decisões tomadas

- **Sem migration:** `V9__create_forma_pagamento.sql` já cria a tabela com os campos necessários. Não há motivo identificado para alterar o schema.
- **Delete lógico:** `pagamento.forma_pagamento_id` referencia a forma de pagamento sem regra de cascata explícita; manter a linha preserva o histórico. O CRUD de Desconto já representa DELETE como inativação.
- **Autorização:** as regras atuais do Spring Security exigem autenticação para qualquer rota não pública, reservando a regra específica de ADMIN somente para `/api/funcionario/**`. Manter o CRUD de forma de pagamento em `authenticated()` sem matcher de perfil.
- **Sem clientes:** a busca no código versionado não encontrou consumidor da forma de pagamento em `shared` nem nos apps; a API nova não exige alteração cliente para atender ao pedido.
- **Contrato HTTP proposto:** usar `/forma-pagamento` como caminho REST kebab-case; o prefixo `/api` é aplicado pela configuração comum. Não existe rota composta anterior que determine essa grafia.
- **DTOs:** não criar DTO/camada adicional; os CRUDs de referência usam a entidade do backend diretamente.
- **Risco de unicidade:** `nome` possui UNIQUE no banco. `DatabaseConfig` atualmente converte exceções SQL genéricas em erro de conexão; este CRUD não deve alterar o mapeamento global sem decisão do time. Definir a resposta HTTP de nome duplicado antes ou durante a execução, conforme Questions.
