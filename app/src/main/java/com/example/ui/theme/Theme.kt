package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BattleColorScheme = darkColorScheme(
    primary = Player1Color,
    onPrimary = Color.White,
    primaryContainer = Player1CardBg,
    onPrimaryContainer = Player1Accent,
    secondary = Player2Color,
    onSecondary = Color.Black,
    secondaryContainer = Player2CardBg,
    onSecondaryContainer = Player2Accent,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = ArenaDarkBg,
    onBackground = TextPrimary,
    surface = ArenaSurfaceBg,
    onSurface = TextPrimary,
    surfaceVariant = ArenaCardBg,
    onSurfaceVariant = TextSecondary,
    outline = ArenaBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BattleColorScheme,
        typography = Typography,
        content = content
    )
}
