# Questions — Backend: CRUD de Cliente

## Open

- O telefone deve ter validação de formato mais estrita (ex.: somente dígitos) ou basta `NOT NULL` + ≤20 chars como no DER? Assumido: não-blank + ≤20.
- Quando houver autenticação no backend, quem aplica a restrição de perfil FUNCIONARIO/ADMIN — middleware global ou cada controller? Fora de escopo aqui; registrar no Update quando auth for implementada.
- Vale mapear SQLState `23xxx` (constraint violations) para 400 em `DatabaseConfig.mapException`? Beneficiaria todos os repositories, mas altera infra compartilhada — deixar para decisão do time.

## Resolved

- **[era bloqueante]** A tabela `cliente` existe no banco dev? → **Sim.** Os 29 testes de integração de cliente passaram contra o Neon dev, provando que a tabela (V4 da `feat/database-migrations`) já está aplicada lá. A branch de migrations continua não mergeada — o schema no código-fonte ainda depende dela chegar a `develop`/`main`.
- DELETE físico de cliente? → Não. Critérios exigem preservar histórico; inativação via `PATCH {"ativo": false}`.
- Como o frontend restringe inativos em novos pedidos? → `GET /cliente?ativo=true`; enforcement definitivo fica no futuro CRUD de pedido.
- `lintCheck` falhando tem relação com o hotfix da colega em `AuthenticationException`? → Não. O hotfix corrigia erro de **compilação** (já ausente nesta branch); as falhas são violações de **estilo** preexistentes em `serverApp/build.gradle.kts` e `desktopApp/main.kt`, fora do escopo da track.
