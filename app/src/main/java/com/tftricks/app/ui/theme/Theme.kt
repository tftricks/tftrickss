package com.tftricks.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * TFTricks is dark-mode ONLY: pure black background, bright yellow accent.
 * There is intentionally no light color scheme and no dynamic color.
 */
private val TFTricksColorScheme = darkColorScheme(
    primary = BrandYellow,
    onPrimary = PureBlack,
    primaryContainer = BrandYellowDim,
    onPrimaryContainer = PureBlack,
    secondary = BrandYellowDim,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = TextSecondary,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = PureBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceCard,
    surfaceContainerLow = SurfaceDark,
    surfaceContainerHigh = SurfaceElevated,
    surfaceContainerHighest = SurfaceElevated,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = DangerRed,
    onError = PureBlack
)

@Composable
fun TFTricksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TFTricksColorScheme,
        typography = TFTricksTypography,
        shapes = TFTricksShapes,
        content = content
    )
}
