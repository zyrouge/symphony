package io.github.zyrouge.symphony.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.zyrouge.symphony.services.SettingsKeys
import io.github.zyrouge.symphony.services.ThemeMode
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.helpers.ViewContext

private val LightBackgroundColor = ThemeColors.Neutral50
private val LightSurfaceColor = ThemeColors.Neutral100
private val LightSurfaceVariantColor = ThemeColors.Neutral200
private val PrimaryColor = ThemeColors.Blue500
private val DarkBackgroundColor = ThemeColors.Neutral900
private val DarkSurfaceColor = ThemeColors.Neutral900
private val DarkSurfaceVariantColor = ThemeColors.Neutral800
private val LightContrastColor = Color.White
private val BlackContrastColor = Color.Black

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = LightContrastColor,
    secondary = PrimaryColor,
    onSecondary = LightContrastColor,
    tertiary = PrimaryColor,
    onTertiary = LightContrastColor,
    background = LightBackgroundColor,
    onBackground = BlackContrastColor,
    surface = LightSurfaceColor,
    surfaceVariant = LightSurfaceVariantColor,
    onSurface = BlackContrastColor
)

private val DarkColorScheme = darkColorScheme(
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
    background = DarkBackgroundColor,
    onBackground = LightContrastColor,
    surface = DarkSurfaceColor,
    onSurface = LightContrastColor,
    surfaceVariant = DarkSurfaceVariantColor,
    onSurfaceVariant = LightContrastColor
)

private val BlackColorScheme = darkColorScheme(
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

private enum class ColorSchemeMode {
    LIGHT,
    DARK,
    BLACK
}

@Composable
fun SymphonyTheme(
    context: ViewContext,
    content: @Composable () -> Unit
) {
    var themeMode by remember { mutableStateOf(context.symphony.settings.getThemeMode()) }
    var useMaterialYou by remember { mutableStateOf(context.symphony.settings.getUseMaterialYou()) }

    EventerEffect(context.symphony.settings.onChange) {
        when (it) {
            SettingsKeys.themeMode -> themeMode = context.symphony.settings.getThemeMode()
            SettingsKeys.materialYou -> useMaterialYou =
                context.symphony.settings.getUseMaterialYou()
        }
    }

    val colorSchemeMode = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) ColorSchemeMode.DARK else ColorSchemeMode.LIGHT
        ThemeMode.LIGHT -> ColorSchemeMode.LIGHT
        ThemeMode.DARK -> ColorSchemeMode.DARK
        ThemeMode.BLACK -> ColorSchemeMode.BLACK
    }

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useMaterialYou) {
        val currentContext = LocalContext.current
        when (colorSchemeMode) {
            ColorSchemeMode.LIGHT -> dynamicLightColorScheme(currentContext)
            ColorSchemeMode.DARK -> dynamicDarkColorScheme(currentContext)
            ColorSchemeMode.BLACK -> dynamicDarkColorScheme(currentContext).copy(background = BlackContrastColor)
        }
    } else when (colorSchemeMode) {
        ColorSchemeMode.LIGHT -> LightColorScheme
        ColorSchemeMode.DARK -> DarkColorScheme
        ColorSchemeMode.BLACK -> BlackColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        SideEffect {
            activity.window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(activity.window, view)
                .isAppearanceLightStatusBars = colorSchemeMode == ColorSchemeMode.LIGHT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
