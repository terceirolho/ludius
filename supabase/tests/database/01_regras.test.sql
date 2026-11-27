-- Testes das regras puras: CPF/CNPJ, comissão, multa de cancelamento e
-- máquina de estados dos pedidos. Os mesmos casos existem nos testes do app.
begin;
create extension if not exists pgtap with schema extensions;
select plan(17);

-- CPF e CNPJ
select ok(private.is_valid_tax_id('52998224725'), 'CPF válido');
select ok(not private.is_valid_tax_id('52998224724'), 'CPF com dígito errado');
select ok(not private.is_valid_tax_id('11111111111'), 'CPF com dígitos repetidos');
select ok(private.is_valid_tax_id('11222333000181'), 'CNPJ válido');
select ok(not private.is_valid_tax_id('11222333000180'), 'CNPJ com dígito errado');
select ok(not private.is_valid_tax_id('529.982.247-25'), 'só aceita números');

-- Comissão: show de R$ 1.000 → contratante paga R$ 50, artista deixa R$ 100.
select is((private.calc_fees(100000)).client_fee_cents, 5000, 'taxa do contratante 5%');
select is((private.calc_fees(100000)).artist_fee_cents, 10000, 'comissão do artista 10%');
-- R$ 333,33: 5% = 16,6665 → 16,67; 10% = 33,333 → 33,33
select is((private.calc_fees(33333)).client_fee_cents, 1667, 'arredonda meio para cima');
select is((private.calc_fees(33333)).artist_fee_cents, 3333, 'arredonda para baixo');

-- Política de cancelamento do contratante
select is(private.client_refund_percent(
  '2026-12-01 10:00-03', '2026-12-05 20:00-03', '2026-12-04 10:00-03'), 100,
  'até 7 dias depois de contratar: 100% (CDC), mesmo perto do evento');
select is(private.client_refund_percent(
  '2026-10-01 10:00-03', '2026-12-20 20:00-03', '2026-12-01 10:00-03'), 100,
  'mais de 7 dias antes do evento: 100%');
select is(private.client_refund_percent(
  '2026-10-01 10:00-03', '2026-12-05 20:00-03', '2026-12-01 10:00-03'), 50,
  'entre 7 dias e 48 h: 50%');
select is(private.client_refund_percent(
  '2026-10-01 10:00-03', '2026-12-05 20:00-03', '2026-12-04 21:00-03'), 0,
  'menos de 48 h: 0%');

-- Máquina de estados
select ok(private.order_transition_allowed('aguardando', 'agendado'), 'aguardando → agendado');
select ok(not private.order_transition_allowed('efetivado', 'cancelado'), 'efetivado é final');
select ok(not private.order_transition_allowed('agendado', 'aguardando'), 'não volta para trás');

select * from finish();
rollback;
