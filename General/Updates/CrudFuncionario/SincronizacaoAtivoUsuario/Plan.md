# Plan — Sincronização do status da conta do funcionário

## TL;DR

Ao persistir uma atualização de funcionário, espelhar seu `ativo` em `usuario.ativo` pelo relacionamento existente `usuario.funcionario_id`, na mesma `DatabaseConfig.session` que atualiza `funcionario`. Isso cobre o DELETE, que atualmente desativa o funcionário no use case, e o PATCH que altera explicitamente `ativo`. Não mudar endpoints, contratos, outras regras de conta ou JWT emitidos; não há migration.

## Estado atual confirmado

- `FuncionarioController.desativar` chama `FuncionarioUseCase.desativar`; o use case copia o funcionário com `ativo = false` e chama `FuncionarioRepository.atualizar`.
- `FuncionarioUseCase.atualizar` aceita `ativo` no PATCH, preserva o valor atual quando o campo não é informado e também persiste pelo método `FuncionarioRepository.atualizar`.
- `FuncionarioRepository.atualizar` executa o UPDATE do funcionário e a leitura subsequente dentro de uma única `databaseConfig.session { ... }`. `DatabaseConfig.session` abre uma transação Exposed em torno do bloco.
- O repositório já mapeia `UsuarioTable`, incluindo `ativo` e `funcionario_id`; esse campo referencia `FuncionarioTable.id`.
- A migration V3 declara `usuario.funcionario_id` como FK e `UNIQUE`, portanto há no máximo uma conta vinculada a cada funcionário. A FK não garante que todo funcionário tenha uma conta.
- O método `FuncionarioRepository.excluir` faz DELETE físico, mas não é o caminho utilizado pela rota DELETE do controller; não deve ser introduzido no fluxo de desativação.
- O repositório não contém hoje atualização de `usuario.ativo` ao atualizar funcionário. `UsuarioRepository` também não expõe operação de status aplicável a esse fluxo.
- Os testes existentes em `serverApp/src/test` cobrem segurança, autenticação e provisionamento; não há teste de persistência para `FuncionarioRepository`. `serverApp/build.gradle.kts` inclui o driver PostgreSQL e `spring-boot-starter-test`, mas não declara H2 nem Testcontainers.
- Não há contrato de cliente ou API a alterar: DELETE e PATCH mantêm suas rotas e payloads. Nenhum módulo cliente, `shared` ou schema/migration precisa mudar.

## Steps

- [x] Alterar `FuncionarioRepository.atualizar` para, dentro do bloco já existente `databaseConfig.session`, atualizar `UsuarioTable.ativo` para `funcionario.ativo` usando o vínculo `UsuarioTable.funcionarioId eq id`, após confirmar que o UPDATE do funcionário afetou uma linha. Manter a leitura/retorno do funcionário existente.
- [x] Preservar a semântica para funcionário sem conta vinculada: o UPDATE de `usuario` pode afetar zero linhas sem impedir a atualização do funcionário. Não localizar conta por login nem presumir igualdade entre os IDs de funcionário e usuário.
- [ ] Adicionar testes focados de persistência do repositório para os dois sentidos do status (`true → false` e `false → true`), confirmando em consultas após a chamada que funcionário e conta ficaram com o mesmo valor.
- [ ] Cobrir atomicidade: em banco isolado, fazer o UPDATE da conta falhar intencionalmente durante o teste e confirmar que a alteração anterior de `funcionario.ativo` foi revertida pela transação. Não usar um banco compartilhado/Neon ou a configuração local de desenvolvimento como fixture de teste.
- [ ] Verificar como provisionar PostgreSQL descartável para os testes. O projeto não tem atualmente H2/Testcontainers nem harness de integração de persistência; reutilizar infraestrutura PostgreSQL de teste já disponível, se houver, e não adicionar dependência sem confirmar a necessidade. Registrar como blocker caso não exista destino isolado e seguro.
- [x] Validar estaticamente a ligação dos fluxos: DELETE chama `desativar` e este atualiza com `ativo=false`; PATCH com `ativo=true/false` alcança o mesmo método. Não alterar regras de login, autorização, JWT ou exclusão física.
- [ ] Executar os testes direcionados e validações Gradle somente após autorização explícita. Nesta etapa foi solicitado não executar build/testes/migration; nenhuma migration é necessária em qualquer etapa desta mudança.

## Arquivos relevantes

### Alterar na implementação

- `serverApp/src/main/kotlin/org/greenhouse/repository/FuncionarioRepository.kt` — atualizar a conta vinculada na mesma sessão/transação Exposed usada para atualizar o funcionário.
- `serverApp/src/test/kotlin/org/greenhouse/repository/FuncionarioRepositoryTest.kt` — criar testes de persistência do espelhamento e atomicidade, no local compatível com a estrutura de testes já existente. A estratégia de banco de teste precisa ser isolada, pois não há harness configurado atualmente.

### Consultar; não alterar sem necessidade

- `serverApp/src/main/kotlin/org/greenhouse/controller/FuncionarioController.kt` — confirma o roteamento atual de DELETE/PATCH.
- `serverApp/src/main/kotlin/org/greenhouse/usecase/FuncionarioUseCase.kt` — confirma que os dois caminhos chamam `atualizar` com o valor de status esperado.
- `serverApp/src/main/kotlin/org/greenhouse/adapter/DatabaseConfig.kt` — delimita a transação Exposed atual; conservar uma única `session` para as duas tabelas.
- `serverApp/src/main/kotlin/org/greenhouse/repository/UsuarioRepository.kt` — mapeamento separado de `usuario`; não é necessário alterar seu contrato para esta operação.
- `serverApp/src/main/resources/db/migration/V2__create_funcionario.sql`, `V3__create_usuario.sql` e `V23__add_primeiro_acesso_feito_to_usuario.sql` — consultar constraints/colunas existentes; não alterar nem executar migrations.
- `serverApp/build.gradle.kts` — dependências e configuração de testes atuais; consultar apenas se o teste isolado exigir ajuste, sem adicionar harness ou dependência por suposição.
- `serverApp/src/test/kotlin/org/greenhouse/usecase/FuncionarioProvisioningTest.kt` e `serverApp/src/test/kotlin/org/greenhouse/security/FuncionarioSecurityTest.kt` — convenções atuais dos testes; nenhum deles valida persistência real.

## Como verificar

- Em PostgreSQL isolado, preparar um funcionário e sua conta ligada por `funcionario_id`; exercitar `FuncionarioRepository.atualizar` com status verdadeiro e falso, consultando os dois registros depois de cada chamada.
- Falhar de forma controlada o UPDATE de `usuario` no teste de atomicidade e confirmar que o valor anterior de `funcionario.ativo` permanece após rollback.
- Confirmar que um funcionário sem linha correspondente em `usuario` continua atualizável sem violação de integridade ou mudança de comportamento.
- Revisão estática dos caminhos: DELETE deve continuar usando desativação lógica; PATCH só espelha o status recebido/mesclado; nenhum endpoint, schema, migration, cliente ou regra de JWT deve mudar.
- Após autorização para build/testes, executar o teste focado do repositório e a validação Gradle direcionada do `serverApp`; registrar comandos e resultados em `Worklog.md`. Não aplicar migration.
- Não executar build, testes ou migrations nesta etapa de planejamento, conforme solicitado.

## Decisões tomadas

- Atualizar diretamente a tabela `usuario` já mapeada em `FuncionarioRepository`, no mesmo `DatabaseConfig.session` e transação da atualização de funcionário. Isso evita uma segunda sessão/commit e preserva o fluxo existente.
- Filtrar a conta pela FK `usuario.funcionario_id`, não por ID presumido ou login. A constraint única limita a zero ou uma linha correspondente.
- Manter a atualização do funcionário válida quando não há conta vinculada, pois a FK é da conta para funcionário e não exige que todo funcionário tenha usuário; não criar uma nova regra de rejeição.
- Usar o caminho comum do repository para sincronizar em DELETE e PATCH; não duplicar persistência no controller/use case. Atualizações de outros campos também gravarão o mesmo status já mantido pelo funcionário.
- Não alterar `excluir`, migrations, entidades/contratos, clientes, OpenAPI, autorização, autenticação, JWT existentes ou outras regras do CRUD.
- Tratar persistência real/atomicidade como validação de integração em banco PostgreSQL descartável. Não usar o banco Neon compartilhado; resolver eventual ausência de infraestrutura isolada antes de executar testes de integração.
- Execução de build/testes permanece pendente de autorização explícita; migration não é necessária.
