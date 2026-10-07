# Scope — CrudPedido

Backend do CRUD de pedidos no `serverApp`, seguindo o template de `docs/architecture/crud_template.md` (referência: `Categoria`), com pedido como agregado transacional (pedido + `pedido_item` na mesma transação) e máquina de status integrada ao módulo `Estoque` (já em `develop`) para reserva, liberação e baixa com movimentações `SAIDA`.

Cobre: registro de pedido com um ou mais itens, consulta (listagem com filtros + detalhe com itens), atualização parcial condicionada ao status atual, cancelamento lógico (sem exclusão física) e gerenciamento de transições de status com efeitos de estoque.

Dependência externa: `EstoqueUseCase.reservar`/`liberarReserva` serão implementados pela desenvolvedora do estoque — a track assume o contrato e não altera os arquivos dela.

Fora de escopo: telas/clientes multiplataforma, autenticação/autorização por perfil (backend ainda não possui auth — `funcionarioId` chega no payload), CRUD independente de `pedido_item` (itens sempre via pedido), pagamentos (`pagamento`), compras de fornecedor e o módulo de estoque em si (`EstoqueController`, `EstoqueRepository`, `EstoqueUseCase` — apenas consumidos).

Nenhuma migration nova é necessária: `pedido` (V14), `pedido_item` (V15), `estoque` (V19) e `movimentacao_estoque` (V20) já existem em `develop`.

## Tracks

- Backend — 80% (implementação concluída; pendente compilação/testes, bloqueados até a entrega de `reservar`/`liberarReserva` pela dev do estoque)
