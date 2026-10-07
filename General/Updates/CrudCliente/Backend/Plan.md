# Plan — Backend: CRUD de Cliente

## TL;DR

Implementar o CRUD backend de `cliente` no `serverApp` seguindo o template de `Categoria`: `entity` → `repository` (Exposed) → `usecase` (validações) → `controller` (`/cliente`), fragmento OpenAPI e testes de integração em `shared/src/jvmTest`. Sem endpoint DELETE — inativação via campo `ativo` preserva o histórico.

## Schema da tabela (fato — migration V4, branch `feat/database-migrations`)

```sql
CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    whatsapp BOOLEAN NOT NULL DEFAULT FALSE,
    cpf VARCHAR(11) UNIQUE,
    email VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_cliente_cpf_formato
        CHECK (cpf IS NULL OR cpf ~ '^[0-9]{11}$')
);
```

`pedido.cliente_id` (V14) referencia `cliente(id)` sem cascade → inativação preserva histórico naturalmente. Nenhuma migration nova é necessária.

## Steps

- [x] **Pré-requisito:** confirmar que a tabela `cliente` existe no banco dev (migrations da branch `feat/database-migrations` aplicadas). Sem isso os testes de integração falham. — **confirmado indiretamente: os 29 testes de integração de cliente passaram contra o banco dev**
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/entity/Cliente.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/repository/ClienteRepository.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/usecase/ClienteUseCase.kt`
- [x] Criar `serverApp/src/main/kotlin/org/greenhouse/controller/ClienteController.kt`
- [x] Criar `serverApp/src/main/resources/openapi/cliente.yml` e registrar refs em `openapi.yaml`
- [x] Adicionar `"cliente"` ao whitelist de `OpenApiController.kt` (linha 25)
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/fixture/ClienteFixture.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/cliente/TestClientePost.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/cliente/TestClienteGet.kt`
- [x] Criar `shared/src/jvmTest/kotlin/org/greenhouse/integration/cliente/TestClientePatch.kt`
- [x] Rodar `./gradlew testDesktop` — **PASSOU** (29 testes de cliente + demais suites, 0 falhas)
- [~] Rodar `./gradlew lintCheck` — parcial: `ktlintCheck` limpo nos arquivos novos, mas falha por violações **preexistentes** em `serverApp/build.gradle.kts` e `desktopApp/main.kt` (não introduzidas pela track); `:androidApp:lint` indisponível sem Android SDK

## Detalhes de implementação

### Entity

```kotlin
data class Cliente(
    val id: Long? = null,
    val nome: String,
    val telefone: String,
    val whatsapp: Boolean = false,
    val cpf: String? = null,
    val email: String? = null,
    val ativo: Boolean = true,
)
```

### Repository (`private object Clientes : Table("cliente")`)

Espelhar as colunas da V4 (`long("id").autoIncrement()`, `varchar("nome", 150)`, `varchar("telefone", 20)`, `bool("whatsapp").default(false)`, `varchar("cpf", 11).nullable()`, `varchar("email", 150).nullable()`, `bool("ativo").default(true)`). Métodos:

- `criar(cliente)` — insert, devolve entity com id gerado
- `listar(nome: String?, telefone: String?, ativo: Boolean?)` — filtros opcionais combinados com `and`; `nome`/`telefone` usam match parcial case-insensitive (`ilike`, Exposed 0.61; fallback `lowerCase() like`)
- `buscarPorId(id)`
- `buscarPorCpf(cpf)` — pré-checagem de unicidade
- `atualizar(id, cliente)`
- `excluir(id)` — **somente para cleanup de testes** (fixture), não exposto no controller

### Use case — validações (mensagens pt-BR claras)

- `nome`: obrigatório, não-blank, ≤150 → `UnprocessableEntityException`
- `telefone`: obrigatório, não-blank, ≤20 → `UnprocessableEntityException` (formato livre — DER não restringe além do tamanho)
- `cpf`: se presente, `^[0-9]{11}$` → `UnprocessableEntityException`; unicidade via `buscarPorCpf` → 400 `"CPF já cadastrado"` (na atualização, ignorar o próprio registro)
- `email`: se presente, ≤150 e `^[^@\s]+@[^@\s]+\.[^@\s]+$` → `UnprocessableEntityException`
- `criar`: ignora `id` e `ativo` informados → sempre cria `ativo = true` (critério: ativo por padrão)
- `atualizar(id, campos: Map<String, Any?>)`: `camposPermitidos = {id, nome, telefone, whatsapp, cpf, email, ativo}`; campo desconhecido → `BadRequestException`; sem campo editável → `BadRequestException`; `null` explícito em `cpf`/`email` limpa o campo (helper `textoOpcional` novo — o `texto()` de Categoria não aceita null); `null` em `nome`/`telefone` → `BadRequestException`
- `id <= 0` → `BadRequestException`; inexistente → `NotFoundException`

### Controller — `@RequestMapping("/cliente")`

| Rota | Sucesso | Erros |
|---|---|---|
| `POST /cliente` | 201 + entity | 400 |
| `GET /cliente?nome=&telefone=&ativo=` | 200 + lista (vazia = `[]`) | — |
| `GET /cliente/{id}` | 200 | 400, 404 |
| `PATCH /cliente/{id}` | 200 + entity | 400, 404 |

`@RequestParam(required = false)` para `nome`, `telefone`, `ativo`. **Sem DELETE.**

### OpenAPI

- `openapi/cliente.yml`: paths `/cliente` e `/cliente/{id}`, schemas `Cliente` e `ClientePatch` (modelo: `openapi/categoria.yml`); documentar os query params `nome`, `telefone`, `ativo`
- `openapi.yaml`: tag `Cliente`, refs de paths e schema
- `OpenApiController.kt`: adicionar `"cliente"` ao `when` do whitelist

### Testes de integração (modelo: `integration/categoria/`)

`ClienteFixture` instancia `ClienteRepository(DatabaseConfig(System.getenv("DATABASE_URL")))` e gera CPF único por teste (ex.: derivado de UUID → 11 dígitos). Casos mínimos:

- **Post:** ativo por padrão; id informado ignorado; nome ausente/>150; telefone ausente/>20; cpf formato inválido; cpf duplicado; email inválido
- **Get:** lista todos (ativos + inativos); filtro `ativo`; filtro `nome` parcial; filtro `telefone`; combinação nome+telefone; busca por id; id inexistente → 404; lista vazia retorna `[]`
- **Patch:** atualização parcial por campo; inativar (`ativo=false`); reativar (`ativo=true`); limpar cpf/email com null; campo desconhecido → 400; body vazio → 400; id inválido → 400; id inexistente → 404; validações continuam valendo no patch

## Arquivos relevantes

**Criar:**
- `serverApp/src/main/kotlin/org/greenhouse/entity/Cliente.kt`
- `serverApp/src/main/kotlin/org/greenhouse/repository/ClienteRepository.kt`
- `serverApp/src/main/kotlin/org/greenhouse/usecase/ClienteUseCase.kt`
- `serverApp/src/main/kotlin/org/greenhouse/controller/ClienteController.kt`
- `serverApp/src/main/resources/openapi/cliente.yml`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/cliente/TestClientePost.kt`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/cliente/TestClienteGet.kt`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/cliente/TestClientePatch.kt`
- `shared/src/jvmTest/kotlin/org/greenhouse/integration/fixture/ClienteFixture.kt`

**Alterar:**
- `serverApp/src/main/resources/openapi.yaml` (tag, paths, schema ref)
- `serverApp/src/main/kotlin/org/greenhouse/controller/OpenApiController.kt` (whitelist)

**Não alterar:** `DatabaseConfig`, `ExceptionConfig`, `GlobalExceptionHandler`, migrations (V4 já define a tabela), nada em `shared/src/commonMain`, `androidApp`, `iosApp`, `desktopApp`, `webApp`.

## Como verificar

- `./gradlew testDesktop` — testes de integração de cliente passando (requer `.env.test.local` com `DATABASE_URL` e tabela `cliente` no banco dev)
- `./gradlew lintCheck` — ktlint sem violações
- Manual (opcional): `./gradlew --no-configuration-cache :serverApp:runTest` e exercitar `POST/GET/PATCH /cliente` na porta 8081; conferir `/openapi/cliente.yml` servido

## Decisões tomadas

- **Sem DELETE**: as user stories exigem preservar histórico (`pedido.cliente_id` referencia `cliente`); status é gerenciado via `PATCH {"ativo": false|true}`. `repository.excluir` existe apenas para cleanup de fixture de teste.
- **Inativar/reativar via PATCH `ativo`**: reutiliza o contrato de atualização parcial do template Categoria em vez de criar endpoints dedicados; confirmação da ação é responsabilidade do frontend.
- **`criar` força `ativo = true`**: critério "cliente criado como ativo por padrão" — mesmo que o payload mande `ativo=false`.
- **Filtros `nome`/`telefone`/`ativo` como query params** do `GET /cliente`: cobre "visualizar ativos e inativos" e "localizar por nome e/ou telefone" sem criar rotas extras; match parcial case-insensitive.
- **Pré-checagem de CPF duplicado no use case**: `DatabaseConfig.mapException` transformaria a violação UNIQUE (SQLState 23505) em 503; a checagem prévia devolve 400 com mensagem clara. Race residual aceita (registrado em Questions.md).
- **PATCH aceita `null` em `cpf`/`email`** para permitir limpar campos opcionais; exige helper `textoOpcional` no use case.
- **Sem enforcement de perfil**: backend ainda não possui autenticação; endpoints abertos como `/categoria`. Critérios de FUNCIONARIO/ADMIN ficam pendentes de auth futura.
