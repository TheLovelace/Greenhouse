# Worklog — Provisionamento de conta de acesso do funcionário

## Sessão de planejamento

- Lido `AGENTS.md`; não foram encontradas instruções adicionais `AGENTS.md` em `serverApp`, `shared` ou `General`.
- Preservada a track `Backend` existente e concluída; criada esta track separada para o provisionamento e o primeiro acesso.
- Consentimento registrado: ao criar funcionário, criar também usuário, derivar login da parte do e-mail antes de `@` e usar senha placeholder não utilizável enquanto o primeiro acesso não for concluído.
- Sentido da flag confirmado pelo usuário: `primeiro_acesso_feito = true` é primeiro acesso concluído; `false` é primeiro acesso ainda não feito.
- Confirmado no código que `Funcionario.email` é opcional e `usuario.login` é único; por isso e-mail ausente/inválido e colisão precisam de política definida, não presumida.
- Confirmado que as duas tabelas já se relacionam por FK única e que repositórios abrem `DatabaseConfig.session`; a implementação precisa compartilhar uma transação para evitar funcionário sem conta.
- Confirmado que o login atual compara o valor recebido diretamente com `senha_hash` e o primeiro acesso atualiza só a senha. Não há algoritmo/encoder de senha estabelecido no backend; registrado como pergunta bloqueante.
- Confirmadas migrations até V22, seed de usuários sem a nova flag e auditoria que espera V1..V22. Plano propõe V23 com backfill `TRUE` para contas antigas, default `FALSE` para novas inserções e ajuste explícito do seed.
- A API pode preservar os formatos atuais; documentar a mudança semântica no OpenAPI e ajustar schemas/status somente se uma decisão de implementação exigir alteração de contrato.
- Nenhum arquivo de código foi editado; nenhuma migration, build ou teste foi executado.

## Decisões autônomas

- Decisão autônoma: planejar backfill `TRUE` para usuários já existentes porque marcá-los como pendentes poderia bloquear contas que já autenticam hoje; usar default `FALSE` para novas linhas para falhar fechado.
- Decisão autônoma: manter o login derivado estável após a criação, sem renomeá-lo automaticamente quando o e-mail do funcionário for editado, porque a solicitação confirma provisionamento na criação e não especifica sincronização posterior.

## Atualização do planejamento — BCrypt e compatibilidade da conta ADMIN

- Usuário confirmou incluir BCrypt no escopo e condicionou a mudança a não interromper a única conta ADMIN operacional, usada para cadastrar funcionários.
- Decisão registrada no `Plan.md`: fluxo compatível gradual. Hash BCrypt válido é verificado com BCrypt; texto legado é comparado como hoje e só vira BCrypt depois de login bem-sucedido. Migration não altera credenciais e backfill deixa contas atuais, inclusive ADMIN, com primeiro acesso concluído (`true`).
- `V23` mantém default `false` para futuras contas. O primeiro acesso grava BCrypt e define a flag `true` atomicamente; login de qualquer conta pendente é recusado antes de emitir JWT.
- `senha_hash VARCHAR(255)` já comporta BCrypt. `spring-boot-starter-security` já está declarado no backend; registrar no plano a checagem da disponibilidade de BCrypt antes de adicionar qualquer dependência.
- Adicionado limite recomendado/definido de 72 bytes UTF-8 para senha, sem truncamento, mantendo mínimo existente de 6 caracteres; validar e descrever explicitamente em OpenAPI.
- Perguntas abertas: email obrigatório/válido, normalização, colisão e tratamento de eventual credencial legada acima de 72 bytes UTF-8. Recomendação para senhas novas: validar/rejeitar acima de 72 bytes e documentar em OpenAPI; para credencial legada longa, preservar login sem truncar e adiar upgrade para não comprometer a conta ADMIN.
- Atualizados `Scope.md`, `Plan.md`, `Questions.md` e este diário. Track continua em 0%; não houve alteração em código, build, teste ou migration.
- Investigação confirmou: coluna atual de email é nullable e `login` é unique; a autenticação compara diretamente com `senha_hash`; V1..V22; FK única usuário-funcionário; API mantém `senhaHash` no payload atual. Nenhum seed/senha/valor de credencial foi reproduzido.

## Sessão de execução 1

- Decisões confirmadas pelo usuário: email obrigatório e válido; login extraído do local-part e normalizado para minúsculas; colisão rejeita o cadastro inteiro. Senhas novas protegidas com BCrypt; preservar a conta ADMIN legada sem alterar credenciais na migration. Credenciais legadas acima de 72 bytes UTF-8 permanecem válidas sem upgrade e sem truncamento.
- Cadastro valida email, deriva login e cria funcionário e usuário `FUNCIONARIO` na mesma `DatabaseConfig.session`/transação; login duplicado é verificado antes do insert. A conta recebe marcador não utilizável e `primeiro_acesso_feito=false`.
- `Usuario` e o repository incluem a flag. Login recusa conta pendente; BCrypt é verificado para hashes reconhecidos e senha legada é comparada como antes. Após credencial legada correta e compatível com BCrypt, o hash é migrado; falhas não alteram o valor persistido.
- Primeiro acesso aceita somente conta ativa pendente, rejeita senha abaixo de 6 caracteres ou acima de 72 bytes UTF-8, e atualiza hash BCrypt e flag juntos num `UPDATE` condicionado à flag ainda falsa.
- Criada V23 com default `FALSE` e backfill `TRUE` para contas existentes, sem atualizar credenciais. A migration **não foi aplicada**.
- Seed de dev preenche flag `TRUE` para os usuários já provisionados. Auditoria SQL agora espera V1..V23 e verifica a coluna booleana `NOT NULL` com default `FALSE`.
- OpenAPI de funcionário exige email na criação e descreve a conta provisionada; OpenAPI de auth explica bloqueio de pendentes, hashing e limites de senha. Payloads existentes permanecem inalterados.
- Testes unitários adicionados para derivação/validação do login, compatibilidade e migração BCrypt, bloqueio de pendente, limite UTF-8 e primeiro acesso. Rollback real e migration em PostgreSQL ainda precisam ser exercitados em ambiente isolado.
- Não executados Gradle/build/testes nem migration. Build/testes requerem autorização explícita; aplicação de migration requer autorização separada.
- Revisão estática: `git diff --check` passou. PyYAML não está instalado, então a validação automatizada de sintaxe YAML não foi executada; os arquivos OpenAPI foram inspecionados. A V23 não foi aplicada.

## Sessão de validação autorizada

- Usuário autorizou a execução dos testes do backend e a aplicação da V23 ao banco de desenvolvimento.
- `.\gradlew.bat :serverApp:test` — **BUILD SUCCESSFUL**; relatórios JUnit confirmam 18 testes, 0 falhas, 0 erros em `AuthUseCaseTest`, `FuncionarioProvisioningTest` e `FuncionarioSecurityTest`.
- A primeira execução dos testes encontrou falhas nos stubs/captors Mockito dos testes de autenticação; os testes foram corrigidos e a execução final passou integralmente.
- Flyway executado por `:serverApp:bootRun --args=--spring.main.web-application-type=none`, com `DATABASE_URL` de `.env.test.local`; log confirmou migração V23 aplicada com sucesso e schema na versão v23. A coluna foi adicionada com default false e backfill true. Nenhuma credencial foi atualizada; produção não foi alvo.
- `git diff --check` passou.
- Não foi feito teste de integração de rollback usando PostgreSQL descartável, pois não há harness configurado e evitaram-se writes adicionais permanentes no banco Neon de desenvolvimento. Essa validação permanece pendente; teste unitário confirma a derivação/validação e delegação de criação ao repository, mas não comprova rollback real.
- Não aplicado seed nem executada a auditoria SQL completa; nenhuma credencial ou connection string foi impressa.

## Validação manual pelo usuário

- Usuário confirmou que repetiu os testes no Swagger e validou os dados no banco: criação de funcionário e usuário, primeiro acesso e autenticação funcionaram.
- Também confirmou que a credencial ADMIN foi convertida para BCrypt após login para obter JWT; considerou esse comportamento correto e desejado.
- Track funcional validada pelo usuário no ambiente de desenvolvimento. Falha forçada para comprovar rollback não foi exercitada; nenhuma pendência funcional relatada.
