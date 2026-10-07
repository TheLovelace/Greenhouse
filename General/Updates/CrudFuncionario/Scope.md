# Scope — CrudFuncionario

Este update reúne responsabilidades do backend `serverApp`: autorização do CRUD de funcionário, provisionamento da conta associada, documentação OpenAPI e a extensão planejada para sincronizar o status ativo da conta vinculada ao funcionário.

## Tracks

- Backend — 100% (concluída; autorização do CRUD e documentação OpenAPI).
- ProvisionamentoAcesso — 100% (testes automatizados, migration no desenvolvimento e validação manual via Swagger/banco concluídos; rollback forçado não exercitado).
- OpenApi — 100% (parâmetros de rota corrigidos; usuário confirmou que aparecem corretamente no Swagger UI).
- SincronizacaoAtivoUsuario — implementação concluída; validação PostgreSQL isolada pendente.

## Dentro do escopo

- Manter as cinco rotas do CRUD de funcionário protegidas por JWT e perfil `ADMIN`, com Swagger UI e documentos OpenAPI públicos.
- Na criação de funcionário, criar a conta `usuario` associada na mesma transação, derivar o login da parte do e-mail anterior a `@`, iniciar a conta com senha provisória não utilizável e `primeiro_acesso_feito = false`.
- Proteger novas senhas com BCrypt e converter credenciais legadas gradualmente, apenas após autenticação bem-sucedida, preservando a única conta ADMIN existente.
- Bloquear login enquanto `primeiro_acesso_feito = false`; no primeiro acesso, atualizar a senha e definir a flag como `true`.
- Versionar V23 com backfill `true` das contas já existentes e default `false` para novas, sem redefinir credenciais; aplicar limite BCrypt de 72 bytes UTF-8 com validação e documentação OpenAPI.
- Atualizar testes, validações do schema/seed e documentação OpenAPI pertinente.
- Na extensão OpenApi, explicitar parâmetros `in: path` obrigatórios para todas as operações documentadas que usam ID na rota, alinhar referências do documento principal com os paths reais dos fragments e validar a apresentação no Swagger UI.
- Na extensão SincronizacaoAtivoUsuario, manter `usuario.ativo` igual a `funcionario.ativo` ao desativar pelo DELETE ou alterar `ativo` pelo PATCH, dentro da mesma transação de persistência do funcionário; cobrir a persistência com testes focados.

## Fora do escopo

- Mudanças nos clientes multiplataforma ou em `shared`.
- Alterar a autorização concluída na track Backend, o perfil da conta criada (`FUNCIONARIO`) ou outras regras do CRUD sem necessidade direta para este fluxo.
- Sincronizar/renomear automaticamente o login em alterações posteriores do e-mail do funcionário; esta extensão trata o provisionamento na criação.
- Resetar/substituir as credenciais existentes na migration ou exigir redefinição da senha da conta ADMIN atual.
- Aplicar migrations ou executar build/testes durante esta etapa de planejamento.
- Alterar endpoints/controllers ou outros contratos/comportamentos de runtime; criar operações que não existem no backend; ampliar o trabalho a componentes de cliente ou a mudanças de persistência além do espelhamento de `ativo` definido nesta extensão.
- Revogar ou bloquear JWT já emitidos por causa da desativação, alterar outras regras de autenticação/conta, mudar a exclusão física existente ou adicionar migration/schema para a sincronização de status.
