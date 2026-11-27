# 0003. Stripe Connect, sem carteira própria

Data: 24/11/2026 · Status: aceita

**Contexto.** Guardar dinheiro de usuários e fazer Pix a partir de um saldo próprio é
atividade regulada pelo Banco Central.

**Decisão.** Stripe Brasil com Connect. Cartão é pré-autorizado e capturado após o aceite
do artista (até 24 h); Pix é cobrado e estornado se não houver aceite. O dinheiro fica no
Stripe até depois do evento. A "Carteira" do app é só um extrato do saldo no Stripe.
Comissão: 10% do artista + 5% do contratante, calculada no banco.

**Consequências.** Sem custódia própria. No cartão o artista recebe em cerca de 30 dias,
no Pix em 2 dias úteis. Falta confirmar no ambiente de testes que o Pix funciona com
cobranças e repasses separados.
