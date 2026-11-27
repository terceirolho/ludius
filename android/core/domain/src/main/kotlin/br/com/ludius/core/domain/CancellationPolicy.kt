package br.com.ludius.core.domain

import java.time.Duration
import java.time.Instant

/**
 * Política de cancelamento aprovada em 25/11/2026, ainda sujeita à revisão do advogado.
 * Mesma regra de `private.client_refund_percent` no banco.
 */
object CancellationPolicy {

    private val CDC_WITHDRAWAL = Duration.ofDays(7)   // art. 49 do CDC
    private val FULL_REFUND_NOTICE = Duration.ofDays(7)
    private val HALF_REFUND_NOTICE = Duration.ofHours(48)

    /**
     * Percentual (0, 50 ou 100) que o contratante recebe de volta quando ELE cancela.
     * Quando o artista cancela, use [artistCancelledRefundPercent]: sempre 100%.
     */
    fun clientRefundPercent(contractedAt: Instant, eventStartsAt: Instant, cancelledAt: Instant): Int {
        val sinceContract = Duration.between(contractedAt, cancelledAt)
        val beforeEvent = Duration.between(cancelledAt, eventStartsAt)
        return when {
            // O direito de arrependimento do CDC vem primeiro e prevalece sobre o resto.
            sinceContract <= CDC_WITHDRAWAL -> 100
            beforeEvent > FULL_REFUND_NOTICE -> 100
            beforeEvent >= HALF_REFUND_NOTICE -> 50
            else -> 0
        }
    }

    /** Artista que cancela não paga multa em dinheiro: o contratante recebe tudo de volta. */
    const val artistCancelledRefundPercent: Int = 100
}
