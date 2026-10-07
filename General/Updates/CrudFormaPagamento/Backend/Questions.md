# Questions — Backend: CRUD de Forma de Pagamento

## Open

Nenhuma.

## Resolved

- **`nome` vazio/em branco:** rejeitar. A validação do use case exige nome não blank e respeita o máximo de 50 caracteres da coluna.
- **Nome duplicado:** o solicitante considerou irrelevante definir um contrato HTTP específico nesta tarefa. Manter a constraint `UNIQUE` existente e não alterar o tratamento global de exceções nem prometer um status específico.
- **É necessária migration?** Não. `V9__create_forma_pagamento.sql` já cria a tabela e `V16__create_pagamento.sql` já a referencia.
- **Qual o comportamento de DELETE?** Planejado como inativação (`ativo=false`), seguindo Desconto e preservando referências históricas de pagamentos. Decisão autônoma baseada no schema; confirmar caso o requisito pretendesse exclusão física.
- **Quais perfis podem operar?** Qualquer identidade autenticada com JWT válido; incluir testes com `ADMIN` e `FUNCIONARIO`. Não adicionar regra de perfil.
- **Há consumidor cliente atual?** Não foi encontrada referência a forma de pagamento no código de `shared` ou nos apps durante a busca.
