# Arquitetura - Sistema de Pastas

Este documento descreve a arquitetura de pastas (folder system) do projeto. A estrutura foi elaborada de forma a seguir os padrões de arquitetura [MVVM](https://learn.microsoft.com/en-us/dotnet/architecture/maui/mvvm) e [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html), utilizando os conceitos de `use_case`, `repository`, `adapter` e `entity`.

Para evitar redundâncias, os componentes de UI são centralizados em `/shared`, enquanto a lógica de back-end é centralizada em `/serverApp`.

## Descrição detalhada das camadas

```text
📦 serverApp
┗ 📂 src/main/kotlin/org/greenhouse
  ┣ 📂 controller
  ┣ 📂 usecase
  ┣ 📂 repository
  ┣ 📂 adapter
  ┣ 📂 entity
  ┗ 📜 Main.kt
```

### controller/

Agrupa as lógicas relacionadas às rotas e à entrada de requisições da aplicação.

### usecase/

Representa a lógica de módulos específicos, concentrando funções e operações mais complexas, como a publicação e o consumo de alertas.

### adapter/

Representa os serviços e integrações utilizados pela aplicação, sendo o local onde serão instanciadas conexões com o banco de dados, configurações de JWT Token, entre outros.

### entity/

Representa as entidades, DTOs e Models utilizados pelo projeto.

### repository/

Camada responsável por realizar as operações relacionadas às entidades no banco de dados.

```text
📦 shared
┗ 📂 src/commonMain/kotlin/org/greenhouse
  ┣ 📂 ui
  ┃ ┣ 📂 components
  ┃ ┣ 📂 screens
  ┗ 📂 style
```

### components/

Agrupa os componentes compartilhados do projeto relacionados à construção e ao design da interface.

### screens/

Representa as telas e páginas da aplicação.

### style/

Centraliza as configurações globais de estilização da aplicação.
