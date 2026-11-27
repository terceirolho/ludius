# 0002. Supabase como back-end

Data: 24/11/2026 · Status: aceita

**Contexto.** Equipe de uma pessoa, em tempo parcial. Precisa de login, banco, arquivos,
chat em tempo real e funções no servidor, com custo baixo e dados no Brasil.

**Decisão.** Supabase (PostgreSQL), região São Paulo, plano Pro antes do lançamento.
Mudanças no banco só por migrações versionadas em `supabase/migrations/`.

**Consequências.** Pouca infraestrutura para manter. Por ser Postgres comum, dá para
migrar para outro provedor sem reescrever o app. O ponto de atenção é a segurança por
linha (RLS): uma política errada expõe dados, por isso cada regra tem teste.
