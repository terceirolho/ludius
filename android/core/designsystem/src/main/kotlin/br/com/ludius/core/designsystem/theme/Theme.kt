package br.com.ludius.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Tema do Ludius. Toda tela fica dentro de `LudiusTheme { ... }`.
 * Segue o modo claro/escuro do celular.
 */
@Composable
fun LudiusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    // Também preenche o esquema do Material 3, para componentes prontos
    // (diálogos, menus) já nascerem com as cores do Ludius.
    val material = if (darkTheme) {
        darkColorScheme(
            primary = colors.lime, onPrimary = colors.onLime,
            background = colors.background, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink,
            onSurfaceVariant = colors.inkSecondary, outline = colors.line,
            error = colors.danger,
        )
    } else {
        lightColorScheme(
            primary = colors.lime, onPrimary = colors.onLime,
            background = colors.background, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink,
            onSurfaceVariant = colors.inkSecondary, outline = colors.line,
            error = colors.danger,
        )
    }

    CompositionLocalProvider(LocalLudiusColors provides colors) {
        MaterialTheme(
            colorScheme = material,
            typography = LudiusTypography,
            shapes = LudiusShapes,
            content = content,
        )
    }
}

/** Atalho para as cores dentro das telas: `LudiusTheme.colors.lime`. */
object LudiusTheme {
    val colors: LudiusColors
        @Composable @ReadOnlyComposable
        get() = LocalLudiusColors.current
}
