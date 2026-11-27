package br.com.ludius.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Cantos arredondados: pequeno (12), médio (16, campos) e grande (22, cartões). */
val LudiusShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

/** Formato de pílula, usado em botões e selos. */
val PillShape = RoundedCornerShape(percent = 50)
