-- =============================================================================
-- Ludius · Migração 1 de 4: base
-- O que faz: cria extensões, o schema "private" (funções internas que o app
-- não enxerga), os tipos enumerados e as funções de validação e de cálculo
-- usadas pelas outras migrações.
-- =============================================================================

-- No Supabase as extensões ficam no schema "extensions".
create schema if not exists extensions;

-- btree_gist permite a restrição EXCLUDE da agenda (igualdade + sobreposição).
create extension if not exists btree_gist with schema extensions;

-- Schema interno: a API do Supabase só expõe o schema "public",
-- então nada aqui pode ser chamado diretamente pelo app.
create schema if not exists private;
revoke all on schema private from public;
grant usage on schema private to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- Tipos
-- -----------------------------------------------------------------------------

-- Tipo de conta escolhido na etapa 0 do cadastro.
create type public.account_type as enum ('artista', 'contratante');

-- Ciclo de vida de um pedido de contratação (máquina de estados).
-- As transições permitidas ficam em private.order_transition_allowed().
create type public.order_status as enum (
  'aguardando_pagamento', -- horário segurado por 15 min enquanto o contratante paga
  'aguardando',           -- pago ou pré-autorizado; artista tem 24 h para aceitar
  'agendado',             -- artista aceitou
  'efetivado',            -- apresentação aconteceu
  'cancelado',            -- cancelado por uma das partes
  'recusado',             -- artista recusou
  'expirado'              -- pagamento ou aceite não aconteceu no prazo
);

-- Tipos de consentimento registrados na etapa 2 do cadastro (LGPD).
create type public.consent_kind as enum (
  'termos_de_uso',
  'politica_de_privacidade',
  'comunicados_email',
  'comunicados_sms'
);

-- -----------------------------------------------------------------------------
-- Funções auxiliares
-- -----------------------------------------------------------------------------

-- Mantém a coluna updated_at sempre atualizada.
create or replace function private.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

-- Valida CPF (11 dígitos) ou CNPJ (14 dígitos) pelo dígito verificador.
-- Recebe só números. A mesma regra existe no app (TaxIdValidator.kt).
create or replace function private.is_valid_tax_id(doc text)
returns boolean
language plpgsql
immutable
as $$
declare
  digits int[];
  total int;
  dv int;
  weights int[];
  i int;
begin
  if doc is null or doc !~ '^(\d{11}|\d{14})$' then
    return false;
  end if;

  -- Sequências repetidas (000..., 111...) passam na conta mas são inválidas.
  if doc ~ '^(\d)\1+$' then
    return false;
  end if;

  digits := array(select substr(doc, n, 1)::int from generate_series(1, length(doc)) n);

  if length(doc) = 11 then
    -- CPF: dois dígitos verificadores, pesos decrescentes a partir de 10 e 11.
    for pos in 10..11 loop
      total := 0;
      for i in 1..(pos - 1) loop
        total := total + digits[i] * (pos + 1 - i);
      end loop;
      dv := (total * 10) % 11;
      if dv = 10 then dv := 0; end if;
      if dv <> digits[pos] then
        return false;
      end if;
    end loop;
    return true;
  end if;

  -- CNPJ: pesos 5..2,9..2 para o 1º dígito e 6..2,9..2 para o 2º.
  for pos in 13..14 loop
    if pos = 13 then
      weights := array[5,4,3,2,9,8,7,6,5,4,3,2];
    else
      weights := array[6,5,4,3,2,9,8,7,6,5,4,3,2];
    end if;
    total := 0;
    for i in 1..(pos - 1) loop
      total := total + digits[i] * weights[i];
    end loop;
    dv := total % 11;
    dv := case when dv < 2 then 0 else 11 - dv end;
    if dv <> digits[pos] then
      return false;
    end if;
  end loop;
  return true;
end;
$$;

-- Comissão da plataforma, em centavos.
-- Decidido em 25/11/2026: 10% descontado do artista + 5% de taxa de serviço
-- paga pelo contratante. Arredonda para o centavo mais próximo (meio para cima).
-- A mesma conta existe no app (FeeCalculator.kt); o valor que vale é o daqui.
create or replace function private.calc_fees(
  price_cents integer,
  out client_fee_cents integer,
  out artist_fee_cents integer
)
language sql
immutable
as $$
  select ((price_cents * 5 + 50) / 100)::integer,   -- 5% do contratante
         ((price_cents * 10 + 50) / 100)::integer;  -- 10% do artista
$$;

-- Percentual devolvido ao contratante quando ELE cancela (0, 50 ou 100).
-- Política aprovada em 25/11/2026, ainda sujeita à revisão do advogado:
--   * 100% se cancelar até 7 dias depois de contratar (art. 49 do CDC)
--     ou mais de 7 dias antes do evento;
--   * 50% se cancelar entre 7 dias e 48 h antes do evento;
--   * 0% com menos de 48 h ou se não comparecer.
-- Quando o ARTISTA cancela, o contratante sempre recebe 100% (fora desta função).
-- A mesma regra existe no app (CancellationPolicy.kt).
create or replace function private.client_refund_percent(
  contracted_at timestamptz,
  event_starts_at timestamptz,
  cancelled_at timestamptz
)
returns integer
language sql
immutable
as $$
  select case
    -- Direito de arrependimento do CDC vem primeiro: prevalece sobre o resto.
    when cancelled_at <= contracted_at + interval '7 days' then 100
    when event_starts_at - cancelled_at > interval '7 days' then 100
    when event_starts_at - cancelled_at >= interval '48 hours' then 50
    else 0
  end;
$$;

-- Quais mudanças de status de pedido são permitidas.
-- Qualquer outra é bloqueada pelo gatilho em public.orders.
create or replace function private.order_transition_allowed(
  from_status public.order_status,
  to_status public.order_status
)
returns boolean
language sql
immutable
as $$
  select (from_status, to_status) in (
    ('aguardando_pagamento'::public.order_status, 'aguardando'::public.order_status),
    ('aguardando_pagamento', 'expirado'),
    ('aguardando_pagamento', 'cancelado'),
    ('aguardando', 'agendado'),
    ('aguardando', 'recusado'),
    ('aguardando', 'expirado'),
    ('aguardando', 'cancelado'),
    ('agendado', 'efetivado'),
    ('agendado', 'cancelado')
  );
$$;

grant execute on all functions in schema private to authenticated, service_role;
