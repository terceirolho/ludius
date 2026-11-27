package br.com.ludius.core.domain

/**
 * Valores de uma contratação, sempre em centavos (Long) para não perder centavos
 * com contas de número decimal.
 */
data class OrderAmounts(
    val priceCents: Long,       // preço do serviço definido pelo artista
    val clientFeeCents: Long,   // taxa de serviço paga pelo contratante (5%)
    val artistFeeCents: Long,   // comissão descontada do artista (10%)
) {
    /** O que o contratante paga. */
    val totalCents: Long get() = priceCents + clientFeeCents

    /** O que o artista recebe, antes das taxas do Stripe. */
    val artistPayoutCents: Long get() = priceCents - artistFeeCents
}

/**
 * Calcula a comissão da plataforma (decidido em 25/11/2026).
 * Mesma conta de `private.calc_fees` no banco; o valor cobrado é sempre o do banco.
 */
object FeeCalculator {
    const val CLIENT_FEE_PERCENT = 5
    const val ARTIST_FEE_PERCENT = 10

    fun calculate(priceCents: Long): OrderAmounts {
        require(priceCents > 0) { "O preço precisa ser maior que zero" }
        return OrderAmounts(
            priceCents = priceCents,
            clientFeeCents = percentOf(priceCents, CLIENT_FEE_PERCENT),
            artistFeeCents = percentOf(priceCents, ARTIST_FEE_PERCENT),
        )
    }

    // Arredonda para o centavo mais próximo, meio centavo para cima (igual ao banco).
    private fun percentOf(cents: Long, percent: Int): Long = (cents * percent + 50) / 100
}
