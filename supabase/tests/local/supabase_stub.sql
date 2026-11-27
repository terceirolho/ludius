-- =============================================================================
-- Imitação mínima do Supabase para testar as migrações num Postgres comum.
-- O que faz: cria os papéis (anon, authenticated, service_role), a tabela
-- auth.users e a função auth.uid(), do mesmo jeito que o Supabase faz.
-- Uso: só pelo script run_local.sh. No Supabase de verdade isto já existe.
-- =============================================================================
do $$
begin
  if not exists (select 1 from pg_roles where rolname = 'anon') then
    create role anon nologin;
  end if;
  if not exists (select 1 from pg_roles where rolname = 'authenticated') then
    create role authenticated nologin;
  end if;
  if not exists (select 1 from pg_roles where rolname = 'service_role') then
    create role service_role nologin bypassrls;
  end if;
end $$;

create schema auth;
grant usage on schema auth to anon, authenticated, service_role;
grant usage on schema public to anon, authenticated, service_role;

create table auth.users (
  id uuid primary key default gen_random_uuid(),
  email text,
  raw_user_meta_data jsonb default '{}'::jsonb,
  created_at timestamptz default now()
);

-- Igual ao Supabase: o id do usuário vem do token (JWT) da requisição.
create function auth.uid() returns uuid
language sql stable
as $$
  select nullif(
    coalesce(
      current_setting('request.jwt.claim.sub', true),
      current_setting('request.jwt.claims', true)::jsonb ->> 'sub'
    ),
    ''
  )::uuid;
$$;

-- No Supabase, anon/authenticated recebem acesso às tabelas novas por padrão;
-- as migrações retiram e concedem o que for preciso.
alter default privileges in schema public grant all on tables to anon, authenticated, service_role;
alter default privileges in schema public grant all on functions to anon, authenticated, service_role;
