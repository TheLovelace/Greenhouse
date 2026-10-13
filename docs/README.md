# Greenhouse

## Identificação

- **Projeto:** Greenhouse
- **Grupo/repositório:** Best-of-the-class
- **Repositório Git:** https://github.com/Best-of-the-class/Greenhouse

## Integrantes e responsabilidades

As migrations do banco registram os seguintes responsáveis pelos CRUDs. O repositório contém somente os primeiros nomes:

| Integrante | Entidades/CRUDs sob responsabilidade |
|---|---|
| Luna | Funcionário e forma de pagamento |
| Isabella | Categoria e produto |
| Amanda | Espécie e configuração de alertas |
| Anahi | Fornecedor e compra de fornecedor |
| Julia | Cliente e pedido |
| Debora | Desconto e estoque |

As tabelas auxiliares são utilizadas pelos respectivos fluxos, como itens de pedido, itens de compra e relações entre produtos, categorias e espécies.

## Sobre a aplicação

Greenhouse é uma plataforma para apoiar a gestão de uma floricultura. O backend oferece uma API para operações de cadastro e gerenciamento de dados como funcionários, clientes, fornecedores, categorias, produtos, formas de pagamento, descontos e estoque. O modelo do banco também contempla pedidos e compras, espécies de plantas, dispositivos IoT, leituras ambientais e configuração de alertas.

A centralização desses dados e regras ajuda a reduzir controles manuais e inconsistências entre catálogo, estoque e operações da loja. As validações de domínio e persistência ficam no backend, enquanto o banco aplica restrições de integridade. O estoque, por exemplo, controla quantidades físicas e reservadas; os descontos aceitam percentuais ou valores fixos sujeitos às regras cadastradas.

O backend é a camada de API e persistência. As aplicações Android, iOS, Desktop e Web compartilham a interface Compose Multiplatform. **No estado atual do repositório, as entradas das aplicações cliente exibem a tela de demonstração padrão do Compose; elas ainda não apresentam uma interface completa integrada aos CRUDs da floricultura.** O Swagger/OpenAPI pode ser utilizado para explorar a API.

## Tecnologias

- Kotlin e Gradle
- Kotlin Multiplatform e Compose Multiplatform
- Spring Boot e Spring Data JPA
- PostgreSQL
- Flyway para versionamento de migrations
- Spring Security e JWT para autenticação/autorização
- OpenAPI/Swagger para documentação da API
- Docker e Jib para empacotamento do backend
- Android, iOS, Desktop (JVM), JavaScript e Kotlin/Wasm como targets cliente

## Arquitetura e pastas

### Backend (`serverApp`)

O backend é uma aplicação Kotlin/JVM independente. Seu código principal está em `serverApp/src/main/kotlin/org/greenhouse/`:

| Pasta | Responsabilidade |
|---|---|
| `controller/` | Endpoints REST e tradução de requisições/respostas HTTP |
| `service/` | Regras de aplicação e validações de domínio |
| `repository/` | Persistência e consultas Spring Data JPA |
| `model/` | Entidades JPA mapeadas para o banco |
| `dto/` | Contratos de entrada e saída da API |
| `mapper/` | Conversão entre entidades e DTOs |
| `security/` | Autenticação JWT e configuração de autorização |
| `adapter/` | Tratamento de exceções e configurações da aplicação |
| `enums/` | Enumerações de domínio |

As migrations Flyway ficam em `serverApp/src/main/resources/db/migration/`. A configuração da aplicação está em `serverApp/src/main/resources/application.properties`; os contratos OpenAPI ficam em `serverApp/src/main/resources/openapi/` e no arquivo `serverApp/src/main/resources/openapi.yaml`.

Fluxo geral do backend:

```text
Requisição HTTP -> controller -> service -> repository -> PostgreSQL
                                  |
                         model / DTO / mapper
```

O Flyway gerencia a evolução do schema e o Hibernate está configurado para validar o mapeamento (`ddl-auto=validate`), sem alterar o schema automaticamente.

### Frontend e código multiplataforma

O código compartilhado está em `shared/src/`:

- `commonMain/kotlin/org/greenhouse/`: interface Compose compartilhada e código comum.
- `androidMain/`, `iosMain/`, `jvmMain/`, `jsMain/` e `wasmJsMain/`: implementações específicas de plataforma.
- `commonTest/`, `androidHostTest/`, `iosTest/`, `jvmTest/` e `webTest/`: testes por target.

Os módulos de entrada das aplicações ficam em:

- `androidApp/`: aplicação Android.
- `iosApp/`: projeto nativo iOS aberto pelo Xcode.
- `desktopApp/`: aplicação Desktop JVM.
- `webApp/`: aplicação Web.

As telas e componentes da interface ainda não estão organizados em camadas próprias de `screens/` e `components/` no código atual; a interface compartilhada permanece na tela de demonstração em `shared/src/commonMain/`.

## Requisitos

- Git.
- JDK compatível com o Gradle do projeto (o workflow de CI utiliza JDK 17).
- Acesso a um banco PostgreSQL de desenvolvimento/teste para executar o backend e os testes de integração.
- Docker Desktop, somente para executar o backend pelo container local `runTest`.
- Android SDK e dispositivo/emulador para executar no Android.
- macOS e Xcode para compilar/executar o aplicativo iOS.

## Instalação

Clone o repositório e entre na pasta:

```powershell
git clone https://github.com/Best-of-the-class/Greenhouse.git
Set-Location Greenhouse
```

No macOS/Linux, os comandos do Gradle Wrapper usam `./gradlew`; no Windows, use `.\gradlew.bat`.

## Configuração do banco

O backend exige a variável de ambiente `DATABASE_URL`, no formato de URL JDBC PostgreSQL. Configure-a com os dados de um banco de **desenvolvimento/teste** fornecidos pela equipe. Por exemplo, no PowerShell:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://<host>:<porta>/<banco>?user=<usuario>&password=<senha>"
```

Substitua os marcadores localmente; não coloque credenciais reais neste arquivo, no código-fonte ou em commits. O Flyway aplica as migrations disponíveis na inicialização e o Hibernate valida o schema. Não execute contra produção para testes locais.

Para executar os testes JVM de integração, crie na raiz o arquivo local `.env.test.local` com a variável `DATABASE_URL` configurada para o banco de teste. Esse arquivo é local e não deve ser versionado nem compartilhado.

## Execução

### Backend local

Com `DATABASE_URL` configurada:

```powershell
.\gradlew.bat :serverApp:bootRun
```

O backend inicia na porta `8080`.

### Backend em container de teste

Inicie o Docker Desktop e configure `.env.test.local` na raiz. Depois:

```powershell
.\gradlew.bat --no-configuration-cache :serverApp:runTest
```

A task constrói a imagem local e inicia o container mapeando a porta `8081` da máquina para a porta `8080` do container. Para pará-lo:

```powershell
.\gradlew.bat :serverApp:stopTest
```

### Aplicações cliente

```powershell
# Web em modo de desenvolvimento
.\gradlew.bat devWeb

# Desktop
.\gradlew.bat devDesktop

# Instalação/execução Android em dispositivo ou emulador conectado
.\gradlew.bat devMobile
```

Para iOS, abra `iosApp/iosApp.xcodeproj` no Xcode em um Mac e execute pelo Xcode.

## Acesso à API e ao Swagger

Com o backend local:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI YAML:** http://localhost:8080/api/openapi.yaml
- **Status da API:** http://localhost:8080/api/status

Com o container de teste, use `http://localhost:8081` no lugar de `http://localhost:8080`.

As rotas de negócio exigem autenticação JWT. As rotas de login, status e documentação OpenAPI são públicas. Para testar os endpoints protegidos, autentique-se pelo endpoint de login e informe o token como `Bearer` no Swagger.

## Testes e qualidade

```powershell
# Testes JVM, incluindo os testes de integração do backend
.\gradlew.bat testDesktop

# Testes Android host
.\gradlew.bat :shared:testAndroidHostTest

# Testes Web (JavaScript e Wasm)
.\gradlew.bat testWeb

# Verificação de formatação e lint
.\gradlew.bat lintCheck

# Validar a mensagem do último commit
.\gradlew.bat checkCommitMessage
```

Os testes de integração JVM dependem de `DATABASE_URL` para acessar o PostgreSQL de teste.
