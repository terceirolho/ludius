# 0004. Regras críticas no banco e dados pessoais separados

Data: 27/11/2026 · Status: aceita

**Contexto.** O app roda no celular da pessoa e pode ser modificado. LGPD exige mínimo
de dados expostos e controle de acesso.

**Decisão.**
- Regras de dinheiro, agenda e permissão ficam no banco (restrições, gatilhos, RLS).
  O app repete algumas só para mostrar avisos antes de enviar.
- Pedidos só são gravados pelo servidor (Edge Functions); o app apenas lê.
- Dados pessoais ficam em `personal_data`, separados do perfil público, visíveis só ao dono.
- Consentimentos e histórico de pedidos são registros que não podem ser alterados.

**Pendente (fase 1).** Criptografar CPF/CNPJ, telefone, nascimento e endereço nas colunas,
além da criptografia de disco que o Supabase já faz, e mascarar esses dados no painel admin.
A unicidade do CPF passará a usar um hash do número.

**Consequências.** Mais trabalho no banco, mas um app adulterado não consegue mudar valores,
furar a agenda ou ler dados de outra pessoa.
