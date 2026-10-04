# Fluxo de CRUD

Este documento relata o caminho que deve ser utilizado para realizar CRUDs. A ideia é manter cada parte com uma responsabilidade simples, sem criar camadas só por formalidade.

`Categoria` foi usada como base e exemplo.

## Caminho da requisição

```text
HTTP -> controller -> usecase -> repository -> PostgreSQL
```

- `controller/`: responsável por receber a rota, ler os parâmetros e devolver o status HTTP. Evite colocar regra de negócio aqui.
- `usecase/`: valida o que veio da requisição, aplica as regras do domínio e decide quando lançar uma exception existente.
- `repository/`: concentra o SQL e transforma as linhas do banco em entidades. Neste projeto, o repository é uma classe concreta.
- `entity/`: representa os dados usados entre as camadas. Se o ID é gerado pelo banco, ele pode ser opcional na criação, isso também vale para outros valores default.

No retorno, o caminho é o mesmo ao contrário: o repository devolve a entidade, o use case garante que ela faz sentido e o controller monta a resposta HTTP.

## Rotas e erros

Para categoria, as operações ficam em `/categoria`: `POST` cria, `GET` lista, `GET /{id}` busca, `PATCH /{id}` atualiza e `DELETE /{id}` exclui. O controller escolhe os status de sucesso, exceptions como `BadRequestException`, `NotFoundException` e `UnprocessableEntityException` são convertidas pelo handler global. É recomendado utilizar `PATCH` invés de `PUT`.

## Testes

Separe os casos por operação para facilitar achar o que falhou: `TestCategoriaPost`, `TestCategoriaGet`, `TestCategoriaPatch` e `TestCategoriaDelete`. Quando mais de uma classe precisar preparar ou limpar dados, deixe essa preparação em `integration/fixture` em vez de esconder uma classe auxiliar dentro do teste.

Os testes de integração usam registros temporários e limpam o que criaram. Assim dá para exercitar o repository real sem deixar lixo no banco.

## Ao começar outro CRUD

1. Confira o schema existente e os vínculos da tabela antes de escrever o mapeamento.
2. Crie a entidade e o repository concreto seguindo os tipos e limites reais das colunas.
3. Coloque validações e decisões de domínio no use case, reutilize as exceptions existentes.
4. Mapeie as rotas e status HTTP no controller.
5. Documente o contrato OpenAPI e divida os testes por operação.
6. Rode os testes JVM e o lint. Se depender de banco, confirme também que a configuração local está disponível.