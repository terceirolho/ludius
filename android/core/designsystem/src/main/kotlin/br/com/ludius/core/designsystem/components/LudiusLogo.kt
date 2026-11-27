package br.com.ludius.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import br.com.ludius.core.designsystem.theme.LudiusTheme
import br.com.ludius.core.designsystem.theme.Outfit

/** Logotipo em texto: "Ludius" com o ponto final em verde. */
@Composable
fun LudiusLogo(modifier: Modifier = Modifier, size: TextUnit = 44.sp) {
    val c = LudiusTheme.colors
    Text(
        text = buildAnnotatedString {
            append("Ludius")
            withStyle(SpanStyle(color = c.lime)) { append(".") }
        },
        style = TextStyle(
            fontFamily = Outfit,
            fontWeight = FontWeight.Bold,
            fontSize = size,
            letterSpacing = (-0.03).em,
            color = c.ink,
        ),
        modifier = modifier,
    )
}
