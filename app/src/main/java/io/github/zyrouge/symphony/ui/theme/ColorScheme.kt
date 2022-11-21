package io.github.zyrouge.symphony.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

object ThemeColorSchemes {
    private val LightBackgroundColor = ThemeColors.Neutral50
    private val LightSurfaceColor = ThemeColors.Neutral100
    private val LightSurfaceVariantColor = ThemeColors.Neutral200
    private val DarkBackgroundColor = ThemeColors.Neutral900
    private val DarkSurfaceColor = ThemeColors.Neutral900
    private val DarkSurfaceVariantColor = ThemeColors.Neutral800
    private val LightContrastColor = Color.White
    private val BlackContrastColor = Color.Black

    private const val BackgroundBlendRatio = 0.03f

    fun createLightColorScheme(PrimaryColor: Color) = lightColorScheme(
        primary = PrimaryColor,
        onPrimary = LightContrastColor,
        secondary = PrimaryColor,
        onSecondary = LightContrastColor,
        tertiary = PrimaryColor,
        onTertiary = LightContrastColor,
        background = blendColors(LightBackgroundColor, PrimaryColor, BackgroundBlendRatio),
        onBackground = BlackContrastColor,
        surface = LightSurfaceColor,
        surfaceVariant = LightSurfaceVariantColor,
        onSurface = BlackContrastColor,
    )

    fun createDarkColorScheme(PrimaryColor: Color) = darkColorScheme(
        primary = PrimaryColor,
        primaryContainer = PrimaryColor,
        onPrimary = LightContrastColor,
        onPrimaryContainer = LightContrastColor,
        secondary = PrimaryColor,
        secondaryContainer = PrimaryColor,
        onSecondary = LightContrastColor,
        onSecondaryContainer = LightContrastColor,
        tertiary = PrimaryColor,
        tertiaryContainer = PrimaryColor,
        onTertiary = LightContrastColor,
        onTertiaryContainer = LightContrastColor,
        background = blendColors(DarkBackgroundColor, PrimaryColor, BackgroundBlendRatio),
        onBackground = LightContrastColor,
        surface = DarkSurfaceColor,
        onSurface = LightContrastColor,
        surfaceVariant = DarkSurfaceVariantColor,
        onSurfaceVariant = LightContrastColor
    )

    fun createBlackColorScheme(PrimaryColor: Color) = darkColorScheme(
        primary = PrimaryColor,
        primaryContainer = PrimaryColor,
        onPrimary = LightContrastColor,
        onPrimaryContainer = LightContrastColor,
        secondary = PrimaryColor,
        secondaryContainer = PrimaryColor,
        onSecondary = LightContrastColor,
        onSecondaryContainer = LightContrastColor,
        tertiary = PrimaryColor,
        tertiaryContainer = PrimaryColor,
        onTertiary = LightContrastColor,
        onTertiaryContainer = LightContrastColor,
        background = BlackContrastColor,
        onBackground = LightContrastColor,
        surface = DarkBackgroundColor,
        surfaceVariant = DarkSurfaceColor,
        onSurface = LightContrastColor,
        onSurfaceVariant = LightContrastColor
    )

    fun toBlackColorScheme(colorScheme: ColorScheme) =
        colorScheme.copy(background = BlackContrastColor)

    private fun blendColors(color1: Color, color2: Color, ratio: Float) =
        Color(ColorUtils.blendARGB(color1.toArgb(), color2.toArgb(), ratio))
}