package br.com.ludius.core.designsystem.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Ícones de traço simples do Ludius, copiados do protótipo (grade 24x24, traço 1,8).
 * Usar com `Icon(LudiusIcons.Home, contentDescription = "Início")`; a cor vem do `tint`.
 */
object LudiusIcons {
    val Home by lazy { icon("Home", "M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z") }
    val Search by lazy { icon("Search", "M4,11a7,7 0 1,0 14,0a7,7 0 1,0 -14,0", "m20 20-3.5-3.5") }
    val Receipt by lazy { icon("Receipt", "M6 3h12v18l-3-2-3 2-3-2-3 2z", "M9 8h6M9 12h6") }
    val User by lazy { icon("User", "M8,8a4,4 0 1,0 8,0a4,4 0 1,0 -8,0", "M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6") }
    val Chat by lazy { icon("Chat", "M4 5h16v11H9l-5 4z") }
    val Bell by lazy { icon("Bell", "M6 16V11a6 6 0 0 1 12 0v5l2 2H4z", "M10 20a2 2 0 0 0 4 0") }
    val Store by lazy { icon("Store", "M4 9h16l-1-5H5zM5 9v11h14V9", "M10 20v-5h4v5") }
    val Wallet by lazy {
        icon(
            "Wallet",
            "M6,6h12a3,3 0 0 1 3,3v8a3,3 0 0 1 -3,3h-12a3,3 0 0 1 -3,-3v-8a3,3 0 0 1 3,-3z",
            "M16 13h2M3 10h18",
        )
    }
    val Check by lazy { icon("Check", "m5 12.5 4.5 4.5L19 7.5") }

    // Monta um ícone a partir dos caminhos SVG (atributo "d" de cada <path>).
    private fun icon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
