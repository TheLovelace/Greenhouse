# Worklog — Backend: CRUD de Pedido

## Sessão de planejamento

Investigação do repositório (branch `feat/crud-pedido`, criada a partir de `develop` atualizado):

- Template de CRUD em `docs/architecture/crud_template.md`; referências vivas: `Categoria`, `Cliente`, `Produto` e `Desconto` (este último com o precedente de endpoint dedicado de status: `PATCH /desconto/{id}/status` + DTO `AtualizarStatusDescontoRequest`).
- Schema completo já existe em `develop`: `pedido` (V14), `pedido_item` (V15, FK `ON DELETE CASCADE`), `pagamento` (V16), `estoque` (V19, CHECK `reservada <= fisica`), `movimentacao_estoque` (V20, sem tipo RESERVA). **Nenhuma migration nova.**
- `DatabaseConfig` é `@Bean` único (`Main.kt`); `session {}` encapsula `transaction {}` do Exposed — cada método de repository é uma transação. Para atomicidade pedido+itens+estoque, os efeitos ficam **dentro** de uma única chamada de repository.
- Branch paralela `origin/feat/crud-estoque` (não mergeada) já contém `Estoque`/`MovimentacaoEstoque`/`TipoMovimentacaoEstoque`/`EstoqueRepository`/`EstoqueUseCase`/`openapi/estoque.yml` + testes. Relevante porque:
  - `EstoqueUseCase.validarOrigem` confirma a regra: `SAIDA` vinculada somente a pedido concluído; `ENTRADA` somente a compra — ou seja, conclusão de pedido deve gerar movimentação `SAIDA` por item.
  - `EstoqueRepository.registrarMovimentacao` ajusta só `quantidade_fisica` — **não** trata `quantidade_reservada`. A reserva é responsabilidade do fluxo de pedido.
  - Decisão: não depender dessa branch; `PedidoRepository` declara `Estoques`/`MovimentacoesEstoque` como table objects privados (mesmo padrão de `ProdutoRepository`, que já declara `ItensPedido`/`Estoques`/`MovimentacoesEstoque` privados para `possuiDependencias`).
- `seed_dev.sql` já semeia 3 pedidos com itens, estoque por produto e uma movimentação `SAIDA` ligada a pedido — confirma o modelo e serve de referência para a fixture.
- Backend sem autenticação: `funcionarioId` vem no payload e `clienteId` segue nullable conforme V14.
- `UnprocessableEntityException` mapeia para HTTP 400 (não 422) — erros da API são 400/404 (+503 de infra).
- Testes: fixtures em `shared/src/jvmTest/.../integration/fixture/` instanciam `Repository(DatabaseConfig(System.getenv("DATABASE_URL")))`; setup pesado usa JDBC direto (`DescontoFixture`). Rodam com `./gradlew testDesktop`.

Respostas do usuário durante o planejamento (registradas também em `Questions.md`):

- Cancelamento: ler a user story de estoque → "liberar ou consumir reserva conforme alteração/cancelamento/conclusão do pedido" implica cancelar de estados que reservam → `CONFIRMADO` e `PRONTO_RETIRADA` canceláveis com liberação de reserva.
- `preco_unitario`: snapshot de `produto.preco` no backend.
- `cliente_id`: opcional, validado (existe + ativo) quando informado.

Decisão autônoma: nome do Update `CrudPedido` e track única `Backend` — mesma estrutura do update `CrudCliente` (escopo idêntico: só serverApp).

Decisão autônoma (revista após o merge — ver seção abaixo): enums `ModalidadePedido`/`StatusPedido` ficam em arquivos próprios em `enums/`.

## Replanejamento após merge de `crud-estoque` em `develop`

`feat/crud-estoque` foi mergeada em `develop` (commit `9ac0736`) e o merge foi trazido para `feat/crud-pedido` (fast-forward `74202c4..9ac0736`). O plano foi reescrito porque fatos mudaram:

- **Reuso substitui duplicação**: `EstoqueRepository`/`EstoqueUseCase` agora existem em `develop`. `EstoqueUseCase.registrarMovimentacaoInterna` é `internal` e aceita `pedidoId`/`funcionarioId` — projetada para o fluxo de pedido. `PedidoRepository` **não** recria tabelas de `estoque`/`movimentacao_estoque`; a track apenas adiciona `reservar`/`liberarReserva` ao módulo de estoque (única operação que ele não cobre).
- **Nova fronteira transacional**: `PedidoUseCase` injeta `DatabaseConfig` e abre `session {}` nas transições com efeito — chamadas aninhadas de repository/use case juntam-se à transação externa (mesmo `Database` único). Nas fixtures, todos os repos/use cases precisam compartilhar a MESMA instância de `DatabaseConfig`.
- **Convenções novas de `develop`**: enums em `enums/` (`TipoDesconto` foi movido para lá); DTOs de request em arquivo próprio em `controller/` (`RegistrarMovimentacaoEstoqueRequest.kt`, com enum tipado); table objects privados com sufixo `Table`.
- **`PATCH /desconto/{id}/status` removido** do controller (desconto agora usa campo `ativo` no PATCH + DELETE lógico), mas `openapi.yaml` ainda referencia o path → **ref quebrada preexistente**, fora do escopo desta track (registrado em `Questions.md`). Para pedido, o endpoint dedicado `/pedido/{id}/status` permanece justificado: transições têm efeitos colaterais e matriz de validade, diferente de um booleano.
- `EstoqueFixture.novoProdutoComEstoque(fisica, reservada, minima)` já existe e pode ser composta pela `PedidoFixture`.

## Replanejamento — divisão de responsabilidade com a dev do estoque

Decisão do usuário: `reservar`/`liberarReserva` serão implementados pela Débora (dev do CRUD de estoque). A track **não altera** `EstoqueRepository`/`EstoqueUseCase` — o pedido apenas consome o contrato:

- `estoqueUseCase.reservar(produtoId, quantidade)` / `liberarReserva(produtoId, quantidade)` — a existir; insuficiência de estoque deve virar `UnprocessableEntityException`.
- `registrarMovimentacaoInterna(..., SAIDA, pedidoId, funcionarioId)` e `buscarPosicaoPorProdutoId(produtoId)` — já existem.

Implicação para o executor: `PedidoUseCase.atualizarStatus` só compila depois que os métodos existirem na branch — pré-requisito 2 do Plan.md. Se a assinatura entregue divergir do contrato assumido, ajustar as chamadas (barato) e registrar aqui.

## Respostas do usuário (rodada 2)

- `data_retirada_prevista` deve ser sempre do dia vigente em diante → plano passa a validar `>= LocalDate.now()` na criação e no PATCH (comparação por data, não instante).
- Dono do CRUD de desconto autorizou a correção da ref quebrada `/desconto/{id}/status` em `openapi.yaml` → remoção entrou no escopo da track (step do OpenAPI).
- Pedido em `PRONTO_RETIRADA`/`CANCELADO` **não** grava em `movimentacao_estoque` — o usuário desistiu ao saber que o CHECK de `tipo` não cobre eventos de reserva (exigiria migration). `SAIDA` por item apenas na conclusão (já previsto).

## Execução — implementação do CRUD de pedido

- Pré-requisito 2 verificado: `reservar`/`liberarReserva` **não existem** na `develop` mergeada. Usuário optou por **assumir o contrato**: `PedidoUseCase` chama `estoqueUseCase.reservar(produtoId, quantidade)` e `estoqueUseCase.liberarReserva(produtoId, quantidade)` — a branch só compila depois que a Débora entregar os métodos. `testDesktop`/`ktlintCheck` ficam pendentes por isso + autorização de build.
- Implementados: enums `ModalidadePedido`/`StatusPedido`, entities `Pedido`/`PedidoItem`, `PedidoRepository` (agregado pedido+itens na mesma session, `bloquearPorId` com `forUpdate`, `funcionarioAtivo`, `excluir` para fixture), `PedidoUseCase` (validações + máquina de status + efeitos de estoque dentro de `databaseConfig.session`), `AtualizarStatusPedidoRequest`, `PedidoController` (5 rotas, sem DELETE), `openapi/pedido.yml` + registro em `openapi.yaml` (ref quebrada `/desconto/{id}/status` removida, autorizada) + whitelist no `OpenApiController`.
- Testes: `PedidoFixture` (JDBC para funcionario/cliente/desconto/produto/estoque; **DatabaseConfig único compartilhado** entre todos os repositories — é o que faz as sessions aninhadas juntarem-se à transação do use case) + 4 classes de teste cobrindo os casos do plano.
- Decisão autônoma: fixture simula reserva concorrente via JDBC (`reservarEstoque`) em vez de chamar `estoqueUseCase.reservar`, porque se a Débora implementar como `internal` o módulo `shared/jvmTest` não a enxergaria.
- Decisão autônoma: PATCH revalida `dataRetiradaPrevista` **somente quando o campo vem no payload** — revalidar sempre bloquearia edições não relacionadas num pedido AGENDADA cuja data ficou no passado com o tempo.

## Validação — `ktlintCheck` (autorizado, executado)

`./gradlew ktlintCheck` rodado após autorização ("pode rodar"). Resultado:

- **Arquivos novos da track: zero violações** após 2 rodadas de correção:
  - `PedidoRepository.kt`: 4× `chain-method-continuation` — cadeia Exposed passou a quebrar antes do primeiro `.selectAll()`/`.update()` (convenção da ktlint nova, vista em `EstoqueRepository`).
  - `TestPedidoPatch.kt` (5×) e `TestPedidoStatus.kt` (9×): `multiline-expression-wrapping` — `val x = expr multilinha` passou a `val x =\n    expr(...)` com interior reindentado.
- **Check global continua FALHANDO — tudo preexistente**: dezenas de arquivos antigos violam as regras (`Class body should not start with blank line`, `Newline expected after opening parenthesis`, etc.): `desktopApp/main.kt`, `serverApp/build.gradle.kts`, `SharedLogicAndroidHostTest.kt`, `DatabaseConfig`, `ExceptionConfig`, `WebConfig`, `CategoriaController`, `ClienteController`, `ProdutoController`, `OpenApiController` (linhas preexistentes — a linha 25 alterada por esta track está limpa), `Status.kt`, `Main.kt`, repositories e use cases antigos, fixtures/testes antigos de jvmTest. **Nenhum pertence ao escopo do CRUD de pedido** — corrigir seria alterar trabalho de outros devs.
- **Correção de causa (investigado depois)**: NÃO foi atualização de versão — o plugin `org.jlleitschuh.gradle.ktlint` está fixo em `14.0.1` no `build.gradle.kts` raiz desde `84aff17` (17/09), `libs.versions.toml` nem declara ktlint (o engine vem pinado no plugin) e nunca existiu `.editorconfig`. As violações **sempre existiram** desde que os arquivos foram escritos — só ficaram visíveis agora com a primeira execução do `ktlintCheck` nesta track. Implicação: `lintCheck` da CI em `develop` provavelmente está vermelho desde ~set/out (status dos runs não verificado — sem `gh` CLI).
- Pendente: `./gradlew testDesktop` (autorização + compilação bloqueada por `reservar`/`liberarReserva` da Débora).

## Validação — compilação (autorizada)

- `./gradlew :serverApp:compileKotlin` **sem o contrato**: exatamente 3 erros, todos `Unresolved reference` nos call sites de `PedidoUseCase.atualizarStatus` (`reservar` linha 160; `liberarReserva` linhas 164, 181) — confirma que a única pendência do backend é a entrega da Débora.
- Verificação reversível do código de teste: stub temporário com **extension functions** (`internal fun EstoqueUseCase.reservar/liberarReserva`, corpo `TODO()`) em arquivo descartável — não tocou nos arquivos da Débora e extensions perdem para members quando os reais existirem. Com o stub: `:serverApp:compileKotlin` + `:shared:compileTestKotlinJvm` = **BUILD SUCCESSFUL** — fixture e as 4 classes de teste compilam limpas. Stub deletado em seguida; `git status` verificado sem rastro.
- Pendente real: `testDesktop` continua bloqueado — exige os métodos reais (execução dos efeitos de estoque) + `DATABASE_URL`/`.env.test.local`.

## Validação — teste manual via Swagger (executada, stub descartado)

- Swagger UI já existia no projeto: `springdoc.swagger-ui.url=/api/openapi.yaml` em `application.properties` → UI serve o spec manual (incluindo `Pedido`). URL: `http://localhost:8080/swagger-ui/index.html` via `bootRun` local (8080) ou `runTest` Docker (8081).
- Para destravar o boot sem a entrega da Débora, recriado `EstoquePedidoStubsTemp.kt` — desta vez **funcional**: extensions `reservar`/`liberarReserva` executando `reservada ± qtd` via Exposed **na transação ambiente** (join na `session` do `PedidoUseCase` — atomicidade real), com `forUpdate()` e guards (`reservada+qtd <= fisica`, `reservada-qtd >= 0` → `UnprocessableEntityException`). Arquivo marcado `NÃO COMMITAR` e **deletado após a sessão de teste** — `git status` verificado limpo.
- `bootRun` subiu na 8080 com `DATABASE_URL` de `.env.test.local`. **Atenção**: o arquivo tem `&` na URL → `source` quebra a atribuição (o `&` vira separador de job). Extração correta: `export DATABASE_URL="$(grep '^DATABASE_URL=' .env.test.local | cut -d= -f2-)"`. Daemon Gradle precisa ser criado DEPOIS do export (`--stop` antes) para propagar ao `bootRun`.
- Verificado no ar: `GET /api/pedido` → 200 com seeds+itens serializando no contrato; `GET /api/openapi.yaml` e `/api/openapi/pedido.yml` → 200 (refs resolvem); Swagger UI → 200. Usuário executou testes manuais e encerrou a sessão.
- App e daemon parados; stub removido. Resta: `testDesktop` após merge dos métodos reais da Débora.

## Pendências para o executor

- Implementar os 10 arquivos + 2 alterações listados no `Plan.md`.
- Validar com `./gradlew testDesktop` e `./gradlew ktlintCheck` (ver seção de ambiente do Worklog do CrudCliente: `.env.test.local`, `JAVA_HOME` se `java` não estiver no PATH, `--stop` de daemon Gradle se a env não pegar).
- Marcar os checkboxes do `Plan.md` conforme avanço.
