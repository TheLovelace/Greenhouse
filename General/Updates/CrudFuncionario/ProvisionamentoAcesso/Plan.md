# Plan — Provisionamento de conta de acesso do funcionário

## TL;DR

Ao criar um funcionário, criar também sua conta `usuario` na mesma transação. Derivar o login da parte anterior a `@`, iniciar a conta com senha placeholder não utilizável e `primeiro_acesso_feito = false`, e impedir autenticação enquanto essa flag for falsa. No primeiro acesso, validar a senha, armazená-la com BCrypt e marcar a flag como verdadeira atomicamente.

Adicionar migração compatível V23: marcar contas existentes como primeiro acesso concluído sem alterar senha/hash, login, perfil ou status e deixar novas contas com default `false`. Para preservar a única conta ADMIN operacional, a autenticação será gradual: verificar hashes BCrypt válidos com BCrypt; credenciais legadas em texto continuam sendo comparadas como hoje e só são convertidas para BCrypt depois de autenticação bem-sucedida. Não resetar senha nem substituir credenciais na migration.

**Estado da track: implementação e testes unitários concluídos; validação de rollback transacional em PostgreSQL pendente.** V23 foi aplicada ao banco Neon de desenvolvimento com autorização do usuário. Produção não foi modificada.

## Estado atual confirmado

- `Funcionario.email` e `funcionario.email` são opcionais; `FuncionarioUseCase.criar` valida CPF/nome e `FuncionarioRepository.criar` persiste somente o funcionário.
- `usuario` possui FK única para funcionário, `login` único, `senha_hash VARCHAR(255) NOT NULL`, perfil e ativo. A entidade/mapeamento ainda não possuem flag de primeiro acesso.
- `AuthUseCase.login` compara diretamente a senha recebida com o valor armazenado e não verifica primeiro acesso. `primeiroAcesso` atualiza apenas senha. O contrato atual usa `senhaHash` no login, embora a comparação existente seja texto direto.
- `DatabaseConfig.session` abre uma transação Exposed; a criação de funcionário e usuário deve compartilhar uma única transação, não apenas chamar repositórios que confirmem sessões distintas.
- As migrations atuais vão até V22. O seed insere usuários e a auditoria SQL espera V1..V22.
- `spring-boot-starter-security` já está declarado no `serverApp`; verificar a disponibilidade de `BCryptPasswordEncoder` nessa dependência antes de propor outra. O campo existente de 255 caracteres comporta um hash BCrypt padrão, não sendo necessária alteração de tamanho para BCrypt.
- Há testes de segurança de funcionário; não foi encontrada suíte existente de autenticação/persistência nem dependência H2/Testcontainers configurada.
- Nenhum arquivo de código foi editado; nenhuma migration, build ou teste foi executado durante esta atualização de planejamento.

## Steps

- [x] Resolver em `Questions.md` a política de e-mail obrigatório/válido e colisões, antes da implementação. Recomendação: exigir e-mail válido para provisionamento; normalizar o trecho local para minúsculas; rejeitar a operação inteira em colisão, sem criar funcionário nem alterar conta existente.
- [x] Acrescentar `primeiro_acesso_feito` ao modelo e mapeamento `Usuario`, com valor explícito ao provisionar conta.
- [x] Criar `V23__add_primeiro_acesso_feito_to_usuario.sql`: coluna booleana `NOT NULL DEFAULT FALSE`, seguida de backfill `TRUE` para todas as linhas já existentes. Não atualizar `senha_hash` nem qualquer outra credencial. O default aplica-se às inserções futuras que não informarem flag.
- [x] Fazer provisionamento transacional de funcionário + usuário: validar e-mail segundo a decisão aprovada; criar funcionário; derivar login do local-part do email, normalizado como decidido; persistir conta `FUNCIONARIO`, senha placeholder que não possa autenticar e flag `false`. Qualquer erro no segundo insert/constraint deve reverter ambos.
- [x] Integrar BCrypt sem interromper credenciais legadas: em login de conta ativa e já liberada, se `senha_hash` for um hash BCrypt válido, verificar pela API BCrypt; caso contrário, comparar exatamente como hoje. Se a comparação legada tiver sucesso e a senha couber em 72 bytes UTF-8, gravar o hash BCrypt calculado sem mudar login/perfil/ativo/flag. Credenciais legadas acima desse limite permanecem funcionais, sem truncamento e sem upgrade. Não regravar credencial após falha de autenticação. O login continua recusando flag `false` antes de emitir JWT, qualquer que seja a senha, inclusive placeholder.
- [x] No primeiro acesso, aceitar somente conta ativa e pendente; impor senha com no mínimo os 6 caracteres atuais e no máximo **72 bytes em UTF-8** (limite BCrypt, sem truncamento), armazenar hash BCrypt e mudar `primeiro_acesso_feito` para `true` numa atualização atômica. Rejeitar tentativa repetida sem sobrescrever senha.
- [x] Manter BCrypt na coluna existente `senha_hash VARCHAR(255)`; não introduzir campo/tabela de hash ou alterar tamanho sem necessidade demonstrada.
- [x] Atualizar `seed_dev.sql` para definir explicitamente `primeiro_acesso_feito = TRUE` nos usuários previamente provisionados pelo seed; preservar os valores de credencial existentes, sem reproduzi-los na documentação ou logs. A compatibilidade legada permite conversão BCrypt somente após login bem-sucedido.
- [x] Atualizar `validate_database.sql` para conferir V1..V23, coluna, `NOT NULL` e default `FALSE`, mantendo as demais verificações.
- [x] Atualizar OpenAPI de funcionário e auth: criação provisiona usuário; login rejeita primeiro acesso pendente; primeiro acesso grava senha BCrypt e conclui a flag; documentar rejeição acima de 72 bytes UTF-8, sem sugerir que `maxLength: 72` represente exatamente bytes UTF-8. Preservar nomes e formatos atuais dos payloads se a implementação não exigir mudança de contrato.
- [x] Criar testes unitários para provisionamento, validação de e-mail, login BCrypt/legado, conversão após login válido, proteção contra atualização em falha, credencial legada longa, bloqueio de conta pendente, primeiro acesso BCrypt/limite UTF-8 e preservação do perfil ADMIN.
- [x] Aplicar V23 somente ao banco Neon de desenvolvimento após autorização explícita. Flyway confirmou sucesso e schema na versão v23; produção não foi modificada.
- [x] Executar `.\gradlew.bat :serverApp:test` após autorização explícita: 18 testes, 0 falhas, 0 erros.
- [x] Validar o provisionamento e o primeiro acesso por Swagger e confirmar os registros/estado no banco de desenvolvimento manualmente pelo usuário; usuário reportou tudo funcionando. Rollback por falha forçada não foi testado.
- [x] `git diff --check` passou. PyYAML não está instalado, portanto a sintaxe YAML foi inspecionada, mas não validada por parser automatizado.

## Arquivos relevantes

### Alterar na implementação

- `serverApp/src/main/kotlin/org/greenhouse/usecase/FuncionarioUseCase.kt` — validação e orquestração do provisionamento.
- `serverApp/src/main/kotlin/org/greenhouse/repository/FuncionarioRepository.kt` — persistência coordenada com a conta na transação única.
- `serverApp/src/main/kotlin/org/greenhouse/repository/UsuarioRepository.kt` — coluna/flag, criação, busca e atualização atômica de senha + flag.
- `serverApp/src/main/kotlin/org/greenhouse/entity/Usuario.kt` — estado de primeiro acesso.
- `serverApp/src/main/kotlin/org/greenhouse/usecase/AuthUseCase.kt` — compatibilidade BCrypt/legado, bloqueio de pendentes e primeiro acesso.
- `serverApp/src/main/resources/infra/database/seed_dev.sql` — flag explícita nos usuários de seed sem alterar nem expor credenciais.
- `serverApp/src/main/resources/infra/database/validate_database.sql` — auditoria de V23 e coluna.
- `serverApp/src/main/resources/openapi/funcionario.yml` e `serverApp/src/main/resources/openapi/auth.yml` — semântica de provisionamento, pendência e limite de senha.
- `serverApp/src/test/kotlin/org/greenhouse/` — testes de use case, autenticação e persistência, conforme estrutura existente.

### Criar na implementação

- `serverApp/src/main/resources/db/migration/V23__add_primeiro_acesso_feito_to_usuario.sql` — flag com default `FALSE` e backfill `TRUE`, sem alteração de credenciais legadas.
- Arquivos de teste apenas se a estrutura atual não oferecer local apropriado; evitar introduzir banco em memória/container sem necessidade e decisão explícita.

### Consultar; não alterar sem necessidade

- `serverApp/src/main/resources/db/migration/V2__create_funcionario.sql` e `V3__create_usuario.sql` — schema, FK e constraints existentes; não editar migrations já versionadas.
- `serverApp/src/main/kotlin/org/greenhouse/controller/FuncionarioController.kt` e `AuthController.kt` — preservar rotas e payloads, salvo incompatibilidade concreta.
- `serverApp/src/main/kotlin/org/greenhouse/adapter/DatabaseConfig.kt` — transação Exposed compartilhada.
- `serverApp/src/main/kotlin/org/greenhouse/security/SecurityConfig.kt` — preservar acesso público já autorizado aos endpoints de login/primeiro acesso.
- `serverApp/build.gradle.kts` — starter de segurança já incluído; confirmar disponibilidade BCrypt antes de adicionar dependência.
- `serverApp/src/test/kotlin/org/greenhouse/security/FuncionarioSecurityTest.kt` — preservar testes da track Backend concluída.

## Impactos e riscos

- **Backend (`serverApp`)**: criação, persistência e autenticação. O risco principal é não compartilhar uma transação e deixar funcionário sem conta; login legado deve continuar operacional durante a transição.
- **PostgreSQL/Flyway**: V23 adiciona a flag. Backfill `TRUE` mantém as contas atuais aptas a login; default futuro `FALSE` impede acesso de contas provisionadas sem ativação. A migration não toca credenciais existentes, especialmente a conta ADMIN atual.
- **BCrypt**: a migração de credenciais é preguiçosa e individual: senha legada correta que caiba em 72 bytes UTF-8 é convertida após login bem-sucedido; senha incorreta não modifica nada; nova senha e primeiro acesso já são persistidos como BCrypt. Não truncar credenciais legadas maiores que o limite; recomenda-se mantê-las no formato legado para preservar acesso até decisão/fluxo de troca de senha. Não armazenar senha placeholder utilizável. Novas senhas acima de 72 bytes UTF-8 são rejeitadas explicitamente, sem truncamento.
- **API/OpenAPI**: os payloads podem permanecer iguais, incluindo o nome legado `senhaHash`; documentar a semântica e o limite em bytes. A recusa de login para pendentes é mudança intencional de comportamento.
- **Clientes (`androidApp`, `iosApp`, `desktopApp`, `webApp`, `shared`)**: nenhuma alteração de contrato está prevista. Confirmar consumidores existentes se a implementação mudar request/response.
- **E-mail/login**: email é nullable e não há política existente de normalização/colisão. Sem a decisão, o executor não deve inventar variante de login nem aceitar provisionamento ambíguo.

## Como verificar

- Testes do use case confirmam e-mail/login conforme a decisão aprovada e rollback completo se conta não puder ser criada.
- Testes de autenticação cobrem hash BCrypt válido, login legado correto com upgrade quando couber no limite, falha legada sem upgrade, conta ADMIN legada sem reset, credencial legada acima de 72 bytes sem truncamento (conforme resposta) e bloqueio de primeiro acesso pendente antes de emissão de token.
- Testes do primeiro acesso verificam mínimo atual, limite de 72 bytes UTF-8 incluindo senha multibyte, rejeição sem truncamento e atualização hash/flag atômica.
- Validar V23 em PostgreSQL descartável contendo ao menos uma conta legada: flag passa a `TRUE`, senha/login/perfil/ativo permanecem iguais; insert futuro sem flag resulta em `FALSE`. Confirmar constraints de FK e unicidade mantidas.
- Confirmar seed com flag explícita e auditoria V1..V23 sem apresentar valores de credenciais.
- Conferir OpenAPI: formato de senha não foi alterado, estado pendente descrito e limite expresso como bytes UTF-8.
- Após implementação/autorização: `.\gradlew.bat :serverApp:test` (ou testes direcionados) e `git diff --check`. Esta atualização de planejamento não executou build, testes ou migration.

## Decisões tomadas

- Consentimento do usuário: criar conta junto com funcionário, derivar login da parte do e-mail antes de `@` e iniciar com senha placeholder inutilizável.
- Semântica confirmada: `primeiro_acesso_feito = false` enquanto pendente; primeiro acesso concluído muda para `true`.
- BCrypt foi autorizado nesta track. Hashes BCrypt válidos são verificados com BCrypt; credenciais legadas que não são hashes BCrypt válidos são comparadas como atualmente e convertidas para BCrypt somente depois de login bem-sucedido, se forem compatíveis com o limite de 72 bytes UTF-8.
- A V23 define `FALSE` como default para novas contas e faz backfill `TRUE` em todas as contas existentes. Não resetar senha ou modificar hash de conta preexistente na migration; especialmente preservar a única conta ADMIN necessária para cadastrar funcionários.
- Conta pendente não autentica nem recebe JWT, independentemente da senha placeholder. Primeiro acesso grava o novo BCrypt hash e `true` de forma atômica.
- Limite recomendado/definido para novas senhas BCrypt: rejeitar senha acima de 72 bytes UTF-8, sem truncamento; manter mínimo atual de 6 caracteres. Documentar limite em bytes na OpenAPI. Para credenciais legadas maiores que 72 bytes, não truncar nem quebrar autenticação; manter compatibilidade sem upgrade é a recomendação a confirmar.
- O `VARCHAR(255)` existente é suficiente para hash BCrypt; não planejar mudança de schema além da flag V23.
- Recomendação para fechar perguntas: exigir e-mail válido; normalizar local-part para minúsculas; rejeitar colisão e reverter a criação inteira. A decisão final de negócio permanece registrada em `Questions.md`.
- Sem sincronização automática do login após alterações posteriores no e-mail nesta track. Não alterar migrations anteriores nem a track Backend concluída.
