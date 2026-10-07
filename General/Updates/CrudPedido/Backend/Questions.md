# Questions — Backend: CRUD de Pedido

## Open

- **[confirmar com o time]** Cancelamento de `CONFIRMADO`/`PRONTO_RETIRADA`: `AGENTS.md` lista explicitamente só `ABERTO -> CANCELADO`, mas a user story de estoque exige liberação de reserva no cancelamento. Plano implementa cancelamento a partir de `ABERTO`, `CONFIRMADO` e `PRONTO_RETIRADA` (com liberação de reserva para `AGENDADA`); `CONCLUIDO` segue terminal. Se o time decidir restringir a `ABERTO`, basta reduzir a matriz de transições no use case.
- `AGENDADA` pode ser criada sem disponibilidade atual de estoque? Plano valida disponibilidade na criação das duas modalidades (critério "disponibilidade validada no backend"); para agendados, estoque pode ser reposto por compra até a retirada — se o time preferir adiar a checagem para `CONFIRMADO`, remover o check de criação apenas para `AGENDADA`.
- `funcionarioId` deve exigir funcionário `ativo`? Plano exige existir + ativo. Quando houver auth, o ideal é derivar do usuário logado e remover do payload.
- **[bloqueante — apenas para execução de testes]** `reservar`/`liberarReserva` ainda não existem na `develop`: a branch **não compila** até a Débora entregá-los. Usuário decidiu assumir o contrato `EstoqueUseCase.reservar(produtoId: Long, quantidade: Int)`/`liberarReserva(produtoId: Long, quantidade: Int)` (erro de insuficiência = `UnprocessableEntityException`). Se a assinatura entregue divergir, ajustar as chamadas em `PedidoUseCase.atualizarStatus` e na `PedidoFixture`. Compilação já verificada com stub temporário descartado: todo o resto (backend + jvmTest) compila limpo — os únicos erros sem o contrato são os 3 call sites documentados no Worklog.

## Resolved

- **[respondida pelo usuário]** De quais status cancelar? → User story de estoque indica liberação de reserva no cancelamento → `ABERTO`, `CONFIRMADO` e `PRONTO_RETIRADA` (ver item em Open para confirmação final do time).
- **[respondida pelo usuário]** Origem de `preco_unitario` → snapshot de `produto.preco` calculado no backend; cliente envia só `produtoId` + `quantidade`.
- **[respondida pelo usuário]** `cliente_id` obrigatório? → Opcional (coluna nullable); se informado, cliente deve existir e estar ativo.
- Transições válidas? → `ABERTO→CONFIRMADO→PRONTO_RETIRADA→CONCLUIDO`; `IMEDIATA` pode `CONFIRMADO→CONCLUIDO`; `CANCELADO` a partir de `ABERTO`/`CONFIRMADO`/`PRONTO_RETIRADA`; `CONCLUIDO`/`CANCELADO` terminais. Demais transições → erro.
- DELETE físico de pedido? → Não. Cancelamento é fluxo de status; `pedido_item` não tem CRUD próprio (substituição total via PATCH do pedido em `ABERTO`).
- Onde ficam `ModalidadePedido`/`StatusPedido`? → `enums/` (convenção confirmada em `develop` após merge do crud-estoque: `TipoDesconto` e `TipoMovimentacaoEstoque` vivem lá).
- Duplicar tabelas de estoque no `PedidoRepository`? → Não. `feat/crud-estoque` já está em `develop`; reusar `EstoqueUseCase.registrarMovimentacaoInterna` + `reservar`/`liberarReserva` — estes dois **implementados pela dev do estoque** (contrato assumido, ver Open). Atomicidade via `session {}` do use case (mesmo `Database` do bean único).
- Como a fixture monta o grafo de dependências? → Uma única instância de `DatabaseConfig` compartilhada por todos os repositories/use cases da fixture (é o que faz as sessions aninhadas se juntarem na mesma transação).
- **[respondida pelo usuário]** `data_retirada_prevista` pode ser no passado? → **Não.** Deve ser sempre do dia vigente em diante (hoje ou futuro) — validar na criação e no PATCH quando o campo for informado.
- **[respondida pelo usuário]** Ref quebrada `/desconto/{id}/status` em `openapi.yaml` (endpoint removido do `DescontoController` no merge do crud-estoque) → **correção autorizada pelo dono do CRUD de desconto**: remover a ref nesta track.
- **[respondida pelo usuário]** `PRONTO_RETIRADA`/`CANCELADO` devem popular `movimentacao_estoque`? → **Não.** O CHECK de `tipo` (V20) só aceita `ENTRADA`/`SAIDA`/`AJUSTE_*`/`PERDA` e todo insert via `EstoqueRepository` altera `quantidade_fisica` — gravar auditoria de reserva exigiria migration nova; usuário decidiu dispensar. `movimentacao_estoque` só recebe `SAIDA` por item na conclusão.
- Enforcement de perfil FUNCIONARIO/ADMIN? → Impossível hoje (sem auth no backend, mesmo caso do CrudCliente); revisitar quando auth existir.
