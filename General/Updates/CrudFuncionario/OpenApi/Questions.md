# Questions — OpenAPI: parâmetros de rota com ID

## Open

- **Renderização no Swagger UI:** a verificação visual das seções Parameters ainda está pendente. O usuário deve recarregar a página e confirmar se os campos de rota aparecem em todas as operações após a alteração.

## Resolved

- **Os parâmetros ID estão ausentes dos fragments?** → Não. Os seis paths ativos já declaram parâmetros obrigatórios no nível Path Item. A extensão deve colocá-los em cada operação que usa o placeholder para tornar explícita a associação que precisa aparecer na UI, removendo a declaração do nível Path Item para não duplicar.
- **A referência `/desconto/{id}/status` deve gerar uma operação nova?** → Não. O pointer atual não existe em `desconto.yml` e `DescontoController` não declara essa rota; remover somente a referência documental órfã, sem criar endpoint/operação.
- **A estrutura incomum do fragment de funcionário exige reorganização?** → Não. O fragment contém `/` e `/{id}`, e as referências atuais do documento principal apontam para essas chaves. Validar e preservar essa forma se os destinos resolverem corretamente.
