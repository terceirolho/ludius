package br.com.ludius.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.ludius.app.R
import br.com.ludius.app.navigation.Role
import br.com.ludius.core.designsystem.components.ButtonVariant
import br.com.ludius.core.designsystem.components.LudiusButton
import br.com.ludius.core.designsystem.components.LudiusLogo
import br.com.ludius.core.designsystem.theme.LudiusTheme

/**
 * Tela de boas-vindas, com a ilustração e o texto aprovados no protótipo (versão 5).
 * Fase 0: os botões só abrem a prévia das abas. Na fase 1 levam ao cadastro e ao login.
 */
@Composable
fun WelcomeScreen(onChooseRole: (Role) -> Unit) {
    val c = LudiusTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        LudiusLogo()
        Text(
            text = "A ponte que permite que Artistas independentes trabalhem com o que amam, " +
                "atendendo diferentes tipos de eventos.",
            style = MaterialTheme.typography.headlineMedium,
            color = c.ink,
        )
        Image(
            painter = painterResource(R.drawable.welcome_hero),
            contentDescription = "Artistas tocando tuba, saxofone, sanfona, DJ, violão, bumbo e pandeiro",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1000f / 563f)
                .clip(MaterialTheme.shapes.large),
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LudiusButton("Quero contratar", onClick = { onChooseRole(Role.CONTRATANTE) })
            LudiusButton("Sou artista", onClick = { onChooseRole(Role.ARTISTA) }, variant = ButtonVariant.Ghost)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePreview() {
    LudiusTheme { WelcomeScreen(onChooseRole = {}) }
}
