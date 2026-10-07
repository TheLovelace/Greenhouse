# Plan — OpenAPI: parâmetros de rota com ID

## TL;DR

Corrigir a representação/renderização dos parâmetros de rota ID em todas as operações OpenAPI que os utilizam, verificar a correspondência entre os `$ref` de `openapi.yaml` e os paths dos fragments e remover somente a referência OpenAPI sem destino de `/desconto/{id}/status`. Não alterar endpoints, comportamento do backend, payloads ou schema do banco.

## Estado atual confirmado

- `serverApp/src/main/resources/openapi.yaml` monta as rotas com referências externas a Path Items em fragments sob `serverApp/src/main/resources/openapi/`.
- Os paths parametrizados ativos encontrados no documento e nos fragments são:
  - `categoria.yml`: `/categoria/{id}` — `GET`, `PATCH`, `DELETE`.
  - `cliente.yml`: `/cliente/{id}` — `GET`, `PATCH`.
  - `desconto.yml`: `/desconto/{id}` — `GET`, `PATCH`, `DELETE`.
  - `estoque.yml`: `/estoque/{produtoId}` — `GET` de posição de estoque.
  - `produto.yml`: `/produto/{id}` — `GET`, `PATCH`, `DELETE`.
  - `funcionario.yml`: `/{id}` no fragment, montado como `/funcionario/{id}` — `GET`, `PATCH`, `DELETE`.
- Os seis paths ativos já declaram seus parâmetros no nível do Path Item, com `in: path`, `required: true`, schema `integer`/`int64` e `minimum: 1`. Isso é válido em OpenAPI e aplica o parâmetro às operações daquele path; portanto, a ausência observada no Swagger UI não se explica simplesmente pela falta dessas declarações.
- Os `$ref` de rota ID listados acima apontam para paths existentes nos fragments. O fragment de funcionário usa paths relativos à raiz (`/` e `/{id}`), e as referências atuais de `/funcionario` e `/funcionario/{id}` apontam para essas chaves.
- `openapi.yaml` também referencia `/desconto/{id}/status` por um pointer que não existe em `desconto.yml`. O `DescontoController` não declara essa rota; suas operações com ID são `GET`, `PATCH` e `DELETE` em `/{id}`.
- `cliente.yml` também tem parâmetros de query no `GET /cliente`; eles não são parâmetros de rota e devem ser preservados sem alteração.
- O Swagger UI está configurado para carregar `/api/openapi.yaml` (`serverApp/src/main/resources/application.properties`); `OpenApiController` serve o documento principal e os fragments aprovados. A renderização visual real ainda não foi reproduzida nesta etapa.
- **Hipótese, não causa comprovada:** como as declarações path-level válidas não aparecem na UI segundo o relato, explicitar o parâmetro no objeto de cada operação pode contornar a forma como a UI resolve Path Items externos. A causa só será confirmada ao conferir a especificação efetivamente carregada/renderizada.

## Steps

- [x] Revisar os paths de todos os fragments e o controller correspondente para manter como inventário somente operações realmente documentadas/implementadas que usam placeholder de rota. Não criar uma rota para `/desconto/{id}/status`.
- [x] Nos fragments `categoria.yml`, `cliente.yml`, `desconto.yml`, `estoque.yml`, `produto.yml` e `funcionario.yml`, declarar o parâmetro path ID em cada operação que o usa (`GET`, `PATCH` e/ou `DELETE` conforme o path). Manter nome, descrição e schema compatíveis com os valores já documentados e com os controllers (`Long` → `integer`/`int64`, mínimo 1); usar `required: true`.
- [x] Promover a declaração atualmente no Path Item para cada operação que usa o ID e remover a declaração comum do Path Item para não duplicar o mesmo parâmetro efetivo. Essa é a correção focada planejada diante do sintoma relatado; se a especificação servida/renderizada ainda não resolver os parâmetros, investigar o alvo/resultado dos `$ref` antes de propor qualquer mudança além dos arquivos desta track. Preservar os parâmetros de query e demais parâmetros não relacionados.
- [x] Em `openapi.yaml`, verificar cada referência externa de rota contra a chave efetivamente existente em seu fragment. Manter as referências corretas, inclusive a montagem de funcionário a partir de `/` e `/{id}`; remover apenas a entrada `/desconto/{id}/status` cujo destino não existe e para a qual não há rota no controller.
- [x] Fazer revisão estática dos YAMLs: cada placeholder (`{id}`/`{produtoId}`) deve ter exatamente um parâmetro efetivo correspondente em cada operação, com `in: path` e `required: true`; cada `$ref` de path deve resolver a um Path Item existente.
- [ ] Validar a documentação servida pelo backend e abrir o Swagger UI para conferir a seção Parameters das operações afetadas. A inspeção visual e a reprodução de screenshot não foram realizadas durante o planejamento; ficam pendentes para execução.

## Arquivos relevantes

### Alterar na implementação

- `serverApp/src/main/resources/openapi.yaml` — referências do documento principal; remover o `$ref` órfão de `/desconto/{id}/status` e confirmar correspondência dos demais pointers, sem normalização cosmética alheia à tarefa.
- `serverApp/src/main/resources/openapi/categoria.yml` — parâmetros de `GET`, `PATCH` e `DELETE /categoria/{id}`.
- `serverApp/src/main/resources/openapi/cliente.yml` — parâmetro de `GET` e `PATCH /cliente/{id}`; preservar filtros query do `GET /cliente`.
- `serverApp/src/main/resources/openapi/desconto.yml` — parâmetros de `GET`, `PATCH` e `DELETE /desconto/{id}`.
- `serverApp/src/main/resources/openapi/estoque.yml` — parâmetro de `GET /estoque/{produtoId}`.
- `serverApp/src/main/resources/openapi/produto.yml` — parâmetros de `GET`, `PATCH` e `DELETE /produto/{id}`.
- `serverApp/src/main/resources/openapi/funcionario.yml` — parâmetros de `GET`, `PATCH` e `DELETE /{id}` no fragment (montado como `/funcionario/{id}`).

### Consultar; não alterar para esta extensão

- `serverApp/src/main/kotlin/org/greenhouse/controller/CategoriaController.kt`, `ClienteController.kt`, `DescontoController.kt`, `EstoqueController.kt`, `ProdutoController.kt` e `FuncionarioController.kt` — confirmar rotas/verbos e tipos ID reais; nenhuma alteração de controller está no escopo.
- `serverApp/src/main/kotlin/org/greenhouse/controller/OpenApiController.kt` e `serverApp/src/main/resources/application.properties` — confirmar entrega dos documentos e URL configurada para a especificação; não alterar.
- `serverApp/src/main/resources/openapi/status.yml` — destino de referências de respostas já usadas por fragments; não alterar salvo se a revisão revelar bloqueio direto à validação, caso em que pausar e registrar a questão em vez de ampliar o escopo.

### Não alterar

- `shared/`, `androidApp/`, `iosApp/`, `desktopApp/` e `webApp/` — sem impacto esperado; não há mudança de contrato runtime.
- Controllers, services/use cases, repositories, segurança, migrations, schema SQL e payloads — nenhuma mudança de implementação ou persistência é necessária.

## Como verificar

- Verificação estática dos YAMLs: os parâmetros de rota devem aparecer em cada operação cujo path contém o placeholder correspondente, com `in: path`, `required: true` e tipo coerente com os controllers. Confirmar ausência de duplicação do parâmetro por operação e ausência do pointer órfão para `/desconto/{id}/status`.
- Resolver os `$ref` de `openapi.yaml` relativos à URL `/api/openapi.yaml` e confirmar que cada arquivo/path alvo existe, incluindo as referências com caracteres `{}` percent-encoded e os fragments `auth`.
- Consultar o documento principal e fragments servidos pelo backend e abrir a URL configurada do Swagger UI; conferir a seção Parameters em todas as operações listadas no inventário.
- Não executar build, testes Gradle ou alterar contratos de runtime nesta track. A validação visual do Swagger UI permanece pendente até a implementação ter ambiente acessível; screenshot de referência não está disponível.

## Decisões tomadas

- O sintoma não será tratado como simples ausência dos parâmetros: eles já existem no nível Path Item dos seis fragments ativos. A causa do comportamento no Swagger UI ainda é hipótese. A extensão os declara no nível de operação para tornar explícita a associação em cada operação renderizada após resolução dos `$ref`; não manter a mesma declaração também no nível Path Item. Confirmar na UI antes de considerar validada a hipótese.
- Cobrir todas as operações encontradas com ID, incluindo a consulta de estoque por `produtoId`, e não apenas PATCH de funcionário.
- Preservar os nomes reais (`id` ou `produtoId`) e os tipos já evidenciados nos controllers e fragments; não alterar semântica, payloads, endpoints nem código cliente.
- Não criar/documentar ação `/desconto/{id}/status`: a referência atual não resolve e não há handler em `DescontoController`. Remover somente a referência órfã do documento principal, sem criar uma operação ou endpoint.
- Manter as referências de funcionário para `/{id}` como estão se continuarem resolvendo, pois refletem a estrutura real do fragment; alinhar referências significa validar/corrigir destinos, não reorganizar paths por preferência.
- A renderização com navegador e a reprodução de screenshot não foram executadas nem são possíveis nesta fase; devem permanecer identificadas como validação manual pendente.
- Após o sintoma ser relatado, movidos os parâmetros de path do nível Path Item para o nível de cada operação em todos os seis fragments. Isso preserva as definições dos IDs, mas torna explícito o parâmetro para as operações renderizadas pelo Swagger UI.
- Validação estática executada: as 15 operações com placeholder possuem exatamente um parâmetro de path explícito, obrigatório e int64; todas as referências externas de paths resolvem; a referência órfã de desconto foi removida; `git diff --check` passou. A UI ainda precisa ser aberta para confirmação visual.
