package br.com.ludius.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.ludius.app.navigation.Role
import br.com.ludius.app.navigation.tabsFor
import br.com.ludius.core.designsystem.components.LudiusIcons
import br.com.ludius.core.designsystem.theme.LudiusTheme

/**
 * Estrutura da área logada: cabeçalho com Chat e Notificações + barra de abas embaixo.
 * As abas mudam conforme o tipo de conta (ver Destinations.kt).
 */
@Composable
fun MainScaffold(role: Role) {
    val c = LudiusTheme.colors
    val tabs = tabsFor(role)
    var current by rememberSaveable { mutableStateOf(tabs.first().route) }
    val currentTab = tabs.first { it.route == current }

    Scaffold(
        containerColor = c.background,
        topBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding() // não fica embaixo do relógio e da bateria
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(currentTab.label, style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.weight(1f))
                IconButton(onClick = { /* fase 1: abre o Chat */ }) {
                    Icon(LudiusIcons.Chat, contentDescription = "Chat", tint = c.ink)
                }
                IconButton(onClick = { /* fase 1: abre as Notificações */ }) {
                    Icon(LudiusIcons.Bell, contentDescription = "Notificações", tint = c.ink)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = c.surface) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = tab.route == current,
                        onClick = { current = tab.route },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = c.limeText,
                            selectedTextColor = c.ink,
                            indicatorColor = c.limeSoft,
                            unselectedIconColor = c.inkSecondary,
                            unselectedTextColor = c.inkSecondary,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        // Fase 0: a primeira aba mostra o catálogo de componentes; as outras, um aviso.
        if (current == tabs.first().route) {
            ComponentCatalog(contentPadding = padding)
        } else {
            ComingSoon(title = currentTab.label, contentPadding = padding)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScaffoldPreview() {
    LudiusTheme { MainScaffold(role = Role.CONTRATANTE) }
}
