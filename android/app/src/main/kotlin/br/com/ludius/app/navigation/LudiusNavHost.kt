package br.com.ludius.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.ludius.app.ui.MainScaffold
import br.com.ludius.app.ui.WelcomeScreen

/**
 * Mapa de navegação do app.
 * Fase 0: boas-vindas → área principal. Na fase 1 entram cadastro, login e as telas de cada aba.
 */
@Composable
fun LudiusNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "boas-vindas") {
        composable("boas-vindas") {
            WelcomeScreen(
                onChooseRole = { role ->
                    nav.navigate("principal/${role.name}") {
                        // Tira as boas-vindas da pilha: o "voltar" não retorna para ela.
                        popUpTo("boas-vindas") { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = "principal/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType }),
        ) { entry ->
            val role = Role.valueOf(entry.arguments?.getString("role") ?: Role.CONTRATANTE.name)
            MainScaffold(role = role)
        }
    }
}
