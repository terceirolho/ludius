-- =============================================================================
-- Ludius · Migração 3 de 4: vitrine e agenda
-- O que faz: cria os serviços que o artista oferece, a disponibilidade semanal,
-- os bloqueios de datas e os favoritos do contratante.
-- =============================================================================

-- Categorias de evento que o artista marca em cada serviço (decidido em 26/11).
create or replace function private.valid_event_categories(categories text[])
returns boolean
language sql
immutable
as $$
  select cardinality(categories) > 0
     and categories <@ array[
       'Festa', 'Festival', 'Show', 'Casamento',
       'Aniversário', 'Corporativo', 'Bar e restaurante'
     ]::text[];
$$;

-- -----------------------------------------------------------------------------
-- Serviços da vitrine
-- -----------------------------------------------------------------------------
create table public.services (
  id uuid primary key default gen_random_uuid(),
  artist_id uuid not null references public.profiles (id) on delete cascade,
  name text not null check (char_length(name) between 3 and 60),
  description text check (char_length(description) <= 1000),

  -- Dinheiro sempre em centavos (inteiro), nunca em decimal, para não perder centavos.
  price_cents integer not null check (price_cents > 0),
  extra_hour_price_cents integer check (extra_hour_price_cents > 0),

  -- Duração em blocos de 15 min, de 30 min a 12 h.
  duration_minutes integer not null
    check (duration_minutes between 30 and 720 and duration_minutes % 15 = 0),
  -- Antecedência mínima para contratar (até 90 dias).
  min_notice_hours integer not null default 48 check (min_notice_hours between 0 and 2160),
  -- Intervalo depois do evento para deslocamento e desmontagem.
  buffer_minutes integer not null default 60 check (buffer_minutes between 0 and 480),

  event_categories text[] not null check (private.valid_event_categories(event_categories)),
  music_genres text[] not null default '{}',
  -- Imagens já comprimidas pelo app (WebP, até 1080 px). Máximo 6.
  image_paths text[] not null default '{}' check (cardinality(image_paths) <= 6),
  -- Só links do YouTube ou do SoundCloud (vídeo não é enviado ao app).
  media_url text check (
    media_url ~* '^https://(www\.|m\.)?(youtube\.com|youtu\.be|soundcloud\.com)/'
  ),
  rider_path text,
  issues_invoice boolean not null default false,
  is_published boolean not null default false,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index services_artist_idx on public.services (artist_id);

create trigger services_updated_at
  before update on public.services
  for each row execute function private.set_updated_at();

alter table public.services enable row level security;

-- Serviços publicados de artistas ativos aparecem para todos os logados.
create policy "servicos publicados sao visiveis"
  on public.services for select
  to authenticated
  using (
    artist_id = auth.uid()
    or (is_published and private.is_artist(artist_id))
  );

-- Só quem tem conta de artista cria e mantém os próprios serviços.
create policy "artista cria os proprios servicos"
  on public.services for insert
  to authenticated
  with check (artist_id = auth.uid() and private.is_artist(auth.uid()));

create policy "artista altera os proprios servicos"
  on public.services for update
  to authenticated
  using (artist_id = auth.uid())
  with check (artist_id = auth.uid() and private.is_artist(auth.uid()));

create policy "artista apaga os proprios servicos"
  on public.services for delete
  to authenticated
  using (artist_id = auth.uid());

revoke all on public.services from anon, authenticated;
grant select, insert, update, delete on public.services to authenticated;

-- -----------------------------------------------------------------------------
-- Disponibilidade semanal do artista (vale para todos os serviços dele)
-- Ex.: quinta a sábado, das 18h às 2h. Horários no fuso de Brasília.
-- Se end_time <= start_time, a janela termina no dia seguinte (vira a noite).
-- -----------------------------------------------------------------------------
create table public.availability_rules (
  id uuid primary key default gen_random_uuid(),
  artist_id uuid not null references public.profiles (id) on delete cascade,
  weekday smallint not null check (weekday between 0 and 6), -- 0 = domingo
  start_time time not null,
  end_time time not null,
  created_at timestamptz not null default now()
);

create index availability_rules_artist_idx on public.availability_rules (artist_id, weekday);

-- -----------------------------------------------------------------------------
-- Datas bloqueadas pelo próprio artista (férias, compromisso pessoal).
-- Não guarda o motivo, porque a tabela é lida para calcular horários livres.
-- -----------------------------------------------------------------------------
create table public.availability_blocks (
  id uuid primary key default gen_random_uuid(),
  artist_id uuid not null references public.profiles (id) on delete cascade,
  period tstzrange not null check (not isempty(period)),
  created_at timestamptz not null default now(),
  -- Bloqueios do mesmo artista não podem se sobrepor.
  constraint availability_blocks_no_overlap
    exclude using gist (artist_id with =, period with &&)
);

-- As duas tabelas de agenda: leitura para quem está logado (calcular horários),
-- escrita só pelo próprio artista.
alter table public.availability_rules enable row level security;
alter table public.availability_blocks enable row level security;

create policy "agenda visivel para quem esta logado"
  on public.availability_rules for select to authenticated using (true);
create policy "artista gerencia a propria agenda"
  on public.availability_rules for all to authenticated
  using (artist_id = auth.uid())
  with check (artist_id = auth.uid() and private.is_artist(auth.uid()));

create policy "bloqueios visiveis para quem esta logado"
  on public.availability_blocks for select to authenticated using (true);
create policy "artista gerencia os proprios bloqueios"
  on public.availability_blocks for all to authenticated
  using (artist_id = auth.uid())
  with check (artist_id = auth.uid() and private.is_artist(auth.uid()));

revoke all on public.availability_rules, public.availability_blocks from anon, authenticated;
grant select, insert, update, delete
  on public.availability_rules, public.availability_blocks to authenticated;

-- -----------------------------------------------------------------------------
-- Favoritos do contratante ("Meus favoritos")
-- -----------------------------------------------------------------------------
create table public.favorites (
  client_id uuid not null references public.profiles (id) on delete cascade,
  artist_id uuid not null references public.profiles (id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (client_id, artist_id)
);

alter table public.favorites enable row level security;

create policy "cada um ve os proprios favoritos"
  on public.favorites for select to authenticated
  using (client_id = auth.uid());
create policy "cada um adiciona os proprios favoritos"
  on public.favorites for insert to authenticated
  with check (client_id = auth.uid() and private.is_artist(artist_id));
create policy "cada um remove os proprios favoritos"
  on public.favorites for delete to authenticated
  using (client_id = auth.uid());

revoke all on public.favorites from anon, authenticated;
grant select, insert, delete on public.favorites to authenticated;
