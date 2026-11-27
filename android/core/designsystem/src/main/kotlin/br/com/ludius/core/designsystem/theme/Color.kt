package br.com.ludius.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Todas as cores do Ludius. Os nomes descrevem o USO (fundo, texto, destaque),
 * não a cor, para poder trocar a paleta sem mexer nas telas.
 */
@Immutable
data class LudiusColors(
    val background: Color,     // fundo das telas
    val surface: Color,        // cartões e campos
    val ink: Color,            // texto principal
    val inkSecondary: Color,   // texto secundário
    val line: Color,           // bordas e divisórias
    val lime: Color,           // destaque: botão principal, aba ativa
    val onLime: Color,         // texto sobre o verde (sempre escuro)
    val limeSoft: Color,       // fundo de selo "Efetivado" e aba ativa
    val limeText: Color,       // texto verde legível sobre fundo claro
    val sky: Color,            // selo "Agendado"
    val skySoft: Color,
    val warn: Color,           // selo "Aguardando"
    val warnSoft: Color,
    val danger: Color,         // selo "Cancelado" e ações destrutivas
    val dangerSoft: Color,
    val isDark: Boolean,
)

val LightColors = LudiusColors(
    background = Color(0xFFF2F2F0),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF151514),
    inkSecondary = Color(0xFF62635C),
    line = Color(0xFFE3E3DD),
    lime = Color(0xFF8CCF00),
    onLime = Color(0xFF172200),
    limeSoft = Color(0xFFECF6D6),
    limeText = Color(0xFF4B7400),
    sky = Color(0xFF2459C9),
    skySoft = Color(0xFFE5ECFB),
    warn = Color(0xFF8F5A00),
    warnSoft = Color(0xFFFBF0D6),
    danger = Color(0xFFB93C12),
    dangerSoft = Color(0xFFFBE6DD),
    isDark = false,
)

val DarkColors = LudiusColors(
    background = Color(0xFF121311),
    surface = Color(0xFF1C1D1A),
    ink = Color(0xFFF2F2F0),
    inkSecondary = Color(0xFFA3A49C),
    line = Color(0xFF2E2F2A),
    lime = Color(0xFF97D81A),
    onLime = Color(0xFF142000),
    limeSoft = Color(0xFF243112),
    limeText = Color(0xFFA9E04A),
    sky = Color(0xFF8DB0F5),
    skySoft = Color(0xFF1C2740),
    warn = Color(0xFFE9B65A),
    warnSoft = Color(0xFF352812),
    danger = Color(0xFFF29A78),
    dangerSoft = Color(0xFF3A1E14),
    isDark = true,
)

/** Selo "Já contratei": sempre preto com texto branco, nos dois temas. */
val HiredBadgeBackground = Color(0xFF151514)
val HiredBadgeContent = Color(0xFFFFFFFF)

internal val LocalLudiusColors = staticCompositionLocalOf { LightColors }
