package br.com.ludius.core.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Mesmos casos de supabase/tests/database/01_regras.test.sql
class TaxIdValidatorTest {
    @Test fun cpfValido() = assertTrue(TaxIdValidator.isValid("529.982.247-25"))
    @Test fun cpfComDigitoErrado() = assertFalse(TaxIdValidator.isValid("52998224724"))
    @Test fun cpfRepetido() = assertFalse(TaxIdValidator.isValid("111.111.111-11"))
    @Test fun cnpjValido() = assertTrue(TaxIdValidator.isValid("11.222.333/0001-81"))
    @Test fun cnpjComDigitoErrado() = assertFalse(TaxIdValidator.isValid("11222333000180"))
    @Test fun tamanhoErrado() = assertFalse(TaxIdValidator.isValid("1234"))
}
