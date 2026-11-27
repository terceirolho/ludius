package br.com.ludius.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import br.com.ludius.core.designsystem.components.LudiusIcons

/** Tipo de conta que define quais abas aparecem. */
enum class Role { CONTRATANTE, ARTISTA }

/** Uma aba da barra inferior. [route] é o endereço interno da tela. */
data class Tab(val route: String, val label: String, val icon: ImageVector)

/**
 * Abas de cada tipo de conta, na ordem do protótipo (versão 5).
 * Quem contrata: Início, Buscar, Pedidos, Perfil.
 * Artista: Pedidos, Minha vitrine, Extrato, Perfil (artista não contrata, então não tem Buscar).
 */
fun tabsFor(role: Role): List<Tab> = when (role) {
    Role.CONTRATANTE -> listOf(
        Tab("inicio", "Início", LudiusIcons.Home),
        Tab("buscar", "Buscar", LudiusIcons.Search),
        Tab("pedidos", "Pedidos", LudiusIcons.Receipt),
        Tab("perfil", "Perfil", LudiusIcons.User),
    )
    Role.ARTISTA -> listOf(
        Tab("pedidos", "Pedidos", LudiusIcons.Receipt),
        Tab("vitrine", "Minha vitrine", LudiusIcons.Store),
        Tab("extrato", "Extrato", LudiusIcons.Wallet),
        Tab("perfil", "Perfil", LudiusIcons.User),
    )
}
