package io.github.zyrouge.symphony.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.zyrouge.symphony.services.ThemeMode
import io.github.zyrouge.symphony.ui.helpers.ViewContext

private val PrimaryColor = ThemeColors.Emerald400

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryColor,
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
    secondary = PrimaryColor,
    secondaryContainer = PrimaryColor,
    onSecondary = Color.White,
    onSecondaryContainer = Color.White,
    tertiary = PrimaryColor,
    tertiaryContainer = PrimaryColor,
    onTertiary = Color.White,
    onTertiaryContainer = Color.White,
    background = ThemeColors.Neutral900,
    onBackground = Color.White,
    surface = ThemeColors.Neutral800,
    surfaceVariant = ThemeColors.Neutral800,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

private val BlackColorScheme = darkColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryColor,
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
    secondary = PrimaryColor,
    secondaryContainer = PrimaryColor,
    onSecondary = Color.White,
    onSecondaryContainer = Color.White,
    tertiary = PrimaryColor,
    tertiaryContainer = PrimaryColor,
    onTertiary = Color.White,
    onTertiaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    surfaceVariant = Color.Black,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    secondary = PrimaryColor,
    onSecondary = Color.White,
    tertiary = PrimaryColor,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = ThemeColors.Neutral200,
    surfaceVariant = ThemeColors.Neutral200,
    onSurface = Color.Black
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
    val themeMode = context.symphony.settings.getThemeMode()
    val useMaterialYou =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.symphony.settings.getUseMaterialYou()

    val colorSchemeMode = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) ColorSchemeMode.DARK else ColorSchemeMode.LIGHT
        ThemeMode.LIGHT -> ColorSchemeMode.LIGHT
        ThemeMode.DARK -> ColorSchemeMode.DARK
        ThemeMode.BLACK -> ColorSchemeMode.BLACK
    }

    val colorScheme = if (useMaterialYou) {
        val currentContext = LocalContext.current
        when (colorSchemeMode) {
            ColorSchemeMode.LIGHT -> dynamicLightColorScheme(currentContext)
            ColorSchemeMode.DARK -> dynamicDarkColorScheme(currentContext)
            ColorSchemeMode.BLACK -> dynamicDarkColorScheme(currentContext).copy(background = Color.Black)
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
            activity.window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(activity.window, view)
                .isAppearanceLightStatusBars = colorSchemeMode != ColorSchemeMode.LIGHT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
