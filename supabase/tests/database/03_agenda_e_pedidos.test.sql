-- Testes da vitrine, da agenda e dos pedidos: só artista cria serviço,
-- o banco impede horário duplicado, o status segue a máquina de estados
-- e o app não consegue gravar pedidos.
begin;
create extension if not exists pgtap with schema extensions;
select plan(13);

insert into auth.users (id, email, raw_user_meta_data) values
  ('00000000-0000-0000-0000-00000000000a', 'ana@teste.com', '{"account_type":"artista"}'),
  ('00000000-0000-0000-0000-00000000000c', 'caio@teste.com', '{"account_type":"contratante"}'),
  ('00000000-0000-0000-0000-00000000000d', 'dora@teste.com', '{"account_type":"contratante"}');
update public.profiles set profile_completed_at = now(), signup_step = 4;

-- Ana cria um serviço (como artista logada).
set local role authenticated;
select set_config('request.jwt.claims', '{"sub":"00000000-0000-0000-0000-00000000000a"}', true);

select lives_ok(
  $$insert into public.services (id, artist_id, name, price_cents, duration_minutes,
      buffer_minutes, event_categories, is_published)
    values ('00000000-0000-0000-0000-0000000000f1', auth.uid(), 'Voz e violão', 100000, 180,
      60, array['Bar e restaurante', 'Aniversário'], true)$$,
  'artista cria serviço');

select throws_ok(
  $$insert into public.services (artist_id, name, price_cents, duration_minutes, event_categories)
    values (auth.uid(), 'Show', 100000, 100, array['Show'])$$,
  '23514', null, 'duração precisa ser múltipla de 15 min');

-- Caio (contratante) não pode criar serviço.
select set_config('request.jwt.claims', '{"sub":"00000000-0000-0000-0000-00000000000c"}', true);
select throws_ok(
  $$insert into public.services (artist_id, name, price_cents, duration_minutes, event_categories)
    values (auth.uid(), 'Falso', 100000, 60, array['Show'])$$,
  '42501', null, 'contratante não cria serviço');

select isnt_empty($$select * from public.services$$, 'contratante vê serviço publicado');

select throws_ok(
  $$insert into public.orders (client_id, artist_id, service_id, event_name, event_address,
      event_city, starts_at, ends_at)
    values (auth.uid(), '00000000-0000-0000-0000-00000000000a',
      '00000000-0000-0000-0000-0000000000f1', 'Aniversário', 'Rua A, 1', 'São Paulo',
      '2026-12-12 20:00-03', '2026-12-12 23:00-03')$$,
  '42501', null, 'app não grava pedido direto');

-- O servidor (aqui, o usuário administrador do teste) cria os pedidos.
reset role;

insert into public.orders (id, client_id, artist_id, service_id, event_name, event_address,
  event_city, starts_at, ends_at, price_cents, client_fee_cents, artist_fee_cents)
values ('00000000-0000-0000-0000-0000000000b1', '00000000-0000-0000-0000-00000000000c',
  '00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-0000000000f1',
  'Aniversário', 'Rua A, 1', 'São Paulo', '2026-12-12 20:00-03', '2026-12-12 23:00-03',
  1, 1, 1);  -- valores falsos de propósito: o banco precisa ignorá-los

select is(
  (select array[price_cents, client_fee_cents, artist_fee_cents, total_cents, artist_payout_cents]
     from public.orders where id = '00000000-0000-0000-0000-0000000000b1'),
  array[100000, 5000, 10000, 105000, 90000],
  'valores vêm do serviço e da comissão, não do app');

select throws_ok(
  $$insert into public.orders (client_id, artist_id, service_id, event_name, event_address,
      event_city, starts_at, ends_at, price_cents, client_fee_cents, artist_fee_cents)
    values ('00000000-0000-0000-0000-00000000000d', '00000000-0000-0000-0000-00000000000a',
      '00000000-0000-0000-0000-0000000000f1', 'Festa', 'Rua B, 2', 'São Paulo',
      '2026-12-12 22:00-03', '2026-12-13 01:00-03', 0, 0, 0)$$,
  '23P01', null, 'horário sobreposto é recusado');

select throws_ok(
  $$insert into public.orders (client_id, artist_id, service_id, event_name, event_address,
      event_city, starts_at, ends_at, price_cents, client_fee_cents, artist_fee_cents)
    values ('00000000-0000-0000-0000-00000000000d', '00000000-0000-0000-0000-00000000000a',
      '00000000-0000-0000-0000-0000000000f1', 'Festa', 'Rua B, 2', 'São Paulo',
      '2026-12-12 23:30-03', '2026-12-13 01:00-03', 0, 0, 0)$$,
  '23P01', null, 'respeita o intervalo de 60 min depois do evento');

select lives_ok(
  $$insert into public.orders (client_id, artist_id, service_id, event_name, event_address,
      event_city, starts_at, ends_at, price_cents, client_fee_cents, artist_fee_cents)
    values ('00000000-0000-0000-0000-00000000000d', '00000000-0000-0000-0000-00000000000a',
      '00000000-0000-0000-0000-0000000000f1', 'Almoço', 'Rua B, 2', 'São Paulo',
      '2026-12-12 13:00-03', '2026-12-12 16:00-03', 0, 0, 0)$$,
  'dois eventos no mesmo dia, com tempo entre eles');

-- Máquina de estados e histórico.
update public.orders set status = 'aguardando' where id = '00000000-0000-0000-0000-0000000000b1';
update public.orders set status = 'agendado' where id = '00000000-0000-0000-0000-0000000000b1';

select throws_ok(
  $$update public.orders set status = 'aguardando' where id = '00000000-0000-0000-0000-0000000000b1'$$,
  '23514', null, 'status não volta para trás');

select is(
  (select array_agg(to_status::text order by id) from public.order_events
     where order_id = '00000000-0000-0000-0000-0000000000b1'),
  array['aguardando_pagamento', 'aguardando', 'agendado'],
  'cada mudança fica no histórico');

select throws_ok(
  $$delete from public.order_events$$,
  '42501', null, 'histórico não pode ser apagado');

-- Depois de cancelado, o horário volta a ficar livre.
update public.orders set status = 'cancelado' where id = '00000000-0000-0000-0000-0000000000b1';
select lives_ok(
  $$insert into public.orders (client_id, artist_id, service_id, event_name, event_address,
      event_city, starts_at, ends_at, price_cents, client_fee_cents, artist_fee_cents)
    values ('00000000-0000-0000-0000-00000000000d', '00000000-0000-0000-0000-00000000000a',
      '00000000-0000-0000-0000-0000000000f1', 'Festa', 'Rua B, 2', 'São Paulo',
      '2026-12-12 21:00-03', '2026-12-12 23:00-03', 0, 0, 0)$$,
  'pedido cancelado libera o horário');

select * from finish();
rollback;
