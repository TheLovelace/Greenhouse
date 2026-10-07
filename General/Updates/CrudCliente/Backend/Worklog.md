# Worklog — Backend: CRUD de Cliente

## Sessão de planejamento

Investigação do repositório (branch `feat/config-prod`):

- Template de CRUD documentado em `docs/architecture/crud_template.md`; `Categoria` é a implementação de referência (`entity` → `repository` Exposed → `usecase` → `controller` Spring).
- Tabela `cliente` já está definida em `V4__create_cliente.sql` na branch `feat/database-migrations` — **ainda não mergeada** em `develop` nem na branch atual. Colunas: `id BIGSERIAL`, `nome VARCHAR(150) NN`, `telefone VARCHAR(20) NN`, `whatsapp BOOL DEFAULT FALSE`, `cpf VARCHAR(11) UNIQUE` + CHECK `^[0-9]{11}$`, `email VARCHAR(150)`, `ativo BOOL DEFAULT TRUE`.
- `pedido.cliente_id` (V14) referencia `cliente(id)` — histórico garantido por FK.
- A branch `feat/database-migrations` também adiciona Flyway + `spring-boot-starter-jdbc` ao `serverApp` — ausentes na branch atual.
- Testes de integração ficam em `shared/src/jvmTest/.../integration/` com fixtures que conectam direto via `DATABASE_URL` (`.env.test.local`); rodam com `./gradlew testDesktop`.
- OpenAPI é servido por fragmentos: `resources/openapi/<entidade>.yml` + whitelist em `OpenApiController` + refs em `openapi.yaml`.
- Backend **não possui autenticação/autorização** — perfis FUNCIONARIO/ADMIN não podem ser enforced ainda.
- Limitação encontrada: `DatabaseConfig.mapException` mapeia qualquer SQLException fora de conexão/auth para `DatabaseConnectionException` (503) — uma violação UNIQUE de `cpf` viraria 503. Mitigado com pré-checagem no use case; melhoria futura possível é mapear `23xxx` → 400.

Decisão autônoma: omitir endpoint DELETE porque as user stories exigem inativação sem remover histórico, e `pedido.cliente_id` depende da integridade da linha.

Decisão autônoma: nome do Update `CrudCliente` e track única `Backend` porque o trabalho forma uma unidade coerente (uma entidade, um contrato REST, um conjunto de testes).

## Validação de ambiente

- `.env.test.local` criado com `DATABASE_URL` do banco dev (Neon). Arquivo já coberto por `.env.*.local` no `.gitignore`.
- Conexão com o banco dev **confirmada**: `./gradlew :shared:jvmTest --tests "org.greenhouse.integration.adapter.Test_Database_Config"` passou (inclui o caso `deve estabelecer conexao com o banco`).
- Não confirmado: existência da tabela `cliente` no banco dev (sem cliente SQL disponível no ambiente).
- Ambiente local não possui Docker — `./gradlew --no-configuration-cache :serverApp:runTest` deve ser executado na máquina host do desenvolvedor.
- Ambiente local não possui `java` no PATH — usado `JAVA_HOME=/opt/android-studio/jbr` (JBR do Android Studio) para rodar o Gradle.

## Sessão de execução 1

Implementados todos os arquivos do Plan.md:

- `entity/Cliente.kt`, `repository/ClienteRepository.kt`, `usecase/ClienteUseCase.kt`, `controller/ClienteController.kt`
- `resources/openapi/cliente.yml` + refs em `openapi.yaml` + `"cliente"` no whitelist do `OpenApiController`
- `integration/fixture/ClienteFixture.kt` + `integration/cliente/TestCliente{Post,Get,Patch}.kt`

Decisões técnicas durante a execução:

- Decisão autônoma: usar `lowerCase() like` no filtro de `nome` porque Exposed 0.61 não possui `ilike` (verificado no jar). Telefone usa `like` simples (não há letras).
- Decisão autônoma: normalizar `cpf`/`email` em branco (`""`, `" "`) para `null` no `criar` e no `textoOpcional` do patch, porque formulários costumam mandar string vazia para campos opcionais; manter validação estrita do DER só quando há valor real.
- Decisão autônoma: `criar` força `id=null` e `ativo=true` via `copy`, cobrindo o critério "ativo por padrão" mesmo com payload malicioso.
- `andWhere { }` usado para compor filtros opcionais do `listar` (API confirmada no jar do Exposed).
- Endpoint `GET /cliente?ativo=`: valor não-booleano (ex.: `?ativo=abc`) resulta em erro de conversão do Spring fora do handler de domínio — aresta aceita, consistente com a ausência de precedente no projeto.

Pendências:

- `./gradlew testDesktop` e `./gradlew lintCheck` aguardam autorização (build exige autorização explícita) e dependem da tabela `cliente` existir no banco dev.

## Validação (autorizada pelo usuário)

- `./gradlew testDesktop` → **BUILD SUCCESSFUL**. Todos os testes passaram, incluindo `TestClientePost` (10), `TestClienteGet` (8), `TestClientePatch` (11) — 29 testes de cliente, 0 falhas. Isso confirma que a tabela `cliente` **existe** no banco dev (dúvida bloqueante resolvida).
- `./gradlew ktlintCheck` → arquivos novos **sem violações**. Falhas apenas em `serverApp/build.gradle.kts` e `desktopApp/main.kt` — violações já commitadas na branch `feat/config-prod`, fora do escopo da track (git diff confirma: não alterados).
- `./gradlew lintCheck` completo não executa nesta máquina: `:androidApp:lint` exige Android SDK ausente.
- Verificação paralela: o hotfix da colega (construtor cíclico em `AuthenticationException`) não tem relação com a falha de lint — era erro de compilação, já ausente nesta branch; a falha aqui é de estilo preexistente.

Arquivos alterados nesta sessão (estado real):

- Criados: `entity/Cliente.kt`, `repository/ClienteRepository.kt`, `usecase/ClienteUseCase.kt`, `controller/ClienteController.kt`, `resources/openapi/cliente.yml`, `integration/fixture/ClienteFixture.kt`, `integration/cliente/TestCliente{Post,Get,Patch}.kt`
- Modificados: `resources/openapi.yaml`, `controller/OpenApiController.kt`
- Docs da track: `Plan.md`, `Worklog.md`, `Questions.md`

## Sessão de execução 2 — validação manual via bootRun

Servidor subiu localmente com `./gradlew :serverApp:bootRun` (porta 8080) após resolver dois detalhes de ambiente:

- `.env.test.local` **não pode ser carregado com `source`/`. `**: a URL contém `&` nos query params do JDBC e o shell quebra a linha em comandos. Forma correta: `export DATABASE_URL="$(cut -d= -f2- .env.test.local)"`. O `loadEnvFile` do Gradle e o `--env-file` do Docker leem a linha inteira, então `runTest` e `testDesktop` não são afetados.
- Exportar a var apenas no shell não basta se houver Gradle Daemon ativo — o daemon carrega o env de quando iniciou. Solução: `./gradlew --stop` antes do bootRun (ou `--no-daemon`).

Validações ao vivo:

- `GET /api/status` → `{"status":"ok","database":"ok"}`
- `POST /api/cliente` → 201 (criou registro de teste `Maria Silva Teste`, id 23, **ficou no banco dev** — sem endpoint DELETE por design)
- `GET /api/cliente?nome=` → filtro parcial funcionando
- `POST` sem `nome` → 400
- Rotas são prefixadas por `/api` via `WebConfig.configurePathMatch` — inclui `OpenApiController` e os controllers internos do springdoc (`/api/v3/api-docs`)

Bug preexistente corrigido (necessário para testar via Swagger):

- `springdoc.swagger-ui.url` apontava `/openapi.yaml`, mas a spec é servida em `/api/openapi.yaml` (o `OpenApiController` também recebe o prefixo). Swagger UI falhava ao carregar a definição. Corrigido para `/api/openapi.yaml` em `application.properties` — confirmado em `/api/v3/api-docs/swagger-config`.
