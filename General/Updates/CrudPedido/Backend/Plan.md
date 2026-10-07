# Plan — Backend: CRUD de Pedido

## TL;DR

Implementar o CRUD backend de `pedido` no `serverApp` seguindo o template `entity` → `repository` (Exposed) → `usecase` → `controller` (`/pedido`), fragmento OpenAPI e testes de integração em `shared/src/jvmTest`. Diferenças do template de `Categoria`/`Cliente`: pedido é um **agregado** — sempre persiste com seus itens (`pedido_item`) na mesma transação, sem CRUD independente; possui **máquina de status** com efeitos de estoque (reserva, liberação, baixa) consumindo métodos internos do módulo `Estoque`; **sem DELETE** — cancelamento é transição de status via `PATCH /pedido/{id}/status`.

**Dependência externa**: `EstoqueUseCase.reservar`/`liberarReserva` serão implementados pela desenvolvedora do estoque (Débora) — esta track assume o contrato definido na seção "Dependência: módulo Estoque" abaixo e não altera os arquivos dela.

## Schema das tabelas (fato — migrations V14, V15, V19, V20 já em `develop`)

`pedido` (V14): `id BIGSERIAL PK`, `cliente_id BIGINT` **nullable** FK→cliente, `funcionario_id BIGINT NN` FK→funcionario, `desconto_id BIGINT` nullable FK→desconto, `modalidade VARCHAR(30) NN` CHECK IN (`IMEDIATA`,`AGENDADA`), `status VARCHAR(30) NN DEFAULT 'ABERTO'` CHECK IN (5 status), `data_pedido TIMESTAMPTZ NN DEFAULT NOW()`, `data_retirada_prevista TIMESTAMPTZ` (CHECK: obrigatória quando `AGENDADA`), `data_retirada TIMESTAMPTZ`, `valor_desconto NUMERIC(12,2) NN DEFAULT 0` CHECK >=0, `valor_total NUMERIC(12,2) NN DEFAULT 0` CHECK >=0, `observacao TEXT`.

`pedido_item` (V15): `id BIGSERIAL PK`, `pedido_id BIGINT NN` FK→pedido **ON DELETE CASCADE**, `produto_id BIGINT NN` FK→produto, `quantidade INT NN` CHECK >0, `preco_unitario NUMERIC(12,2) NN` CHECK >=0.

`estoque` (V19): `produto_id BIGINT PK` FK→produto, `quantidade_fisica`, `quantidade_reservada`, `quantidade_minima` — INT NN >=0 e CHECK `reservada <= fisica`.

`movimentacao_estoque` (V20): `id`, `produto_id NN`, `pedido_id` nullable, `compra_id` nullable, `funcionario_id` nullable, `tipo` CHECK IN (`ENTRADA`,`SAIDA`,`AJUSTE_ENTRADA`,`AJUSTE_SAIDA`,`PERDA`), `quantidade` INT NN >0, `observacao`, `data_hora DEFAULT NOW()`. **Não existe tipo RESERVA** — reservas são apenas updates em `estoque.quantidade_reservada`; conclusão gera `SAIDA` por item (contrato já implementado: `EstoqueUseCase.validarOrigem` exige `pedido_id` para `SAIDA`).

## Dependência: módulo Estoque (contrato assumido — implementação pela dev do estoque)

Esta track **não altera** `EstoqueRepository`/`EstoqueUseCase`. O pedido consome:

- `registrarMovimentacaoInterna(produtoId, tipo, quantidade, observacao, pedidoId, compraId, funcionarioId)` — **já existe** (`internal`): valida origem (`SAIDA` exige `pedidoId`), ajusta `quantidade_fisica` com `.forUpdate()` + guard `novaFisica >= reservada` e insere a movimentação. Erro de estoque insuficiente → `UnprocessableEntityException`.
- `reservar(produtoId: Long, quantidade: Int)` — **a ser implementado por Débora**: `reservada += quantidade`, rejeitando quando exceder `fisica` (`UnprocessableEntityException`).
- `liberarReserva(produtoId: Long, quantidade: Int)` — **a ser implementado por Débora**: `reservada -= quantidade`, rejeitando resultado negativo.
- `buscarPosicaoPorProdutoId(produtoId)` — **já existe**: devolve `Estoque` com `quantidadeDisponivel` (usado na validação de disponibilidade da criação).

O executor deve verificar, ao iniciar, se `reservar`/`liberarReserva` já existem na branch; se ainda não existirem, os passos de `atualizarStatus` ficam bloqueados para compilação — alinhar a assinatura com a dev do estoque antes de codar (ver `Questions.md`).

Convenções confirmadas pelo merge do crud-estoque: table objects privados com sufixo `Table` (`EstoquesTable`), enums em `org.greenhouse.enums` (um arquivo por enum), DTOs de request em arquivo próprio em `controller/` (`RegistrarMovimentacaoEstoqueRequest.kt`).

## Regras de domínio — transições e efeitos de estoque (contrato)

Fontes: `AGENTS.md` + user stories + respostas do usuário neste planejamento. `UnprocessableEntityException` mapeia para HTTP 400 neste projeto (`ExceptionConfig`).

| Transição | Modalidade | Efeito de estoque (na mesma transação) | Outros efeitos |
|---|---|---|---|
| criação (`ABERTO`) | ambas | nenhum — só valida disponibilidade | — |
| `ABERTO → CONFIRMADO` | `AGENDADA` | por item: `reservar(produtoId, qtd)` → `reservada += qtd` guardado por `reservada + qtd <= fisica` (sem linha de estoque ou guard falho → erro) | — |
| `ABERTO → CONFIRMADO` | `IMEDIATA` | nenhum | — |
| `CONFIRMADO → PRONTO_RETIRADA` | ambas | nenhum (mantém reserva) | — |
| `PRONTO_RETIRADA → CONCLUIDO` | ambas | `AGENDADA`: por item `liberarReserva` **antes** da saída; por item `registrarMovimentacaoInterna(SAIDA, pedidoId, funcionarioId)` → `fisica -= qtd` com guard `novaFisica >= reservada` | `data_retirada = NOW()` |
| `CONFIRMADO → CONCLUIDO` | **somente `IMEDIATA`** | idem sem liberar reserva (o guard `novaFisica >= reservada` já impõe disponibilidade) | idem; `AGENDADA` nesta transição → `UnprocessableEntityException` |
| `CONFIRMADO → CANCELADO` | ambas | `AGENDADA`: `liberarReserva` por item; `IMEDIATA`: nenhum | — |
| `PRONTO_RETIRADA → CANCELADO` | ambas | idem | — |
| `ABERTO → CANCELADO` | ambas | nenhum | — |
| qualquer a partir de `CONCLUIDO`/`CANCELADO` | — | — | rejeitar (terminais) |
| `ABERTO → PRONTO_RETIRADA`/`CONCLUIDO` e demais não listadas | — | — | rejeitar |

**Atomicidade**: as transições com efeito tocam `pedido` + `estoque` + `movimentacao_estoque` — tudo dentro de `databaseConfig.session { }` aberta pelo **use case**. `DatabaseConfig` é `@Bean` único (`Main.kt`) e `session {}` usa `transaction {}` do Exposed: chamadas aninhadas de `session` sobre o mesmo `Database` executam dentro da transação corrente (join implícito); qualquer exceção que escape aborta tudo. Ordem obrigatória na conclusão de `AGENDADA`: liberar reserva **antes** de registrar a `SAIDA` (o guard `novaFisica >= reservada` só passa com a reserva já baixada).

## Steps

- [ ] **Pré-requisito 1:** confirmar que `pedido`, `pedido_item`, `estoque` e `movimentacao_estoque` existem no banco dev (migrations aplicadas). Os testes de integração provam isso ao passar — pendente da execução de `testDesktop`.
- [x] **Pré-requisito 2 (dependência externa):** verificado — `reservar`/`liberarReserva` **ainda não existem** na `develop` mergeada. Usuário decidiu: assumir o contrato (`EstoqueUseCase.reservar(produtoId, quantidade)`/`liberarReserva(produtoId, quantidade)`); compilação/testes aguardam a entrega pela dev do estoque.
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/enums/ModalidadePedido.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/enums/StatusPedido.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/entity/Pedido.kt` e `entity/PedidoItem.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/repository/PedidoRepository.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/usecase/PedidoUseCase.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/controller/AtualizarStatusPedidoRequest.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/controller/PedidoController.kt`
- [x] Criar `serverApp/src/main/resources/openapi/pedido.yml` e registrar refs em `openapi.yaml`; removida a ref quebrada `/desconto/{id}/status` (autorizada pelo dono do CRUD de desconto)
- [x] Adicionar `"pedido"` ao whitelist de `OpenApiController.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/fixture/PedidoFixture.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/pedido/TestPedidoPost.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/pedido/TestPedidoGet.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/pedido/TestPedidoPatch.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/pedido/TestPedidoStatus.kt`
- [ ] Rodar `./gradlew testDesktop` — **bloqueado**: depende de `reservar`/`liberarReserva` (Débora) para compilar + `DATABASE_URL`. Compilação já verificada via stub temporário descartado: `:serverApp:compileKotlin` + `:shared:compileTestKotlinJvm` passam — o único erro real sem o contrato são os 3 call sites em `PedidoUseCase.atualizarStatus` (ver Worklog)
- [x] Rodar `./gradlew ktlintCheck` — executado com autorização; **arquivos novos da track sem nenhuma violação** após correções (`PedidoRepository` chain-continuation; `TestPedidoPatch`/`TestPedidoStatus` multiline-expression-wrapping). Check global falha apenas por violações **preexistentes** (ktlint mais estrito veio no merge da `develop` — `desktopApp/main.kt`, `serverApp/build.gradle.kts`, `SharedLogicAndroidHostTest`, vários `serverApp`/testes antigos) — fora de escopo, registrado no Worklog.

## Detalhes de implementação

### Enums (`enums/` — convenção atual de `develop`)

```kotlin
// enums/ModalidadePedido.kt
enum class ModalidadePedido { IMEDIATA, AGENDADA }

// enums/StatusPedido.kt
enum class StatusPedido { ABERTO, CONFIRMADO, PRONTO_RETIRADA, CONCLUIDO, CANCELADO }
```

### Entities (`entity/Pedido.kt`, `entity/PedidoItem.kt` — um arquivo por classe, convenção atual)

```kotlin
data class PedidoItem(
    val id: Long? = null,
    val produtoId: Long,
    val quantidade: Int,
    val precoUnitario: BigDecimal? = null, // definido pelo backend
)

data class Pedido(
    val id: Long? = null,
    val clienteId: Long? = null,
    val funcionarioId: Long,
    val descontoId: Long? = null,
    val modalidade: ModalidadePedido,
    val status: StatusPedido = StatusPedido.ABERTO,
    val dataPedido: OffsetDateTime? = null,
    val dataRetiradaPrevista: OffsetDateTime? = null,
    val dataRetirada: OffsetDateTime? = null,
    val valorDesconto: BigDecimal = BigDecimal.ZERO,
    val valorTotal: BigDecimal = BigDecimal.ZERO,
    val observacao: String? = null,
    val itens: List<PedidoItem> = emptyList(),
)
```

Entity é usada como request/response (convenção `Cliente`/`Desconto`). Jackson + `jackson-module-kotlin` já presentes; `timestampWithTimeZone` ↔ `OffsetDateTime` conforme `DescontoRepository`. `modalidade`/`status`/`funcionarioId`/`itens` ausentes ou enum inválido no POST → `HttpMessageNotReadableException` → 400 automático.

### Estoque consumido pelo pedido (não implementar — dependência da dev do estoque)

O use case chama, na ordem exigida por cada transição:

- `estoqueUseCase.reservar(produtoId, quantidade)` — `ABERTO→CONFIRMADO` de `AGENDADA`.
- `estoqueUseCase.liberarReserva(produtoId, quantidade)` — conclusão de `AGENDADA` (antes da `SAIDA`) e cancelamento com reserva ativa.
- `estoqueUseCase.registrarMovimentacaoInterna(produtoId, SAIDA, quantidade, observacao, pedidoId, funcionarioId)` — conclusão (qualquer modalidade).
- `estoqueUseCase.buscarPosicaoPorProdutoId(produtoId)` — checagem de disponibilidade na criação/edição de itens.

### Repository (`repository/PedidoRepository.kt`)

`private object`s Exposed (file-private, sem conflito): `Pedidos : Table("pedido")` (todas as colunas da V14), `PedidoItens : Table("pedido_item")`, `Funcionarios : Table("funcionario")` com só `id` + `ativo` (não existe `FuncionarioRepository`; estoque usa tabelas privadas do mesmo jeito). **Não** recriar tabelas de `estoque`/`movimentacao_estoque`/`produto`/`cliente`/`desconto` — essas responsabilidades ficam nos módulos próprios.

Métodos — cada um em um `databaseConfig.session { }`; os de transição serão chamados **dentro** da session do use case (join na transação externa):

- `criar(pedido: Pedido): Pedido` — insert do pedido (omite `id`, `status`, `data_pedido`, `data_retirada` → defaults) + insert dos itens + re-select para devolver `dataPedido` populado.
- `listar(clienteId: Long?, funcionarioId: Long?, status: StatusPedido?, modalidade: ModalidadePedido?): List<Pedido>` — `selectAll` + `andWhere` por filtro; itens em **uma** query `pedidoId inList ids` agrupada (sem N+1); ordenar por `dataPedido` desc (documentar escolha).
- `buscarPorId(id: Long): Pedido?` — pedido + itens.
- `bloquearPorId(id: Long): Pedido?` — igual ao `buscarPorId` + `.forUpdate()`; usado dentro da session do `atualizarStatus` (precedente `.forUpdate()` em `EstoqueRepository`).
- `atualizar(id: Long, pedido: Pedido, substituirItens: Boolean): Pedido?` — update dos campos editáveis; se `substituirItens`, `deleteWhere` dos itens + insert dos novos, mesma session.
- `atualizarStatus(id: Long, status: StatusPedido, dataRetirada: OffsetDateTime?): Pedido?` — apenas o update da linha do pedido; os efeitos de estoque são orquestrados pelo use case na mesma transação.
- `funcionarioAtivo(id: Long): Boolean?` — `null` se inexistente.
- `excluir(id: Long): Boolean` — **somente para cleanup de fixture** (itens caem por CASCADE); não exposto no controller.

### Use case (`usecase/PedidoUseCase.kt`)

Injeção: `DatabaseConfig` + `PedidoRepository` + `ClienteRepository` + `ProdutoRepository` + `DescontoRepository` + `EstoqueUseCase`. `DatabaseConfig` no use case é padrão novo do projeto — justificado porque o `session {}` dele é a fronteira da unidade de trabalho que atravessa `pedido` e `estoque` (ver Decisões).

`criar(pedido)`:

1. Normalizar: `copy(id=null, status=ABERTO, dataPedido=null, dataRetirada=null)`; `valorDesconto`/`valorTotal`/`precoUnitario` ignorados — sempre calculados.
2. `AGENDADA` exige `dataRetiradaPrevista != null` → `UnprocessableEntityException`; quando informada (qualquer modalidade), deve ser do **dia corrente em diante** — comparar por data (`toLocalDate()` >= `LocalDate.now()` no fuso do servidor), não por instante → `UnprocessableEntityException`("A data de retirada prevista não pode ser anterior a hoje").
3. `funcionarioId` >0; `funcionarioAtivo` → `null` = `UnprocessableEntityException`("Funcionário não encontrado"), `false` = "Funcionário inativo".
4. `clienteId != null` → `clienteRepository.buscarPorId` → inexistente/inativo → `UnprocessableEntityException` (enforcement pendente registrado no Questions do CrudCliente).
5. `itens` ≥1; `produtoId` >0 e sem duplicados; `quantidade` >0 → `UnprocessableEntityException`.
6. `produtoRepository.buscarPorId` por item → existe e `ativo`.
7. Disponibilidade: `estoqueUseCase.buscarPosicaoPorProdutoId(produtoId).quantidadeDisponivel >= quantidade` (produto sem linha de estoque → disponível = 0) → `UnprocessableEntityException`.
8. `descontoId != null` → `descontoRepository.buscarPorId` → existe, `ativo`, vigente (`dataInicio <= now <= dataFim` quando definidas).
9. Calcular: `precoUnitario = produto.preco` (snapshot); `subtotal = Σ qtd*preco`; `valorDesconto` = `PERCENTUAL` → `subtotal * valor / 100` `setScale(2, HALF_UP)`; `VALOR_FIXO` → `min(valor, subtotal)`; `valorTotal = subtotal - valorDesconto`.
10. `pedidoRepository.criar` — já transacional sozinho (não precisa de session do use case).

`listar(clienteId, funcionarioId, status, modalidade)` — filtros chegam `String?` no controller; parse de `status`/`modalidade` com `valueOf` → inválido → `BadRequestException`.

`buscarPorId(id)` — `validarId` + `NotFoundException`.

`atualizar(id, campos: Map<String, Any?>)`:

- `camposPermitidos = {id, clienteId, funcionarioId, descontoId, dataRetiradaPrevista, observacao, itens}` — desconhecido (inclui `status`, `modalidade`, `valorTotal`, `valorDesconto`, `dataPedido`, `dataRetirada`) → `BadRequestException`; sem campo editável → `BadRequestException`.
- Gate de status: `ABERTO` permite todos os campos; `CONFIRMADO` permite todos **exceto `itens`** (enviar `itens` → `UnprocessableEntityException`); `PRONTO_RETIRADA`/`CONCLUIDO`/`CANCELADO` → `UnprocessableEntityException` para qualquer campo.
- `itens` = substituição total (mesmas validações da criação + revalida disponibilidade) → recalcula totais.
- `descontoId`/`itens` alterados → recalcula `valorDesconto`/`valorTotal`.
- `clienteId`/`descontoId` aceitam `null` explícito; `dataRetiradaPrevista` não pode virar `null` quando `modalidade == AGENDADA` e, quando informada, deve ser do dia corrente em diante (mesma regra da criação).
- Helpers `Map` novos: `longo`/`longoOpcional` (ids chegam `Number`), `instante`/`instanteOpcional` (padrão `data()` de `DescontoUseCase` com `OffsetDateTime.parse`), `itens` (converte `List<Map<String,Any?>>` → `PedidoItem`: `produtoId`/`quantidade` como `Number`; chave desconhecida no item → `BadRequestException`).

`atualizarStatus(id, request.status)`:

```kotlin
fun atualizarStatus(id: Long, novoStatus: StatusPedido): Pedido =
    databaseConfig.session {
        val pedido = pedidoRepository.bloquearPorId(validarId(id))
            ?: throw NotFoundException("Pedido não encontrado")
        // validar transição na tabela acima → UnprocessableEntityException("Transição de X para Y não é permitida")
        // aplicar efeitos na ordem correta via estoqueUseCase (reservar/liberarReserva/registrarMovimentacaoInterna)
        // pedidoRepository.atualizarStatus(id, novoStatus, dataRetirada = se CONCLUIDO agora)
    }
```

`status` inválido no JSON → 400 automático (enum tipado no DTO, precedente `RegistrarMovimentacaoEstoqueRequest`).

### Controller — `@RequestMapping("/pedido")`

`controller/AtualizarStatusPedidoRequest.kt` (arquivo próprio, convenção `RegistrarMovimentacaoEstoqueRequest.kt`):

```kotlin
data class AtualizarStatusPedidoRequest(val status: StatusPedido)
```

| Rota | Sucesso | Erros |
|---|---|---|
| `POST /pedido` | 201 + `Pedido` com itens | 400 |
| `GET /pedido?clienteId=&funcionarioId=&status=&modalidade=` | 200 + lista com itens (vazia = `[]`) | 400 (filtro inválido) |
| `GET /pedido/{id}` | 200 + `Pedido` com itens | 400, 404 |
| `PATCH /pedido/{id}` | 200 + `Pedido` com itens | 400, 404 |
| `PATCH /pedido/{id}/status` body `{"status":"..."}` | 200 + `Pedido` | 400, 404 |

**Sem DELETE.**

### OpenAPI

- `openapi/pedido.yml` (modelo: `openapi/desconto.yml`): paths `/pedido`, `/pedido/{id}`, `/pedido/{id}/status`; schemas `Pedido`, `PedidoItem`, `PedidoCadastro` (itens obrigatórios com `produtoId`+`quantidade`), `PedidoPatch`, `AtualizarStatusPedido`; documentar os 4 query params e a tabela de transições no `description` do endpoint de status.
- `openapi.yaml`: tag `Pedido` + refs dos 3 paths + schemas `Pedido`/`PedidoItem` em `components`. **Remover a ref quebrada `/desconto/{id}/status`** (linhas 39-40 — endpoint removido do `DescontoController` no merge do crud-estoque; correção autorizada pelo dono do CRUD de desconto).
- `OpenApiController.kt`: adicionar `"pedido"` ao `when`.

### Testes de integração (`shared/src/jvmTest/.../integration/pedido/`)

`PedidoFixture` — **todos os repositories/use cases da fixture devem compartilhar a MESMA instância de `DatabaseConfig`** (é o que faz as sessions aninhadas se juntarem numa transação — em produção o Spring injeta o mesmo bean). Helpers JDBC (modelo `DescontoFixture`): `novoFuncionario()` (cpf 11 dígitos via `UUID`), `novoCliente()`, `novoDesconto()`, `novoProdutoComEstoque(fisica, reservada)` (pode compor `EstoqueFixture` ou replicar o JDBC dela), `posicaoEstoque(produtoId)` e `movimentacoesDoPedido(pedidoId)` via JDBC para asserções. Cleanup na ordem: `movimentacao_estoque` → `pedido` (itens CASCADE) → `estoque` → `produto` → `desconto` → `cliente` → `funcionario`.

Casos mínimos:

- **Post:** cria `ABERTO` com itens e totais calculados; `id`/`status`/`valorTotal`/`precoUnitario` enviados ignorados; `AGENDADA` sem `dataRetiradaPrevista` → erro; sem itens / `quantidade` <= 0 → erro; produto inexistente/inativo → erro; `produtoId` duplicado → erro; cliente inexistente/inativo → erro; funcionário inexistente/inativo → erro; disponibilidade insuficiente → erro; desconto inativo/fora de vigência → erro; `PERCENTUAL` aplica %; `VALOR_FIXO` > subtotal trunca; `dataRetiradaPrevista` anterior ao dia corrente → erro (`AGENDADA` e, se informada, `IMEDIATA`).
- **Get:** lista com itens; filtros `clienteId`, `status`, `modalidade`, `funcionarioId`; filtro inválido → 400; detalhe com itens; 404 e id inválido.
- **Patch:** editar `observacao`/`descontoId`/`dataRetiradaPrevista` em `ABERTO`; substituir `itens` em `ABERTO` recalcula total; `itens` em `CONFIRMADO` → erro; qualquer campo em `PRONTO_RETIRADA`/`CONCLUIDO`/`CANCELADO` → erro; campo desconhecido → 400; `status`/`modalidade`/`valorTotal` no patch → 400; `dataRetiradaPrevista` no passado → erro; `null` em `descontoId`; body vazio → 400.
- **Status:** `ABERTO→CONFIRMADO` AGENDADA sobe `reservada`; `CONFIRMADO→PRONTO_RETIRADA` mantém; `PRONTO_RETIRADA→CONCLUIDO` baixa `fisica`+`reservada`, grava `data_retirada` e cria `SAIDA` por item; IMEDIATA `CONFIRMADO→CONCLUIDO` direto (só `fisica`); AGENDADA `CONFIRMADO→CONCLUIDO` → erro; `ABERTO→CANCELADO`; `CONFIRMADO→CANCELADO` AGENDADA libera reserva; `PRONTO_RETIRADA→CANCELADO` libera; `CONCLUIDO→*`/`CANCELADO→*`/`ABERTO→PRONTO_RETIRADA`/`ABERTO→CONCLUIDO` → erro; reserva acima do disponível → erro; status string inválida → 400; id inexistente → 404.

## Arquivos relevantes

**Criar:**

- `serverApp/src/main/kotlin/org/greenhouse/enums/ModalidadePedido.kt`
- `serverApp/src/main/kotlin/org/greenhouse/enums/StatusPedido.kt`
- `serverApp/src/main/kotlin/org/greenhouse/entity/Pedido.kt`
- `serverApp/src/main/kotlin/org/greenhouse/entity/PedidoItem.kt`
- `serverApp/src/main/kotlin/org/greenhouse/repository/PedidoRepository.kt`
- `serverApp/src/main/kotlin/org/greenhouse/usecase/PedidoUseCase.kt`
- `serverApp/src/main/kotlin/org/greenhouse/controller/PedidoController.kt`
- `serverApp/src/main/kotlin/org/greenhouse/controller/AtualizarStatusPedidoRequest.kt`
- `serverApp/src/main/resources/openapi/pedido.yml`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/pedido/TestPedido{Post,Get,Patch,Status}.kt`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/fixture/PedidoFixture.kt`

**Alterar:**

- `serverApp/src/main/resources/openapi.yaml` (tag `Pedido`, 3 path refs, schemas `Pedido`/`PedidoItem`, remoção da ref quebrada `/desconto/{id}/status` — autorizada)
- `serverApp/src/main/kotlin/org/greenhouse/controller/OpenApiController.kt` (whitelist)

**Não alterar:** migrations, `DatabaseConfig`, `ExceptionConfig`, `GlobalExceptionHandler`, `WebConfig`, repositories de `Cliente`/`Produto`/`Desconto` (consumidos como leitura), **`EstoqueRepository`/`EstoqueUseCase`/`EstoqueController` (módulo da dev do estoque — consumido via contrato, ver Dependência)**, `shared/src/commonMain`, apps cliente.

## Como verificar

- `./gradlew testDesktop` — testes de pedido + regressão dos demais (requer `.env.test.local` com `DATABASE_URL`; ver Worklog do CrudCliente: `JAVA_HOME=/opt/android-studio/jbr`, `./gradlew --stop` se a env não pegar no daemon).
- `./gradlew ktlintCheck` — arquivos novos sem violações (violações preexistentes em `serverApp/build.gradle.kts` e `desktopApp/main.kt` são conhecidas e fora de escopo).
- Manual (opcional): `./gradlew --no-configuration-cache :serverApp:runTest` e exercitar `POST/GET/PATCH /api/pedido` + `PATCH /api/pedido/{id}/status` na porta 8081; conferir `/api/openapi/pedido.yml` servido.

## Decisões tomadas

- **Cancelamento de `CONFIRMADO` e `PRONTO_RETIRADA` permitido** (além de `ABERTO`→`CANCELADO` do AGENTS.md): a user story de estoque exige "liberar reserva conforme cancelamento" — só existe reserva nesses estados. Transições do AGENTS.md permanecem válidas; `CONCLUIDO`/`CANCELADO` terminais. Registrado em `Questions.md` para confirmação do time.
- **Reuso do módulo `Estoque` em vez de tabelas duplicadas**: `feat/crud-estoque` foi mergeada em `develop`; `registrarMovimentacaoInterna` foi projetada para este consumo. `reservar`/`liberarReserva` são **implementados pela dev do estoque** — esta track assume o contrato (assinatura + `UnprocessableEntityException` em insuficiência) e não toca nos arquivos dela.
- **`DatabaseConfig` injetado em `PedidoUseCase` como fronteira da unidade de trabalho**: transições de status atravessam `pedido` + `estoque` + `movimentacao_estoque`; um `session {}` do use case com chamadas aninhadas de repository/use case garante atomicidade (join implícito do Exposed no mesmo `Database`). Padrão novo, justificado pela exigência de "consistência transacional" da user story de estoque. Leitura do pedido dentro da session usa `forUpdate` (`bloquearPorId`) para serializar transições concorrentes.
- **`preco_unitario` = snapshot de `produto.preco`** (resposta do usuário): cliente envia só `produtoId`+`quantidade`.
- **`clienteId` opcional, validado se informado** (resposta do usuário + coluna nullable): existe + `ativo`.
- **`funcionarioId` no payload** (sem auth no backend — mesmo caso do CrudCliente): obrigatório, existe + `ativo`.
- **Itens só editáveis em `ABERTO`; demais campos também em `CONFIRMADO`**: depois de `CONFIRMADO` existe reserva para `AGENDADA` — bloquear edição de itens mantém reserva coerente; campos descritivos não afetam reserva.
- **`modalidade` não editável via PATCH**: mudar `IMEDIATA`↔`AGENDADA` altera semântica de reserva; cancelar e recriar é o caminho.
- **Disponibilidade validada na criação para ambas as modalidades** e de novo transacionalmente ao reservar/concluir. Se o time decidir que `AGENDADA` pode nascer sem estoque, é alteração de uma linha — ver `Questions.md`.
- **`dataRetiradaPrevista` >= dia corrente** (resposta do usuário): retirada prevista é sempre hoje ou futuro, validada na criação e no PATCH — comparação por data (`LocalDate`), não por instante, para permitir retirada "hoje mais cedo" sem rejeitar.
- **Ref quebrada `/desconto/{id}/status` removida de `openapi.yaml`**: autorizada pelo dono do CRUD de desconto; o endpoint saiu do controller no merge do crud-estoque e a ref ficou órfã.
- **`valorDesconto`/`valorTotal` calculados no backend**: `PERCENTUAL` sobre subtotal; `VALOR_FIXO` truncado no subtotal (`valorTotal` nunca negativo). Desconto deve estar ativo e vigente.
- **Listagem inclui itens** (carga em lote via `IN`): contrato único `Pedido` com `itens` em listagem e detalhe.
- **`PATCH /pedido/{id}/status` dedicado com enum tipado no DTO** (`AtualizarStatusPedidoRequest`): o precedente `/desconto/{id}/status` foi removido no merge do crud-estoque (status de desconto virou campo `ativo` no PATCH), mas status de pedido é máquina de transições com efeitos colaterais — não cabe num PATCH genérico de campos.
- **Sem DELETE e sem CRUD de `pedido_item` isolado** (exigência explícita); cancelamento via transição de status.
