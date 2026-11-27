package br.com.ludius.core.domain

/**
 * Valida CPF (11 dígitos) e CNPJ (14 dígitos) pelo dígito verificador.
 * Aceita o número com ou sem pontuação. Mesma regra de `private.is_valid_tax_id` no banco.
 */
object TaxIdValidator {

    /** Remove tudo que não for número: "529.982.247-25" vira "52998224725". */
    fun onlyDigits(input: String): String = input.filter { it.isDigit() }

    fun isValid(input: String): Boolean {
        val doc = onlyDigits(input)
        return when (doc.length) {
            11 -> isValidCpf(doc)
            14 -> isValidCnpj(doc)
            else -> false
        }
    }

    private fun isValidCpf(cpf: String): Boolean {
        // Sequências repetidas (111.111.111-11) passam na conta, mas não existem.
        if (cpf.all { it == cpf[0] }) return false
        val d = cpf.map { it.digitToInt() }
        for (pos in 9..10) {
            // Pesos decrescentes: 10..2 para o 1º dígito, 11..2 para o 2º.
            val sum = (0 until pos).sumOf { i -> d[i] * (pos + 1 - i) }
            val dv = (sum * 10 % 11).let { if (it == 10) 0 else it }
            if (dv != d[pos]) return false
        }
        return true
    }

    private fun isValidCnpj(cnpj: String): Boolean {
        if (cnpj.all { it == cnpj[0] }) return false
        val d = cnpj.map { it.digitToInt() }
        val weights1 = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val weights2 = listOf(6) + weights1
        for ((pos, weights) in listOf(12 to weights1, 13 to weights2)) {
            val rest = (0 until pos).sumOf { i -> d[i] * weights[i] } % 11
            val dv = if (rest < 2) 0 else 11 - rest
            if (dv != d[pos]) return false
        }
        return true
    }
}
