package tech.pd7.wellbot.robots_area.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PixelGreen = Color(0xFF57F287)
private val DeepGreen = Color(0xFF07110B)
private val PanelGreen = Color(0xFF102619)
private val SoftGreen = Color(0xFFB8F7CB)

private val WellbotColors = darkColorScheme(
    primary = PixelGreen,
    onPrimary = DeepGreen,
    primaryContainer = PanelGreen,
    onPrimaryContainer = SoftGreen,
    background = DeepGreen,
    onBackground = SoftGreen,
    surface = Color(0xFF0B1B11),
    onSurface = SoftGreen,
    error = Color(0xFFFF6B6B),
)

@Composable
fun WellbotTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WellbotColors, content = content)
}
