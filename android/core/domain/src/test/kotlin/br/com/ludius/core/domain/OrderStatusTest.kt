package br.com.ludius.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrderStatusTest {
    @Test fun aguardandoPodeVirarAgendado() =
        assertTrue(OrderStatus.AWAITING.canChangeTo(OrderStatus.SCHEDULED))

    @Test fun efetivadoEFinal() = assertTrue(OrderStatus.COMPLETED.isFinal)

    @Test fun naoVoltaParaTras() =
        assertFalse(OrderStatus.SCHEDULED.canChangeTo(OrderStatus.AWAITING))

    @Test fun nomesBatemComOBanco() =
        assertEquals(OrderStatus.SCHEDULED, OrderStatus.fromDb("agendado"))
}
