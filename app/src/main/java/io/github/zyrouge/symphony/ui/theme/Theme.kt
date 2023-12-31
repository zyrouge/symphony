package io.github.zyrouge.symphony.ui.theme

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.ViewContext

enum class ThemeMode {
    SYSTEM,
    SYSTEM_BLACK,
    LIGHT,
    DARK,
    BLACK,
}

enum class ColorSchemeMode {
    LIGHT,
    DARK,
    BLACK
}

@Composable
fun SymphonyTheme(
    context: ViewContext,
    content: @Composable () -> Unit,
) {
    val themeMode by context.symphony.settings.themeMode.collectAsState()
    val useMaterialYou by context.symphony.settings.useMaterialYou.collectAsState()
    val primaryColorName by context.symphony.settings.primaryColor.collectAsState()
    val fontName by context.symphony.settings.fontFamily.collectAsState()
    val fontScale by context.symphony.settings.fontScale.collectAsState()
    val contentScale by context.symphony.settings.contentScale.collectAsState()

    val colorSchemeMode = themeMode.toColorSchemeMode(isSystemInDarkTheme())
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useMaterialYou) {
        val currentContext = LocalContext.current
        when (colorSchemeMode) {
            ColorSchemeMode.LIGHT -> dynamicLightColorScheme(currentContext)
            ColorSchemeMode.DARK -> dynamicDarkColorScheme(currentContext)
            ColorSchemeMode.BLACK -> ThemeColorSchemes.toBlackColorScheme(
                dynamicDarkColorScheme(currentContext)
            )
        }
    } else {
        val primaryColor = ThemeColors.resolvePrimaryColor(primaryColorName)
        when (colorSchemeMode) {
            ColorSchemeMode.LIGHT -> ThemeColorSchemes.createLightColorScheme(primaryColor)
            ColorSchemeMode.DARK -> ThemeColorSchemes.createDarkColorScheme(primaryColor)
            ColorSchemeMode.BLACK -> ThemeColorSchemes.createBlackColorScheme(primaryColor)
        }
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

    val textDirection = when (context.symphony.t.LocaleDirection) {
        "ltr" -> TextDirection.Ltr
        "rtl" -> TextDirection.Rtl
        else -> TextDirection.Unspecified
    }
    val typography = SymphonyTypography.toTypography(
        SymphonyTypography.resolveFont(fontName),
        textDirection,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    LocalDensity.current.density * contentScale,
                    LocalDensity.current.fontScale * fontScale,
                )
            ) {
                content()
            }
        }
    )
}

fun ThemeMode.toColorSchemeMode(symphony: Symphony): ColorSchemeMode {
    val isSystemInDarkTheme = symphony.applicationContext.resources.configuration.uiMode.let {
        (it and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    return toColorSchemeMode(isSystemInDarkTheme)
}

fun ThemeMode.toColorSchemeMode(isSystemInDarkTheme: Boolean) = when (this) {
    ThemeMode.SYSTEM -> if (isSystemInDarkTheme) ColorSchemeMode.DARK else ColorSchemeMode.LIGHT
    ThemeMode.SYSTEM_BLACK -> if (isSystemInDarkTheme) ColorSchemeMode.BLACK else ColorSchemeMode.LIGHT
    ThemeMode.LIGHT -> ColorSchemeMode.LIGHT
    ThemeMode.DARK -> ColorSchemeMode.DARK
    ThemeMode.BLACK -> ColorSchemeMode.BLACK
}

fun ColorSchemeMode.isLight() = this == ColorSchemeMode.LIGHT
