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

@Composable
fun SymphonyTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        SideEffect {
            activity.window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(activity.window, view)
                .isAppearanceLightStatusBars = isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
