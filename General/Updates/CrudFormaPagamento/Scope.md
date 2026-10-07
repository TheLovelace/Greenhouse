# Scope — CrudFormaPagamento

Implementar no `serverApp` o CRUD HTTP de formas de pagamento usando a tabela e as convenções existentes. Todas as operações exigem JWT válido, sem restrição de perfil: `ADMIN` e `FUNCIONARIO` podem operar.

Inclui entidade, repository, use case, controller, fragmento e referências OpenAPI, testes de integração do CRUD e teste de segurança HTTP. Não inclui alterações de schema/migration, clientes multiplataforma, CRUD de pagamentos ou mudanças genéricas na infraestrutura de exceções.

## Tracks

- Backend — implementação concluída; testes do `serverApp` passaram e os testes JVM do `shared` compilam. Execução de integração pendente de `DATABASE_URL`; ktlint global reporta violações preexistentes fora do escopo.
