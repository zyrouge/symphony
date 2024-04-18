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
    private const val SurfaceBlendRatio = 0.02f
    private const val SurfaceVariantBlendRatio = 0.01f
    private const val BlackSurfaceBlendRatio = 0.05f
    private const val BlackSurfaceVariantBlendRatio = 0.06f
    private const val DarkOnPrimaryLightness = -0.3f
    private const val DarkOnSecondaryLightness = -0.4f
    private const val DarkOnTertiaryLightness = -0.5f
    private const val LightOnBackgroundLightness = -0.5f
    private const val LightOnSurfaceLightness = -0.5f
    private const val LightOnSurfaceVariantLightness = -0.45f
    private const val DarkToBlackBlendRatio = 0.4f

    fun createLightColorScheme(PrimaryColor: Color) = lightColorScheme(
        primary = PrimaryColor,
        onPrimary = LightContrastColor,
        primaryContainer = PrimaryColor,
        onPrimaryContainer = LightContrastColor,
        secondary = PrimaryColor,
        onSecondary = LightContrastColor,
        secondaryContainer = PrimaryColor,
        onSecondaryContainer = LightContrastColor,
        tertiary = PrimaryColor,
        onTertiary = LightContrastColor,
        tertiaryContainer = PrimaryColor,
        onTertiaryContainer = LightContrastColor,
        background = blendColors(LightBackgroundColor, PrimaryColor, BackgroundBlendRatio),
        onBackground = adjustLightness(PrimaryColor, LightOnBackgroundLightness),
        surface = blendColors(LightSurfaceColor, PrimaryColor, SurfaceBlendRatio),
        onSurface = adjustLightness(PrimaryColor, LightOnSurfaceLightness),
        surfaceVariant = blendColors(LightSurfaceVariantColor, PrimaryColor, SurfaceBlendRatio),
        onSurfaceVariant = adjustLightness(PrimaryColor, LightOnSurfaceVariantLightness),
    )

    fun createDarkColorScheme(PrimaryColor: Color) = darkColorScheme(
        primary = PrimaryColor,
        onPrimary = adjustLightness(PrimaryColor, DarkOnPrimaryLightness),
        primaryContainer = PrimaryColor,
        onPrimaryContainer = LightContrastColor,
        secondary = PrimaryColor,
        onSecondary = adjustLightness(PrimaryColor, DarkOnSecondaryLightness),
        secondaryContainer = PrimaryColor,
        onSecondaryContainer = LightContrastColor,
        tertiary = PrimaryColor,
        onTertiary = adjustLightness(PrimaryColor, DarkOnTertiaryLightness),
        tertiaryContainer = PrimaryColor,
        onTertiaryContainer = LightContrastColor,
        background = blendColors(DarkBackgroundColor, PrimaryColor, BackgroundBlendRatio),
        onBackground = LightContrastColor,
        surface = blendColors(DarkSurfaceColor, PrimaryColor, SurfaceBlendRatio),
        onSurface = LightContrastColor,
        surfaceVariant = blendColors(
            DarkSurfaceVariantColor,
            PrimaryColor,
            SurfaceVariantBlendRatio
        ),
        onSurfaceVariant = LightContrastColor,
    )

    fun createBlackColorScheme(PrimaryColor: Color) = darkColorScheme(
        primary = PrimaryColor,
        onPrimary = adjustLightness(PrimaryColor, DarkOnPrimaryLightness),
        primaryContainer = PrimaryColor,
        onPrimaryContainer = LightContrastColor,
        secondary = PrimaryColor,
        onSecondary = adjustLightness(PrimaryColor, DarkOnSecondaryLightness),
        secondaryContainer = PrimaryColor,
        onSecondaryContainer = LightContrastColor,
        tertiary = PrimaryColor,
        onTertiary = adjustLightness(PrimaryColor, DarkOnTertiaryLightness),
        tertiaryContainer = PrimaryColor,
        onTertiaryContainer = LightContrastColor,
        background = BlackContrastColor,
        onBackground = LightContrastColor,
        surface = blendColors(BlackContrastColor, PrimaryColor, BlackSurfaceBlendRatio),
        onSurface = LightContrastColor,
        surfaceVariant = blendColors(
            BlackContrastColor,
            PrimaryColor,
            BlackSurfaceVariantBlendRatio
        ),
        onSurfaceVariant = LightContrastColor,
    )

    fun toBlackColorScheme(colorScheme: ColorScheme) = colorScheme.copy(
        primaryContainer = convertDarkToBlack(colorScheme.primaryContainer),
        secondaryContainer = convertDarkToBlack(colorScheme.secondaryContainer),
        tertiaryContainer = convertDarkToBlack(colorScheme.tertiaryContainer),
        background = BlackContrastColor,
        surface = convertDarkToBlack(colorScheme.surface),
        surfaceContainerLowest = convertDarkToBlack(colorScheme.surfaceContainerLowest),
        surfaceContainerLow = convertDarkToBlack(colorScheme.surfaceContainerLow),
        surfaceContainer = convertDarkToBlack(colorScheme.surfaceContainer),
        surfaceContainerHigh = convertDarkToBlack(colorScheme.surfaceContainerHigh),
        surfaceContainerHighest = convertDarkToBlack(colorScheme.surfaceContainerHighest),
        surfaceVariant = convertDarkToBlack(colorScheme.surfaceVariant),
        surfaceTint = convertDarkToBlack(colorScheme.surfaceTint),
    )

    private fun convertDarkToBlack(color: Color): Color {
        val argb = ColorUtils.blendARGB(
            BlackContrastColor.toArgb(),
            color.toArgb(),
            DarkToBlackBlendRatio,
        )
        return Color(argb)
    }

    private fun blendColors(color1: Color, color2: Color, ratio: Float) =
        Color(ColorUtils.blendARGB(color1.toArgb(), color2.toArgb(), ratio))

    private fun adjustLightness(color: Color, threshold: Float): Color {
        val hsl = convertColorToHSL(color)
        hsl[2] = hsl[2] + threshold
        return convertHSLToColor(hsl)
    }

    private fun convertColorToHSL(color: Color): FloatArray {
        val out = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), out)
        return out
    }

    private fun convertHSLToColor(hsl: FloatArray) =
        Color(ColorUtils.HSLToColor(hsl))
}
