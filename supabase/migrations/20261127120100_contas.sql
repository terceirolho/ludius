-- =============================================================================
-- Ludius · Migração 2 de 4: contas
-- O que faz: cria o perfil público (@usuário), os dados pessoais privados e o
-- registro de consentimentos (LGPD). O perfil nasce sozinho quando alguém se
-- cadastra no Supabase Auth.
--
-- Regra de ouro: toda tabela tem Row Level Security (RLS) ligada. Sem uma
-- política que permita, ninguém lê nem grava nada.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Perfil público
-- -----------------------------------------------------------------------------
create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  account_type public.account_type not null,

  -- @usuário: só minúsculas, números, ponto e sublinhado.
  username text unique check (username ~ '^[a-z0-9_.]{3,30}$'),
  -- Nome artístico (artista) ou nome do negócio (contratante).
  display_name text check (char_length(display_name) between 2 and 60),
  bio text check (char_length(bio) <= 240),
  avatar_path text,
  city text,
  state char(2) check (state ~ '^[A-Z]{2}$'),
  -- Campo único de gênero, com opções inclusivas; vazio = prefiro não informar.
  gender text,

  -- Redes externas. Nunca e-mail ou telefone no perfil público.
  instagram text,
  spotify text,
  youtube text,
  website text,

  -- Só artista: gêneros musicais e tipo (Voz e violão, Banda, DJ, ...).
  music_genres text[] not null default '{}',
  artist_types text[] not null default '{}',
  -- Só contratante: categoria do espaço (bar, casamento, ...).
  venue_category text,

  -- Etapa do cadastro concluída: 1 essencial, 2 consentimentos,
  -- 3 e-mail confirmado, 4 perfil completo. Alterada só pelo servidor.
  signup_step smallint not null default 1 check (signup_step between 1 and 4),
  profile_completed_at timestamptz,
  suspended_at timestamptz,
  deleted_at timestamptz,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

comment on table public.profiles is
  'Perfil público de artistas e contratantes. Dados pessoais ficam em personal_data.';

create trigger profiles_updated_at
  before update on public.profiles
  for each row execute function private.set_updated_at();

-- Busca de artistas por cidade e gênero musical (aba Buscar).
create index profiles_city_idx on public.profiles (city) where deleted_at is null;
create index profiles_music_genres_idx on public.profiles using gin (music_genres);

alter table public.profiles enable row level security;

-- Qualquer pessoa logada vê perfis completos e ativos; cada um vê o próprio sempre.
create policy "perfis ativos sao visiveis para quem esta logado"
  on public.profiles for select
  to authenticated
  using (
    id = auth.uid()
    or (profile_completed_at is not null and deleted_at is null and suspended_at is null)
  );

create policy "cada um edita o proprio perfil"
  on public.profiles for update
  to authenticated
  using (id = auth.uid())
  with check (id = auth.uid());

-- Segurança por coluna: o app só pode mudar os campos abaixo.
-- Tipo de conta, etapa do cadastro e suspensão ficam só com o servidor.
revoke all on public.profiles from anon, authenticated;
grant select on public.profiles to authenticated;
grant update (
  username, display_name, bio, avatar_path, city, state, gender,
  instagram, spotify, youtube, website,
  music_genres, artist_types, venue_category
) on public.profiles to authenticated;

-- Atalho usado nas políticas das outras tabelas.
create or replace function private.is_artist(user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.profiles p
    where p.id = user_id
      and p.account_type = 'artista'
      and p.deleted_at is null
      and p.suspended_at is null
  );
$$;

-- -----------------------------------------------------------------------------
-- Criação automática do perfil no cadastro
-- -----------------------------------------------------------------------------
-- O app envia o tipo de conta nos metadados do cadastro:
--   supabase.auth.signUp(..., data = { "account_type": "artista" })
create or replace function private.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  chosen text := new.raw_user_meta_data ->> 'account_type';
begin
  if chosen is null or chosen not in ('artista', 'contratante') then
    raise exception 'Tipo de conta obrigatório: artista ou contratante'
      using errcode = 'check_violation';
  end if;

  insert into public.profiles (id, account_type)
  values (new.id, chosen::public.account_type);
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function private.handle_new_user();

-- -----------------------------------------------------------------------------
-- Dados pessoais (nunca públicos)
-- -----------------------------------------------------------------------------
create table public.personal_data (
  user_id uuid primary key references public.profiles (id) on delete cascade,
  first_name text not null check (char_length(first_name) between 1 and 60),
  last_name text not null check (char_length(last_name) between 1 and 80),
  birth_date date not null,
  -- Telefone no formato internacional: +55 + DDD + número.
  phone text not null check (phone ~ '^\+55\d{10,11}$'),
  -- CPF ou CNPJ só com números, validado pelo dígito verificador e único.
  tax_id text not null unique check (private.is_valid_tax_id(tax_id)),
  cep text check (cep ~ '^\d{8}$'),
  street text,
  street_number text,
  complement text,
  neighborhood text,
  city text,
  state char(2) check (state ~ '^[A-Z]{2}$'),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

comment on table public.personal_data is
  'Dados pessoais (LGPD). Só o dono lê e altera. Criptografia de colunas prevista na fase 1 (ver docs/adr/0004).';

create trigger personal_data_updated_at
  before update on public.personal_data
  for each row execute function private.set_updated_at();

-- Maioridade: só maiores de 18 anos. Fica num gatilho porque depende da data de hoje.
create or replace function private.check_adult()
returns trigger
language plpgsql
as $$
begin
  if new.birth_date > (current_date - interval '18 years') then
    raise exception 'É preciso ter 18 anos ou mais'
      using errcode = 'check_violation';
  end if;
  return new;
end;
$$;

create trigger personal_data_adult
  before insert or update of birth_date on public.personal_data
  for each row execute function private.check_adult();

alter table public.personal_data enable row level security;

create policy "dono le os proprios dados"
  on public.personal_data for select
  to authenticated
  using (user_id = auth.uid());

create policy "dono cadastra os proprios dados"
  on public.personal_data for insert
  to authenticated
  with check (user_id = auth.uid());

create policy "dono altera os proprios dados"
  on public.personal_data for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

revoke all on public.personal_data from anon, authenticated;
grant select, insert, update on public.personal_data to authenticated;

-- -----------------------------------------------------------------------------
-- Consentimentos (LGPD): registro que nunca é alterado, só acrescentado.
-- Revogar = gravar uma nova linha com granted = false.
-- -----------------------------------------------------------------------------
create table public.consents (
  id bigint generated always as identity primary key,
  user_id uuid not null references public.profiles (id) on delete cascade,
  kind public.consent_kind not null,
  -- Versão do texto aceito (ex.: "termos-2026-11"), para provar o que foi aceito.
  document_version text not null,
  granted boolean not null,
  created_at timestamptz not null default now()
);

create index consents_user_idx on public.consents (user_id, kind, created_at desc);

alter table public.consents enable row level security;

create policy "dono ve os proprios consentimentos"
  on public.consents for select
  to authenticated
  using (user_id = auth.uid());

create policy "dono registra os proprios consentimentos"
  on public.consents for insert
  to authenticated
  with check (user_id = auth.uid());

-- Sem update nem delete para ninguém do app: o histórico é prova.
revoke all on public.consents from anon, authenticated;
grant select, insert on public.consents to authenticated;
