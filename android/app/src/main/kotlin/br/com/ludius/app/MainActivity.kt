package br.com.ludius.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.ludius.app.navigation.LudiusNavHost
import br.com.ludius.core.designsystem.theme.LudiusTheme

/** Única Activity do app: tudo o mais são telas Compose dentro dela. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // desenha atrás das barras do sistema, como apps modernos
        setContent {
            LudiusTheme {
                LudiusNavHost()
            }
        }
    }
}
