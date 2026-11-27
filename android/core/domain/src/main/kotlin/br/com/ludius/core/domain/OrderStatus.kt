package br.com.ludius.core.domain

/**
 * Status de um pedido de contratação. Os nomes batem com o tipo `order_status` do banco.
 * [label] é o texto mostrado no app.
 */
enum class OrderStatus(val dbValue: String, val label: String) {
    AWAITING_PAYMENT("aguardando_pagamento", "Aguardando pagamento"),
    AWAITING("aguardando", "Aguardando"),
    SCHEDULED("agendado", "Agendado"),
    COMPLETED("efetivado", "Efetivado"),
    CANCELLED("cancelado", "Cancelado"),
    DECLINED("recusado", "Recusado"),
    EXPIRED("expirado", "Expirado");

    /** Status final: o pedido não muda mais. */
    val isFinal: Boolean get() = allowedNext.isEmpty()

    /** Para quais status este pode mudar. Igual a `private.order_transition_allowed` no banco. */
    val allowedNext: Set<OrderStatus>
        get() = when (this) {
            AWAITING_PAYMENT -> setOf(AWAITING, EXPIRED, CANCELLED)
            AWAITING -> setOf(SCHEDULED, DECLINED, EXPIRED, CANCELLED)
            SCHEDULED -> setOf(COMPLETED, CANCELLED)
            COMPLETED, CANCELLED, DECLINED, EXPIRED -> emptySet()
        }

    fun canChangeTo(next: OrderStatus): Boolean = next in allowedNext

    companion object {
        fun fromDb(value: String): OrderStatus =
            entries.firstOrNull { it.dbValue == value }
                ?: error("Status desconhecido: $value")
    }
}
