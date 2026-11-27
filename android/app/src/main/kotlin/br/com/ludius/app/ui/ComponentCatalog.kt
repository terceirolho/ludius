package br.com.ludius.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.ludius.core.designsystem.components.BadgeTone
import br.com.ludius.core.designsystem.components.ButtonVariant
import br.com.ludius.core.designsystem.components.HiredBadge
import br.com.ludius.core.designsystem.components.LudiusButton
import br.com.ludius.core.designsystem.components.LudiusTextField
import br.com.ludius.core.designsystem.components.StatusBadge
import br.com.ludius.core.domain.FeeCalculator
import br.com.ludius.core.domain.OrderStatus
import br.com.ludius.core.domain.TaxIdValidator
import br.com.ludius.core.designsystem.theme.LudiusTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Catálogo de componentes (só na fase 0): mostra no celular cada peça do design system
 * e testa as regras do domínio ao vivo (CPF e comissão). Sai do app na fase 1.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentCatalog(contentPadding: PaddingValues) {
    val c = LudiusTheme.colors
    var cpf by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("1000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Section("Botões") {
            LudiusButton("Contratar", onClick = {})
            LudiusButton("Ver pedido", onClick = {}, variant = ButtonVariant.Dark)
            LudiusButton("Conversar", onClick = {}, variant = ButtonVariant.Ghost)
            LudiusButton("Cancelar pedido", onClick = {}, variant = ButtonVariant.Danger)
            LudiusButton("Publicar", onClick = {}, small = true)
        }

        Section("Selos") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderStatus.entries
                    .filter { it != OrderStatus.AWAITING_PAYMENT }
                    .forEach { StatusBadge(it.label, toneFor(it)) }
                HiredBadge()
                HiredBadge("Já me contratou")
            }
        }

        Section("Campos") {
            val valid = TaxIdValidator.isValid(cpf)
            LudiusTextField(
                label = "CPF ou CNPJ",
                value = cpf,
                onValueChange = { cpf = it },
                placeholder = "000.000.000-00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                hint = if (valid) "Documento válido" else "Digite para testar a validação",
                error = if (cpf.length >= 11 && !valid) "CPF ou CNPJ inválido" else null,
            )
            LudiusTextField(
                label = "Bio",
                value = bio,
                onValueChange = { bio = it },
                singleLine = false,
                maxLength = 240,
                placeholder = "Conte quem você é e o que toca",
            )
        }

        Section("Comissão (10% artista + 5% contratante)") {
            LudiusTextField(
                label = "Preço do serviço (R$)",
                value = price,
                onValueChange = { novo -> price = novo.filter { it.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            val reais = price.toLongOrNull()
            if (reais != null && reais > 0) {
                val a = FeeCalculator.calculate(reais * 100)
                Text("Contratante paga: ${brl(a.totalCents)}", color = c.ink)
                Text("Artista recebe: ${brl(a.artistPayoutCents)} (antes das taxas do Stripe)", color = c.ink)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val c = LudiusTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
        content()
    }
}

/** Liga cada status do pedido à cor do selo. */
fun toneFor(status: OrderStatus): BadgeTone = when (status) {
    OrderStatus.AWAITING_PAYMENT, OrderStatus.AWAITING -> BadgeTone.Waiting
    OrderStatus.SCHEDULED -> BadgeTone.Scheduled
    OrderStatus.COMPLETED -> BadgeTone.Done
    OrderStatus.CANCELLED, OrderStatus.DECLINED -> BadgeTone.Cancelled
    OrderStatus.EXPIRED -> BadgeTone.Neutral
}

/** Formata centavos em reais: 105000 → "R$ 1.050,00". */
fun brl(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(cents / 100.0)
