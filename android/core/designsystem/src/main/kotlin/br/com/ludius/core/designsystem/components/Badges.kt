package br.com.ludius.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.ludius.core.designsystem.theme.HiredBadgeBackground
import br.com.ludius.core.designsystem.theme.HiredBadgeContent
import br.com.ludius.core.designsystem.theme.LudiusTheme
import br.com.ludius.core.designsystem.theme.PillShape

/** Cor do selo, pelo significado. */
enum class BadgeTone { Waiting, Scheduled, Done, Cancelled, Neutral }

/**
 * Selo de status do pedido ("Aguardando", "Agendado", "Efetivado", "Cancelado").
 * O texto vem de fora (ex.: `OrderStatus.label`) para o design system não depender das regras.
 */
@Composable
fun StatusBadge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val c = LudiusTheme.colors
    val (bg, fg) = when (tone) {
        BadgeTone.Waiting -> c.warnSoft to c.warn
        BadgeTone.Scheduled -> c.skySoft to c.sky
        BadgeTone.Done -> c.limeSoft to c.limeText
        BadgeTone.Cancelled -> c.dangerSoft to c.danger
        BadgeTone.Neutral -> c.background to c.inkSecondary
    }
    Pill(text = text, background = bg, content = fg, modifier = modifier)
}

/**
 * Selo de relação entre as partes: "Já contratei" (visão do contratante)
 * ou "Já me contratou" (visão do artista). Sempre preto com texto e ícone brancos.
 */
@Composable
fun HiredBadge(text: String = "Já contratei", modifier: Modifier = Modifier) {
    Pill(text = text, background = HiredBadgeBackground, content = HiredBadgeContent, showCheck = true, modifier = modifier)
}

@Composable
private fun Pill(
    text: String,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
    showCheck: Boolean = false,
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .background(background, PillShape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (showCheck) {
            Icon(LudiusIcons.Check, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = content, maxLines = 1)
    }
}
