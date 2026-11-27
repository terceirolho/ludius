-- Testes de cadastro e privacidade: perfil criado no cadastro, dados pessoais
-- visíveis só para o dono, campos protegidos e consentimentos imutáveis.
begin;
create extension if not exists pgtap with schema extensions;
select plan(11);

-- Duas pessoas: Ana (artista) e Caio (contratante).
insert into auth.users (id, email, raw_user_meta_data) values
  ('00000000-0000-0000-0000-00000000000a', 'ana@teste.com', '{"account_type":"artista"}'),
  ('00000000-0000-0000-0000-00000000000c', 'caio@teste.com', '{"account_type":"contratante"}');

select is(
  (select account_type::text from public.profiles where id = '00000000-0000-0000-0000-00000000000a'),
  'artista', 'perfil criado no cadastro com o tipo de conta');

select throws_ok(
  $$insert into auth.users (id, email, raw_user_meta_data)
    values ('00000000-0000-0000-0000-0000000000ff', 'x@teste.com', '{}')$$,
  '23514', null, 'cadastro sem tipo de conta é recusado');

insert into public.personal_data (user_id, first_name, last_name, birth_date, phone, tax_id)
values ('00000000-0000-0000-0000-00000000000a', 'Ana', 'Souza', '1995-03-10', '+5511999998888', '52998224725');

select throws_ok(
  $$insert into public.personal_data (user_id, first_name, last_name, birth_date, phone, tax_id)
    values ('00000000-0000-0000-0000-00000000000c', 'Caio', 'Lima', current_date - interval '17 years',
            '+5511988887777', '11222333000181')$$,
  '23514', null, 'menor de 18 anos é recusado');

select throws_ok(
  $$insert into public.personal_data (user_id, first_name, last_name, birth_date, phone, tax_id)
    values ('00000000-0000-0000-0000-00000000000c', 'Caio', 'Lima', '1990-01-01',
            '+5511988887777', '52998224725')$$,
  '23505', null, 'CPF repetido é recusado');

-- A partir daqui, age como o Caio (contratante logado).
set local role authenticated;
select set_config('request.jwt.claims', '{"sub":"00000000-0000-0000-0000-00000000000c"}', true);

select is_empty(
  $$select * from public.personal_data where user_id = '00000000-0000-0000-0000-00000000000a'$$,
  'Caio não vê os dados pessoais da Ana');

select is_empty(
  $$select * from public.profiles where id = '00000000-0000-0000-0000-00000000000a'$$,
  'perfil incompleto não aparece para os outros');

select isnt_empty(
  $$select * from public.profiles where id = '00000000-0000-0000-0000-00000000000c'$$,
  'cada um vê o próprio perfil');

select lives_ok(
  $$update public.profiles set display_name = 'Bar do Caio' where id = auth.uid()$$,
  'pode editar o nome do negócio');

select throws_ok(
  $$update public.profiles set suspended_at = null, signup_step = 4 where id = auth.uid()$$,
  '42501', null, 'não pode mudar etapa do cadastro nem suspensão');

insert into public.consents (user_id, kind, document_version, granted)
values (auth.uid(), 'termos_de_uso', 'termos-2026-11', true);

select throws_ok(
  $$update public.consents set granted = false$$,
  '42501', null, 'consentimento registrado não pode ser alterado');

select throws_ok(
  $$insert into public.consents (user_id, kind, document_version, granted)
    values ('00000000-0000-0000-0000-00000000000a', 'termos_de_uso', 'termos-2026-11', true)$$,
  '42501', null, 'não pode registrar consentimento em nome de outra pessoa');

reset role;
select * from finish();
rollback;
