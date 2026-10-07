# Questions — Sincronização do status da conta do funcionário

## Open

- **Qual infraestrutura isolada de PostgreSQL está disponível para teste de persistência?** O repositório declara o driver PostgreSQL, mas não configura H2, Testcontainers ou um harness de integração. Não usar `.env.test.local`, Neon ou banco compartilhado para testes que criam/alteram tabelas ou dados. Confirmar um PostgreSQL descartável antes de implementar/executar a validação de persistência e rollback; se não houver, registrar o blocker antes de adicionar dependências ou infraestrutura.
- **Autorização para executar build/testes Gradle:** pendente. O usuário pediu explicitamente para não executar build/testes nesta etapa; não executar até autorização separada.

## Resolved

- **Quais caminhos devem espelhar status?** → DELETE (desativação lógica) e PATCH que atualiza `ativo`; ambos convergem atualmente em `FuncionarioRepository.atualizar`.
- **Onde fazer a sincronização?** → No `FuncionarioRepository.atualizar`, dentro da mesma `DatabaseConfig.session`/transação do update de funcionário, usando `usuario.funcionario_id`.
- **Há alteração de schema?** → Não. As colunas e a FK já existem; não criar nem executar migration.
- **O que fazer com JWT já emitido?** → Fora do escopo: não bloquear nem revogar tokens emitidos e não mudar outra regra de autenticação.
- **O DELETE atual remove fisicamente o funcionário?** → Não pelo caminho exposto: o controller usa `desativar`, que marca `ativo=false`; o método físico `excluir` não deve ser envolvido nesta mudança.
