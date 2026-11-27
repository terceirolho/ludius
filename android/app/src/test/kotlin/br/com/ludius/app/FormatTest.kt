package br.com.ludius.app

import br.com.ludius.app.ui.brl
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun formataReais() {
        // NumberFormat usa espaço não separável entre "R$" e o número.
        assertEquals("R$ 1.050,00", brl(105_000))
    }
}
