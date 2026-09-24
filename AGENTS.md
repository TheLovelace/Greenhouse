# AGENTS — Greenhouse

## Objetivo

Ser a referência operacional do domínio da plataforma de floricultura Greenhouse, abrangendo acesso de usuários, catálogo, vendas e compras, estoque, espécies de plantas, imagens, monitoramento de IoT, alertas e as aplicações backend e multiplataforma de suporte.

Ao trabalhar neste repositório:

- Leia a estrutura e o código existente antes de alterar qualquer coisa.
- Não assuma que a documentação representa integralmente o estado atual do projeto. Confirme o comportamento no código, nas migrations e nas configurações.
- Faça a menor alteração necessária para atender à tarefa.
- Preserve a arquitetura, as convenções e os contratos existentes, salvo quando a tarefa exigir explicitamente uma mudança.
- Mantenha alterações focadas no escopo solicitado.
- Valide as alterações antes de considerar a tarefa concluída.
- Quando houver incerteza, conflito ou informação ausente, investigue o repositório e sinalize a inconsistência em vez de inventar uma solução.

## Arquitetura e responsabilidades

- Respeite a separação entre os módulos cliente e o backend.
- `androidApp`, `iosApp`, `desktopApp` e `webApp` são aplicações cliente.
- `shared` contém código compartilhado entre as plataformas.
- `serverApp` é o backend JVM independente.
- Não coloque acesso direto ao PostgreSQL/Neon em nenhum módulo cliente.
- Faça os clientes consumirem a API do backend para acessar dados persistidos ou operações protegidas.
- Mantenha regras que dependem de estado persistido, segurança ou integridade de dados no backend, e não apenas no cliente.
- Antes de alterar `shared`, identifique os módulos que consomem o código afetado.
- Antes de alterar um contrato de API, identifique os clientes e serviços que dependem dele.

## Backend

- Preserve `serverApp` como aplicação Kotlin/JVM independente.
- Use o Gradle existente para build e execução do backend.
- Mantenha a construção da imagem Docker integrada ao Gradle por meio do Jib.
- Não introduza um `Dockerfile` separado para o `serverApp` sem uma decisão arquitetural explícita.
- Preserve a porta `8080` do container, salvo mudança intencional da arquitetura.
- Não altere `REGISTRY_OWNER` e `IMAGE_TAG` para valores fixos específicos de um ambiente.
- Não exponha credenciais ou configurações sensíveis dentro do código do backend.
- Ao executar tasks que utilizam a configuração atual do Jib, use `--no-configuration-cache`.
- Para o container local de teste, utilize:

```bash
./gradlew --no-configuration-cache :serverApp:runTest
```

## Banco de Dados

- O banco utiliza PostgreSQL hospedado no Neon.
- Considere `dev` como ambiente de desenvolvimento/testes e `production` como ambiente estável.
- Somente o backend deve possuir acesso às credenciais do banco.
- Nunca coloque connection strings, senhas ou outras credenciais do PostgreSQL/Neon em código cliente.
- Nunca versione secrets, tokens, senhas, Deploy Hooks ou credenciais MQTT.
- Antes de alterar o schema, leia as migrations existentes e verifique como as estruturas são utilizadas pelo backend.
- Toda alteração de schema deve ser representada por uma migration SQL versionada.
- Mantenha migrations, código do backend e estrutura esperada do banco consistentes.
- Não altere PKs, FKs, índices, constraints, status ou regras de integridade sem verificar os fluxos afetados.
- Não crie uma nova tabela, entidade ou conceito de domínio sem verificar se uma estrutura existente já possui a mesma responsabilidade.

## Regras de domínio

Ao trabalhar com as regras abaixo, trate os valores e invariantes existentes como contratos do domínio. Não altere esses contratos apenas para facilitar uma implementação.

### Acesso

- Mantenha os perfis de usuário `ADMIN` e `FUNCIONARIO` consistentes entre banco, backend e cliente.
- Preserve o relacionamento entre `usuario` e `funcionario`.

### Catálogo e imagens

- Mantenha `produto.codigo` como o código interno/SKU do produto.
- Não permita preços negativos.
- Preserve os relacionamentos de produto com categoria e espécie.
- Um registro de `imagem` deve estar vinculado a um produto ou a uma espécie, nunca aos dois simultaneamente.
- Não permita ordem de imagem menor que 1.

### Espécies e monitoramento ambiental

- Preserve os limites mínimo/máximo de temperatura, umidade do ar e umidade do solo.
- Quando mínimo e máximo estiverem definidos, o mínimo não pode ser maior que o máximo.

### Descontos

- Aceite somente `PERCENTUAL` e `VALOR_FIXO` como tipos de desconto existentes.
- Descontos percentuais devem permanecer entre os limites definidos pelo domínio: maiores que `0` e menores ou iguais a `100`.
- Descontos de valor fixo devem ser maiores que `0`.
- Quando as duas datas existirem, `data_fim` não pode ser anterior a `data_inicio`.

### Pedidos

- Preserve as modalidades `IMEDIATA` e `AGENDADA`.
- Preserve os status `ABERTO`, `CONFIRMADO`, `PRONTO_RETIRADA`, `CONCLUIDO` e `CANCELADO`.
- Preserve as transições válidas do pedido:
  - `ABERTO` -> `CONFIRMADO` -> `PRONTO_RETIRADA` -> `CONCLUIDO`
  - `ABERTO` -> `CANCELADO`
- Pedidos imediatos podem concluir sem passar por `PRONTO_RETIRADA`.
- `ABERTO` não deve alterar o estoque.
- Pedido agendado confirmado deve reservar estoque conforme a regra existente.
- `PRONTO_RETIRADA` deve manter a reserva.
- `CONCLUIDO` deve ajustar o estoque físico e o reservado conforme a regra existente.
- Quantidades de itens devem ser maiores que `0`.
- Preços unitários, desconto e total não podem ser negativos.

### Pagamentos

- Preserve os status `PENDENTE`, `PAGO` e `CANCELADO`.
- Não permita valores de pagamento menores ou iguais a `0`.

### Compras e estoque

- Preserve os status `PEDIDA`, `RECEBIDA` e `CANCELADA` das compras de fornecedores.
- `PEDIDA` não altera o estoque.
- `RECEBIDA` deve atualizar o estoque e gerar as movimentações correspondentes.
- Quantidades compradas devem ser maiores que `0`.
- Valores unitários de compra não podem ser negativos.
- `quantidade_fisica`, `quantidade_reservada` e `quantidade_minima` não podem ser negativas.
- O estoque reservado não pode ser maior que o estoque físico.
- Preserve os tipos de movimentação `ENTRADA`, `SAIDA`, `AJUSTE_ENTRADA`, `AJUSTE_SAIDA` e `PERDA`.
- Quantidades de movimentação devem ser maiores que `0`.

### IoT

- Preserve a relação entre dispositivo, leituras e alertas.
- Não reutilize credenciais MQTT entre ambientes.
- Um alerta deve monitorar pelo menos um entre temperatura, umidade do ar e umidade do solo.
- Antes de alterar tópicos MQTT, publishers ou subscribers, identifique todos os produtores e consumidores afetados.

## MQTT e ambientes

- Use HiveMQ como broker MQTT do projeto.
- Mantenha os ambientes `test` e `prod` isolados.
- O usuário MQTT de `test` possui somente permissão de `SUB`.
- O usuário MQTT de `prod` possui permissões de `PUB` e `SUB`.
- Nunca utilize credenciais de produção em desenvolvimento ou teste.
- Nunca coloque credenciais MQTT no repositório.

## CI/CD

- Trate `.github/workflows/ci.yml` como parte da infraestrutura do projeto. Não o altere sem verificar o impacto sobre validação e deploy.
- Preserve a separação entre validação e deploy.
- Não coloque secrets diretamente no workflow ou no código-fonte.
- O deploy do ambiente `test` deve continuar associado ao fluxo de desenvolvimento em `develop`, salvo mudança intencional da estratégia de branches.
- Preserve a rastreabilidade da imagem publicada no GHCR por meio do SHA do commit.
- Não substitua essa rastreabilidade por tags fixas como `latest` sem decisão explícita.
- Ao modificar o pipeline, valide o comportamento do job afetado e os jobs que dependem dele.

## Desenvolvimento e colaboração

- O projeto é desenvolvido por seis desenvolvedores. Evite alterações que possam interferir no trabalho de outra pessoa sem necessidade.
- Não sobrescreva, remova ou reescreva uma implementação existente sem primeiro entender sua finalidade.
- Não faça refatorações amplas durante uma tarefa pontual sem necessidade técnica.
- Não altere arquivos fora do escopo da tarefa.
- Evite adicionar dependências quando os recursos existentes forem suficientes.
- Preserve nomes de classes, funções, propriedades, tabelas, rotas, tasks e arquivos existentes, salvo quando uma mudança for necessária.
- Prefira alterações pequenas, localizadas e fáceis de revisar.
- Quando uma alteração atravessar backend, `shared`, cliente, banco ou infraestrutura, avalie todas as camadas afetadas antes de implementar.
- Não use a solução mais fácil no curto prazo se ela quebrar um limite arquitetural existente.

## Validação

- Escolha a validação mais específica capaz de comprovar a alteração realizada.
- Quando aplicável, utilize as tasks existentes do Gradle:
  - `checkCommitMessage`
  - `lintCheck`
  - `testDesktop`
  - `:shared:testAndroidHostTest`
  - `testWeb`
- Para execução local do container do backend, utilize:

```bash
./gradlew --no-configuration-cache :serverApp:runTest
```

- Para publicação da imagem pelo fluxo de CI, a task utilizada é:

```bash
./gradlew :serverApp:jib
```

- Se uma validação falhar, determine se a falha foi introduzida pela alteração atual ou se já existia.
- Não declare uma tarefa concluída quando uma validação necessária não tiver sido executada.
- Ao relatar a conclusão, informe o que foi alterado e o que foi efetivamente validado.

## Segurança

- Nunca revele ou reproduza secrets encontrados durante a análise do projeto.
- Não copie credenciais de `production` para `dev` ou vice-versa.
- Não mova credenciais do backend para qualquer cliente.
- Não imprima secrets em logs, mensagens de commit, documentação ou respostas.
- Preserve as proteções existentes de autenticação, autorização e acesso a dados ao modificar fluxos relacionados.

## Comunicação

- Dê preferência a evidências do próprio repositório para tomar decisões técnicas.
- Não invente endpoints, entidades, tabelas, campos, tasks ou comportamentos que não existam.
- Quando houver múltiplas abordagens plausíveis, explique a decisão técnica adotada e seu impacto.
- Se a implementação atual contradizer uma regra deste arquivo, investigue primeiro e sinalize a inconsistência antes de executar uma mudança estrutural.
