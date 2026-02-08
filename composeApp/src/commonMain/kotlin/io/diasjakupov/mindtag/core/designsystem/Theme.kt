package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MindTagDarkColorScheme = darkColorScheme(
    primary = MindTagColors.Primary,
    onPrimary = Color.White,
    primaryContainer = MindTagColors.InactiveDot,
    onPrimaryContainer = Color.White,
    secondary = MindTagColors.TextSecondary,
    onSecondary = MindTagColors.BackgroundDark,
    background = MindTagColors.BackgroundDark,
    onBackground = Color.White,
    surface = MindTagColors.SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = MindTagColors.CardDark,
    onSurfaceVariant = MindTagColors.TextSecondary,
    outline = MindTagColors.InactiveDot,
    outlineVariant = MindTagColors.BorderSubtle,
    error = MindTagColors.Error,
    onError = Color.White,
    tertiary = MindTagColors.Warning,
    onTertiary = Color.White,
)

@Composable
fun MindTagTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MindTagDarkColorScheme,
        typography = MindTagTypography(),
        content = content,
    )
}
