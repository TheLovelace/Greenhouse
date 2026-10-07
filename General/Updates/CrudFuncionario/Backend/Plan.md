# Plan — Backend: autorização do CRUD de Funcionário

## TL;DR

Restringir todas as rotas do CRUD de funcionário a requisições com JWT válido e authority `ROLE_ADMIN`, mantendo Swagger UI e documentos OpenAPI acessíveis publicamente. Atualizar a especificação principal e o fragmento OpenAPI de funcionário para documentar Bearer JWT e remover os parâmetros `X-User-Perfil`. Não alterar o comportamento de negócio do CRUD nem o banco.

## Estado atual confirmado

- `JwtAuthenticationFilter` valida o token e cria a authority `ROLE_$perfil`; portanto, o perfil `ADMIN` chega ao Spring Security como `ROLE_ADMIN`.
- `SecurityConfig` permite publicamente login, primeiro acesso, status e rotas da documentação; para as demais rotas exige apenas autenticação. Não há regra de ADMIN para o CRUD de funcionário.
- `FuncionarioController` expõe criar e listar em `/funcionario`, e buscar, atualizar e desativar em `/funcionario/{id}`. O OpenAPI principal informa o prefixo `/api`.
- `openapi/funcionario.yml` descreve `X-User-Perfil` em algumas operações, embora esse header não seja uma fonte confiável de autorização.
- O controller OpenAPI já serve `openapi.yaml` e fragmentos, e os caminhos de documentação estão liberados na configuração de segurança.
- Não foram encontrados testes Kotlin existentes em `serverApp/src/test`; `serverApp` já inclui `spring-boot-starter-test`. `JwtService` permite gerar tokens com perfil para testes.

## Steps

- [x] Em `SecurityConfig.kt`, aplicar a exigência de perfil ADMIN às cinco operações do CRUD de funcionário, usando a authority produzida pelo filtro JWT. Manter login, primeiro acesso, status, Swagger UI, OpenAPI principal e fragmentos explicitamente acessíveis sem autenticação.
- [x] Na especificação principal `openapi.yaml`, declarar um security scheme HTTP Bearer com formato JWT; não aplicar uma exigência global que marque endpoints públicos como protegidos.
- [x] Em `openapi/funcionario.yml`, remover os parâmetros `X-User-Perfil` e as descrições que tratam esse header como autorização. Declarar o requisito de Bearer JWT para cada operação do CRUD; documentar as respostas 401 e 403 aplicáveis.
- [x] Criar testes de segurança em `serverApp/src/test/kotlin/org/greenhouse/security/` para cobrir todas as rotas do CRUD: sem token retorna 401; token válido de `FUNCIONARIO` retorna 403; token válido de `ADMIN` alcança a operação autorizada. Usar token gerado pelo `JwtService` e dublês do use case para não depender do banco.
- [x] Testar que Swagger UI, OpenAPI principal e fragmento OpenAPI permanecem acessíveis sem token.
- [x] Após autorização para executar build/testes, rodar a validação Gradle direcionada ao `serverApp` e registrar o resultado. Não executar build durante esta etapa de planejamento.

## Arquivos relevantes

### Alterar na implementação

- `serverApp/src/main/kotlin/org/greenhouse/security/SecurityConfig.kt` — aplicar autorização por perfil ao CRUD, preservando as exceções públicas existentes.
- `serverApp/src/main/resources/openapi.yaml` — declarar o esquema Bearer JWT.
- `serverApp/src/main/resources/openapi/funcionario.yml` — declarar segurança por Bearer nas operações do CRUD e remover `X-User-Perfil`.

### Criar na implementação

- `serverApp/src/test/kotlin/org/greenhouse/security/FuncionarioSecurityTest.kt` — cobrir permissões e acessibilidade pública dos documentos.

### Consultar; não alterar sem necessidade

- `serverApp/src/main/kotlin/org/greenhouse/security/JwtAuthenticationFilter.kt` — já valida JWT e atribui `ROLE_$perfil`.
- `serverApp/src/main/kotlin/org/greenhouse/security/JwtService.kt` — emissão/validação de tokens, útil para preparar testes.
- `serverApp/src/main/kotlin/org/greenhouse/controller/FuncionarioController.kt` — rotas atuais do CRUD; não requer alteração prevista.
- `serverApp/src/main/kotlin/org/greenhouse/controller/OpenApiController.kt` — entrega da especificação e fragmentos; não requer alteração prevista.
- `serverApp/build.gradle.kts` — já contém `spring-boot-starter-test`; consultar para escolher o comando de validação, sem adicionar dependências sem necessidade.

## Como verificar

- Testes automatizados devem verificar, para cada rota do CRUD de funcionário, resposta 401 sem token e 403 com JWT válido de perfil `FUNCIONARIO`; com JWT válido de `ADMIN`, a requisição deve ultrapassar a autorização e chegar ao handler (resultado funcional controlado por dublê).
- Verificar que as rotas do Swagger UI, do OpenAPI principal e do fragmento de funcionário continuam acessíveis sem autenticação.
- Verificar que a especificação principal contém o esquema Bearer JWT, que todas as operações do CRUD no fragmento declaram esse esquema e que não restam parâmetros `X-User-Perfil`.
- Com autorização para build/testes: executar os testes direcionados do `serverApp` e a task de compilação/teste pertinente do Gradle. Registrar qualquer limitação ou falha preexistente; não declarar validação executada antes da autorização.
- Nenhuma migration ou validação de banco é necessária para esta mudança.

## Decisões tomadas

- Reutilizar a authority já produzida por `JwtAuthenticationFilter` (`ROLE_ADMIN`); não confiar em header de perfil enviado pelo cliente e não alterar o mecanismo de emissão/validação JWT.
- Proteger as cinco operações expostas pelo controller (`POST /funcionario`, `GET /funcionario`, `GET /funcionario/{id}`, `PATCH /funcionario/{id}`, `DELETE /funcionario/{id}`).
- Manter públicos Swagger UI, OpenAPI principal e fragmentos, assim como as liberações atuais de autenticação/status.
- Declarar o security scheme Bearer JWT na especificação principal e referenciá-lo nas operações protegidas do fragmento; não definir segurança global que contradiga endpoints públicos.
- Escopo restrito ao backend e documentação API: nenhuma mudança em `shared`, aplicativos cliente, persistência ou migrations.
- Os arquivos do CRUD de funcionário e OpenAPI já possuem alterações locais do usuário. Preservá-las; integrar apenas as mudanças necessárias, sem substituir trabalho preexistente.
