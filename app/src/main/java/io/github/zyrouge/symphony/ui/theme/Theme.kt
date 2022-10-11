package io.github.zyrouge.symphony.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme(
    primary = ThemeColors.Orange400,
    primaryContainer = ThemeColors.Orange400,
    onPrimary = ThemeColors.White,
    onPrimaryContainer = ThemeColors.White,
    secondary = ThemeColors.Orange400,
    secondaryContainer = ThemeColors.Orange400,
    onSecondary = ThemeColors.White,
    onSecondaryContainer = ThemeColors.White,
    tertiary = ThemeColors.Orange400,
    tertiaryContainer = ThemeColors.Orange400,
    onTertiary = ThemeColors.White,
    onTertiaryContainer = ThemeColors.White,
    background = ThemeColors.Neutral900,
    onBackground = ThemeColors.White,
    surface = ThemeColors.Neutral800,
    surfaceVariant = ThemeColors.Neutral800,
    onSurface = ThemeColors.White,
    onSurfaceVariant = ThemeColors.White
)

private val LightColorScheme = lightColorScheme(
    primary = ThemeColors.Orange400,
    onPrimary = ThemeColors.White,
    secondary = ThemeColors.Orange400,
    onSecondary = ThemeColors.White,
    tertiary = ThemeColors.Orange400,
    onTertiary = ThemeColors.White,
    background = ThemeColors.White,
    onBackground = ThemeColors.Black,
    surface = ThemeColors.Neutral200,
    surfaceVariant = ThemeColors.Neutral200,
    onSurface = ThemeColors.Black

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SymphonyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
