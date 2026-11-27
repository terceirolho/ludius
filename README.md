# Ludius

Aplicativo para contratar artistas para eventos, inspirado no iFood.
MVP: Android, em português, só a categoria música.

> Status: **fase 0 (fundações)**. O app abre, mostra a tela de boas-vindas, as abas
> e um catálogo dos componentes visuais. O banco já tem as tabelas principais com
> segurança por usuário. Cadastro, busca, pagamento e chat chegam na fase 1.

## Como o projeto está organizado

```
ludius/
├── android/            App Android (Kotlin + Jetpack Compose)
│   ├── app/            Telas e navegação
│   └── core/
│       ├── designsystem/  Cores, fontes e componentes (botão, campo, selos)
│       └── domain/        Regras de negócio: CPF/CNPJ, comissão, cancelamento, status
├── supabase/           Banco de dados (PostgreSQL no Supabase)
│   ├── migrations/     Tabelas, regras de segurança e funções, em ordem
│   └── tests/          Testes automáticos do banco
├── docs/               Guias e decisões do projeto
└── .github/workflows/  Verificação automática a cada mudança
```

## Por onde começar

1. [Configurar o computador](docs/01-configurar-ambiente.md) (Windows + Ubuntu no WSL)
2. [Como tudo se conecta](docs/02-arquitetura.md)
3. [Banco de dados](docs/03-banco-de-dados.md)
4. [O que falta na fase 0](docs/04-fase-0-pendencias.md) (contas a abrir, teste do Stripe)
5. [Decisões registradas (ADRs)](docs/adr/)

## Regras do código

- Todo arquivo começa com uma descrição curta do que faz.
- Linhas críticas (dinheiro, agenda, segurança, dados pessoais) têm comentário explicando o porquê.
- Regras de dinheiro e agenda valem **no banco**; o app só mostra.
- Nenhuma chave secreta no código ou no app. Segredos ficam no `local.properties` (fora do Git)
  ou nas configurações do Supabase/GitHub.
- Cada mudança entra por pull request e só é aceita com a verificação automática verde.

## Referências

- [Proposta de arquitetura e escopo](https://claude.ai/artifact/FTtcaBg7nRyZk88yBEwRmB)
- [Protótipo navegável](https://claude.ai/artifact/YVJkC2jiZoXL27egSY6NvE)
