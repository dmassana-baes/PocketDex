package com.example.pocketdex.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pocketdex.core.designsystem.R

private val Condensed = FontFamily(
    Font(R.font.roboto_condensed_regular, FontWeight.Normal),
    Font(R.font.roboto_condensed_medium, FontWeight.Medium),
)

private fun style(size: Int, line: Int, tracking: Float = 0f, medium: Boolean = false) = TextStyle(
    fontFamily = Condensed,
    fontWeight = if (medium) FontWeight.Medium else FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

val PocketDexTypography = Typography(
    displayLarge = style(57, 64), displayMedium = style(45, 52), displaySmall = style(36, 44),
    headlineLarge = style(32, 40), headlineMedium = style(28, 36), headlineSmall = style(24, 32),
    titleLarge = style(22, 28), titleMedium = style(16, 24, .15f, true),
    titleSmall = style(14, 20, .1f, true),
    bodyLarge = style(16, 24, .5f), bodyMedium = style(14, 20, .25f),
    bodySmall = style(12, 16, .4f),
    labelLarge = style(14, 20, .1f, true), labelMedium = style(12, 16, .5f, true),
    labelSmall = style(12, 16, .5f),
)

private val LightColors = with(PocketDexColors) {
    lightColorScheme(
        primary = Yellow, onPrimary = Gray500, primaryContainer = Gray100,
        onPrimaryContainer = Gray500, inversePrimary = Yellow,
        secondary = Gray400, onSecondary = White, secondaryContainer = Gray100,
        onSecondaryContainer = Gray500, tertiary = Gray400, onTertiary = White,
        tertiaryContainer = Gray100, onTertiaryContainer = Gray500,
        background = White, onBackground = Gray500, surface = White, onSurface = Gray500,
        surfaceVariant = Gray100, onSurfaceVariant = Gray500, surfaceTint = Yellow,
        inverseSurface = Gray500, inverseOnSurface = White,
        error = Gray500, onError = White, errorContainer = Gray100, onErrorContainer = Gray500,
        outline = Gray400, outlineVariant = Gray200, scrim = Gray500,
        surfaceBright = White, surfaceDim = Gray200, surfaceContainerLowest = White,
        surfaceContainerLow = Gray100, surfaceContainer = Gray100,
        surfaceContainerHigh = Gray200, surfaceContainerHighest = Gray200,
    )
}

/** Fixed light theme; neither system night mode nor dynamic colors alter the visual contract. */
@Composable
fun PocketDexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = PocketDexTypography,
        shapes = Shapes(medium = PocketDexShapes.Container, small = PocketDexShapes.Chip),
        content = content,
    )
}
