# Configuração local - Variáveis de ambiente

## Ambiente de teste

O ambiente de teste utiliza variáveis de ambiente para configurar a conexão com o banco de dados. Para execução local dos testes, deve ser criado o arquivo `.env.test.local` na raiz do projeto.

Formato esperado do arquivo `.env.test.local`:

```env
DATABASE_URL=jdbc:postgresql://server/db?user=user&password=password&sslmode=require&channel_binding=require
```

A variável `DATABASE_URL` deve conter a URL completa de conexão JDBC, incluindo as credenciais necessárias para autenticação no banco de dados.

> **IMPORTANTE:** O arquivo `.env.test.local` nunca deve ser versionado no GitHub. Mantenha-o apenas no ambiente local de desenvolvimento e certifique-se de que esteja incluído no `.gitignore`.

No ambiente de CI, a variável `DATABASE_URL` é disponibilizada por meio dos Secrets do GitHub, não sendo necessário versionar ou criar o arquivo `.env.test.local`.

O backend também exige `JWT_SECRET`, uma chave aleatória com pelo menos 32 bytes. Para execução local, adicione-a ao `.env.test.local` usando um valor não produtivo e mantenha o arquivo fora do Git. Use chaves diferentes para `test` e `prod`; nos serviços implantados no Render, configure cada valor diretamente nas variáveis/secrets do serviço. O repositório não confirma os valores configurados no painel do Render.
