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

## URL da API do frontend

O módulo `shared` recebe as URLs da API durante a compilação. Se não forem definidas, `API_URL` usa `http://localhost:8080/api` e `API_URL_ANDROID` usa `http://10.0.2.2:8080/api` (endereço do host visto pelo emulador Android).

No PowerShell, configure os endereços antes de compilar, se os padrões não corresponderem ao ambiente:

```powershell
$env:API_URL = "http://localhost:8080/api"
$env:API_URL_ANDROID = "http://10.0.2.2:8080/api"
.\gradlew.bat devDesktop
```

Para um dispositivo Android físico, configure `API_URL_ANDROID` com um endereço acessível na rede local. Como as URLs são incorporadas ao código gerado pelo Gradle, alterá-las exige compilar o app novamente. Essas URLs não substituem a configuração `DATABASE_URL` usada pelos testes do backend.
