package br.com.ludius.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeeCalculatorTest {
    @Test
    fun showDeMilReais() {
        val a = FeeCalculator.calculate(100_000)
        assertEquals(5_000, a.clientFeeCents)
        assertEquals(10_000, a.artistFeeCents)
        assertEquals(105_000, a.totalCents)
        assertEquals(90_000, a.artistPayoutCents)
    }

    @Test
    fun arredondaComoOBanco() {
        val a = FeeCalculator.calculate(33_333)
        assertEquals(1_667, a.clientFeeCents) // 16,6665 → 16,67
        assertEquals(3_333, a.artistFeeCents) // 33,333 → 33,33
    }

    @Test
    fun precoZeroNaoPode() {
        assertFailsWith<IllegalArgumentException> { FeeCalculator.calculate(0) }
    }
}
