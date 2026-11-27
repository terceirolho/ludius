package br.com.ludius.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.ludius.core.designsystem.theme.LudiusTheme

/** Aviso provisório para as abas que ainda serão construídas na fase 1. */
@Composable
fun ComingSoon(title: String, contentPadding: PaddingValues) {
    val c = LudiusTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = c.ink)
        Text("Esta tela chega na fase 1.", style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
    }
}
