# O que falta na fase 0

A fase 0 tem duas partes: o código (este repositório) e as contas e documentos,
que dependem de você.

## Código

- [x] Repositório organizado, com guias e decisões registradas
- [x] App Android base: tema, componentes, abas de contratante e artista
- [x] Banco: tabelas principais, segurança por usuário, regra de agenda, comissão, testes
- [x] Verificação automática no GitHub (app e banco)
- [ ] Teste do fluxo do Stripe (depende da conta, abaixo)

## Contas a abrir (você)

| Conta | Para quê | Custo | Observação |
|---|---|---|---|
| Supabase | Banco na nuvem | Grátis para desenvolver; Pro (~US$ 25/mês) antes de lançar | Criar o projeto na região **South America (São Paulo)** |
| Stripe Brasil | Pagamentos | Sem mensalidade | Com o CNPJ. Pedir a liberação do **Pix** e perguntar a taxa do **Connect** no Brasil |
| Google Play Console | Publicar o app | US$ 25, uma vez | Conta de organização, com o CNPJ |
| Sentry | Erros do app | Grátis no início | Pode ficar para a fase 1 |

Quando criar o projeto do Supabase, **não me mande a chave `service_role`** nem a senha do banco.
Para ligar o GitHub ao Supabase, use a integração oficial em Project Settings > Integrations.

## Teste do Stripe (assim que a conta existir)

Ponto em aberto da proposta: confirmar que o Pix funciona com o modelo
"cobranças e repasses separados" (*separate charges and transfers*), que é o que
permite segurar o dinheiro até depois do evento. Vou montar o teste no modo de testes
do Stripe; preciso só que a conta exista e o Connect esteja ativado.

## Documentos (você + advogado)

- Termos de uso, política de privacidade e política de cancelamento revisados
  (rascunhos em `juridico/` nos arquivos do projeto).
- Análise da marca "Ludius" no INPI.
