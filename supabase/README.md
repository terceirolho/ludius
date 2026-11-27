# supabase/

Banco de dados do Ludius (PostgreSQL no Supabase).

| Pasta | O que tem |
|---|---|
| `migrations/` | Cada arquivo muda o banco um passo; rodam em ordem de data |
| `tests/database/` | Testes pgTAP: segurança, agenda, comissão, cancelamento |
| `config.toml` | Configuração do Supabase local |

## Regras

- **Nunca edite uma migração que já foi aplicada na nuvem.** Para mudar algo, crie uma nova:
  `supabase migration new nome_da_mudanca`.
- Toda tabela nova precisa de `enable row level security`, políticas e um teste.
- Funções internas ficam no schema `private` (o app não consegue chamá-las).

## Rodar os testes

Com Docker Desktop ligado:

```bash
supabase start        # sobe o banco local (primeira vez demora)
supabase test db      # roda os testes
supabase stop
```

O GitHub também roda esses testes sozinho a cada pull request.

Sem Docker, dá para testar num Postgres comum com pgTAP: `scripts/banco-local/testar.sh`.
