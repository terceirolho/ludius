package br.com.ludius.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.ludius.core.designsystem.theme.LudiusTheme
import br.com.ludius.core.designsystem.theme.PillShape

/** Estilos de botão do app. */
enum class ButtonVariant {
    Primary, // verde com texto preto: a ação principal da tela
    Dark,    // preto com texto claro: ação forte secundária
    Ghost,   // branco com borda: ação alternativa
    Danger,  // texto vermelho: cancelar, excluir
}

/**
 * Botão único do Ludius, em formato de pílula.
 * [small] = versão compacta (38 dp) usada dentro de cartões.
 */
@Composable
fun LudiusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    small: Boolean = false,
) {
    val c = LudiusTheme.colors
    val (container, content, border) = when (variant) {
        // Regra da identidade visual: texto preto sobre o verde.
        ButtonVariant.Primary -> Triple(c.lime, c.onLime, null)
        ButtonVariant.Dark -> Triple(c.ink, c.background, null)
        ButtonVariant.Ghost -> Triple(c.surface, c.ink, BorderStroke(1.5.dp, c.line))
        ButtonVariant.Danger -> Triple(c.surface, c.danger, BorderStroke(1.5.dp, c.dangerSoft))
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container.copy(alpha = 0.45f),
            disabledContentColor = content.copy(alpha = 0.45f),
        ),
        contentPadding = PaddingValues(horizontal = if (small) 16.dp else 24.dp),
        modifier = if (small) {
            modifier.height(38.dp)
        } else {
            modifier.fillMaxWidth().height(52.dp)
        },
    ) {
        Text(
            text = text,
            style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
        )
    }
}
