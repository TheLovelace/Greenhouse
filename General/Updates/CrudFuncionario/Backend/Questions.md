# Questions — Backend: autorização do CRUD de Funcionário

## Open

- Nenhuma dúvida bloqueante para o escopo aprovado. A execução e validação Gradle permanecem pendentes de autorização para build/testes.

## Resolved

- **Quais operações do CRUD devem exigir ADMIN?** → Todas as cinco rotas: criar, listar, buscar por ID, atualizar e desativar.
- **Qual evidência de perfil é confiável para autorizar?** → Somente o perfil contido em JWT válido, convertido pela infraestrutura existente em authority `ROLE_ADMIN`; não confiar em `X-User-Perfil`.
- **Swagger UI e OpenAPI devem exigir autenticação?** → Não. Permanecem publicamente acessíveis.
- **A mudança inclui schema/migration, clientes ou correções não aprovadas de funcionário?** → Não; o escopo confirmado é autorização do CRUD e documentação OpenAPI, sem expandir para outras pendências.
