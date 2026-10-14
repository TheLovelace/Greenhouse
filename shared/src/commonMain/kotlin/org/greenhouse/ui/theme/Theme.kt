package org.greenhouse.frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GreenhouseColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = Background,
    surface = Surface,
    error = RedPrimary,

    onPrimary = White,
    onSecondary = White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = White
)

@Composable
fun GreenhouseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GreenhouseColorScheme,
        typography = AppTypography(),
        content = content
    )
}
