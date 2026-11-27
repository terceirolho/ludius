-- =============================================================================
-- Ludius · Migração 4 de 4: pedidos
-- O que faz: cria os pedidos de contratação e o registro de eventos de cada
-- pedido. Aqui ficam as duas regras mais importantes do app:
--   1. o banco nunca deixa vender o mesmo horário duas vezes (EXCLUDE);
--   2. o status só muda seguindo a máquina de estados, e cada mudança fica
--      registrada num histórico que não pode ser alterado.
--
-- O app só LÊ pedidos. Criar, pagar, aceitar e cancelar passam pelo servidor
-- (Edge Functions na fase 1), que confere pagamento, prazos e valores.
-- =============================================================================

create table public.orders (
  id uuid primary key default gen_random_uuid(),
  client_id uuid not null references public.profiles (id),
  artist_id uuid not null references public.profiles (id),
  service_id uuid not null references public.services (id),
  status public.order_status not null default 'aguardando_pagamento',

  -- Dados do evento informados pelo contratante.
  event_name text not null check (char_length(event_name) between 1 and 40),
  guest_count integer check (guest_count > 0),
  event_cep text check (event_cep ~ '^\d{8}$'),
  event_address text not null,
  event_city text not null,
  client_note text check (char_length(client_note) <= 500),

  starts_at timestamptz not null,
  ends_at timestamptz not null,
  -- Cópia do intervalo do serviço no momento da compra.
  buffer_minutes integer not null default 0 check (buffer_minutes >= 0),
  -- Período que fica bloqueado na agenda: do início até o fim + intervalo.
  -- Preenchido pelo gatilho orders_prepare (não pelo app).
  blocked_range tstzrange,

  -- Valores em centavos, calculados pelo banco a partir do preço do serviço.
  price_cents integer not null,
  client_fee_cents integer not null,   -- 5% pago pelo contratante
  artist_fee_cents integer not null,   -- 10% descontado do artista
  total_cents integer generated always as (price_cents + client_fee_cents) stored,
  artist_payout_cents integer generated always as (price_cents - artist_fee_cents) stored,

  payment_method text check (payment_method in ('cartao', 'pix')),
  stripe_payment_intent_id text unique,
  accept_deadline timestamptz,          -- 24 h para o artista aceitar

  cancelled_by uuid references public.profiles (id),
  cancelled_at timestamptz,
  cancel_reason text check (char_length(cancel_reason) <= 500),

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),

  constraint orders_time_check check (ends_at > starts_at),
  constraint orders_not_self check (client_id <> artist_id),

  -- REGRA CRÍTICA: dois pedidos ativos do mesmo artista não podem ocupar
  -- o mesmo período. Mesmo que duas pessoas paguem no mesmo segundo,
  -- só uma gravação passa; a outra recebe erro de conflito.
  constraint orders_no_overlap exclude using gist (
    artist_id with =,
    blocked_range with &&
  ) where (status in ('aguardando_pagamento', 'aguardando', 'agendado'))
);

create index orders_client_idx on public.orders (client_id, starts_at desc);
create index orders_artist_idx on public.orders (artist_id, starts_at desc);
create index orders_status_idx on public.orders (status);

-- -----------------------------------------------------------------------------
-- Antes de gravar: copia preço e intervalo do serviço, calcula comissões e
-- o período bloqueado. Assim o valor nunca vem do app.
-- -----------------------------------------------------------------------------
create or replace function private.orders_prepare()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  svc public.services%rowtype;
  fees record;
begin
  if tg_op = 'INSERT' then
    select * into svc from public.services where id = new.service_id;
    if not found or svc.artist_id <> new.artist_id then
      raise exception 'Serviço não pertence a este artista'
        using errcode = 'foreign_key_violation';
    end if;

    new.price_cents := svc.price_cents;
    new.buffer_minutes := svc.buffer_minutes;
    select * into fees from private.calc_fees(svc.price_cents);
    new.client_fee_cents := fees.client_fee_cents;
    new.artist_fee_cents := fees.artist_fee_cents;
  end if;

  new.blocked_range := tstzrange(
    new.starts_at,
    new.ends_at + make_interval(mins => new.buffer_minutes),
    '[)'  -- inclui o início e exclui o fim: eventos encostados não conflitam
  );
  return new;
end;
$$;

create trigger orders_prepare
  before insert or update of starts_at, ends_at, buffer_minutes on public.orders
  for each row execute function private.orders_prepare();

create trigger orders_updated_at
  before update on public.orders
  for each row execute function private.set_updated_at();

-- -----------------------------------------------------------------------------
-- Histórico de eventos do pedido (prova em disputas e base das métricas).
-- -----------------------------------------------------------------------------
create table public.order_events (
  id bigint generated always as identity primary key,
  order_id uuid not null references public.orders (id),
  from_status public.order_status,
  to_status public.order_status not null,
  -- Quem fez a mudança; vazio quando foi o sistema (ex.: prazo expirado).
  actor_id uuid,
  created_at timestamptz not null default now()
);

create index order_events_order_idx on public.order_events (order_id, created_at);

-- Valida a mudança de status e grava o evento no histórico.
create or replace function private.orders_track_status()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if tg_op = 'UPDATE' and new.status is distinct from old.status then
    if not private.order_transition_allowed(old.status, new.status) then
      raise exception 'Mudança de status não permitida: % -> %', old.status, new.status
        using errcode = 'check_violation';
    end if;
  end if;

  if tg_op = 'INSERT' or new.status is distinct from old.status then
    insert into public.order_events (order_id, from_status, to_status, actor_id)
    values (
      new.id,
      case when tg_op = 'UPDATE' then old.status end,
      new.status,
      auth.uid()
    );
  end if;
  return new;
end;
$$;

-- Roda depois de gravar, para o id do pedido já existir no histórico.
-- A validação também acontece aqui: se falhar, a gravação inteira é desfeita.
create trigger orders_track_status
  after insert or update of status on public.orders
  for each row execute function private.orders_track_status();

-- O histórico não pode ser alterado nem apagado por ninguém.
create or replace function private.block_changes()
returns trigger
language plpgsql
as $$
begin
  raise exception 'Este registro não pode ser alterado nem apagado'
    using errcode = 'insufficient_privilege';
end;
$$;

create trigger order_events_immutable
  before update or delete on public.order_events
  for each row execute function private.block_changes();

-- -----------------------------------------------------------------------------
-- Segurança: só as duas partes do pedido leem; ninguém grava pelo app.
-- -----------------------------------------------------------------------------
alter table public.orders enable row level security;
alter table public.order_events enable row level security;

create policy "partes do pedido podem ler"
  on public.orders for select to authenticated
  using (auth.uid() in (client_id, artist_id));

create policy "partes do pedido leem o historico"
  on public.order_events for select to authenticated
  using (exists (
    select 1 from public.orders o
    where o.id = order_id and auth.uid() in (o.client_id, o.artist_id)
  ));

revoke all on public.orders, public.order_events from anon, authenticated;
grant select on public.orders, public.order_events to authenticated;
