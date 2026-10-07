# Questions — Provisionamento de conta de acesso do funcionário

## Open

- Nenhuma pergunta bloqueante para iniciar a implementação aprovada. A aplicação da migration e a execução de build/testes exigirão autorização explícita.

## Resolved

- **O que foi aprovado para a criação de funcionário?** → Consentimento confirmado pelo usuário: criar usuário associado ao funcionário, usar como base do login a parte do e-mail anterior a `@` e criar senha placeholder não utilizável até o primeiro acesso.
- **E-mail e colisão de login?** → Confirmado pelo usuário: exigir e-mail válido; gerar login com o local-part em minúsculas; rejeitar a operação inteira se o login já existir.
- **Qual o significado de `primeiro_acesso_feito`?** → `true` = primeiro acesso já feito; `false` = primeiro acesso ainda não feito. A conta nova fica `false`; a conclusão do primeiro acesso muda para `true`.
- **O domínio do e-mail compõe o login?** → Não; o login é derivado apenas do trecho anterior a `@`. Local-parts iguais em domínios diferentes colidem após normalização e a segunda criação é rejeitada integralmente.
- **Como proteger senhas e preservar a conta ADMIN existente?** → Usar BCrypt para novas senhas; autenticar hashes BCrypt válidos via BCrypt. Para credenciais legadas em texto, comparar como hoje e atualizar para BCrypt somente após login bem-sucedido. Não alterar nem resetar credenciais na migration.
- **Como migrar o estado de contas existentes?** → V23 adiciona `primeiro_acesso_feito` com default `false` e backfill `true` em todas as contas existentes, sem tocar `senha_hash` ou outros dados de acesso.
- **Qual o limite para novas senhas BCrypt?** → Rejeitar senhas acima de 72 bytes UTF-8, sem truncamento, mantendo mínimo de 6 caracteres; documentar o limite em bytes na OpenAPI.
- **Credencial legada acima de 72 bytes UTF-8?** → Preservar a comparação legada e o acesso; não fazer upgrade para BCrypt nem truncar a credencial. O upgrade pode ocorrer após uma futura troca de senha compatível.
- **A mudança requer alteração dos payloads existentes?** → Não há mudança de formato prevista: os requests/responses existentes de funcionário, login e primeiro acesso são suficientes. Atualizar descrições OpenAPI para a semântica; revisar schemas/status apenas se a implementação final exigir mudança de contrato.
