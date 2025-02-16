package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.services.i18n.CommonTranslation
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.ConsiderContributingTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsFloatInputTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsOptionTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsSideHeading
import io.github.zyrouge.symphony.ui.components.settings.SettingsSwitchTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.PrimaryThemeColor
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import kotlinx.serialization.Serializable

private val scalingPresets = listOf(
    0.25f, 0.5f, 0.75f, 0.9f, 1f,
    1.1f, 1.25f, 1.5f, 1.75f, 2f,
    2.25f, 2.5f, 2.75f, 3f,
)

@Serializable
object AppearanceSettingsViewRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsView(context: ViewContext) {
    val scrollState = rememberScrollState()
    val language by context.symphony.settings.language.flow.collectAsStateWithLifecycle()
    val fontFamily by context.symphony.settings.fontFamily.flow.collectAsStateWithLifecycle()
    val themeMode by context.symphony.settings.themeMode.flow.collectAsStateWithLifecycle()
    val useMaterialYou by context.symphony.settings.useMaterialYou.flow.collectAsStateWithLifecycle()
    val primaryColor by context.symphony.settings.primaryColor.flow.collectAsStateWithLifecycle()
    val fontScale by context.symphony.settings.fontScale.flow.collectAsStateWithLifecycle()
    val contentScale by context.symphony.settings.contentScale.flow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text("${context.symphony.t.Settings} - ${context.symphony.t.Appearance}")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            context.navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButtonPlaceholder()
                },
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    ConsiderContributingTile(context)
                    SettingsSideHeading(context.symphony.t.Appearance)
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Filled.Language, null)
                        },
                        title = {
                            Text(context.symphony.t.Language_)
                        },
                        value = language ?: "",
                        values = run {
                            val defaultLocaleNativeName =
                                context.symphony.translator.getDefaultLocaleNativeName()
                            mapOf(
                                "" to "${context.symphony.t.System} (${defaultLocaleNativeName})"
                            ) + context.symphony.translator.translations.localeNativeNames
                        },
                        captions = run {
                            val defaultLocaleDisplayName =
                                context.symphony.translator.getDefaultLocaleDisplayName()
                            mapOf(
                                "" to "${CommonTranslation.System} (${defaultLocaleDisplayName})"
                            ) + context.symphony.translator.translations.localeDisplayNames
                        },
                        onChange = { value ->
                            context.symphony.settings.language.setValue(value.takeUnless { it == "" })
                        }
                    )
                    HorizontalDivider()
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Filled.TextFormat, null)
                        },
                        title = {
                            Text(context.symphony.t.Font)
                        },
                        value = SymphonyTypography.resolveFont(fontFamily).fontName,
                        values = SymphonyTypography.all.keys.associateWith { it },
                        onChange = { value ->
                            context.symphony.settings.fontFamily.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsFloatInputTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.TextIncrease, null)
                        },
                        title = {
                            Text(context.symphony.t.FontScale)
                        },
                        value = fontScale,
                        presets = scalingPresets,
                        labelText = { "x$it" },
                        onReset = {
                            context.symphony.settings.fontScale.setValue(
                                context.symphony.settings.fontScale.defaultValue,
                            )
                        },
                        onChange = { value ->
                            context.symphony.settings.fontScale.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsFloatInputTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.PhotoSizeSelectLarge, null)
                        },
                        title = {
                            Text(context.symphony.t.ContentScale)
                        },
                        value = contentScale,
                        presets = scalingPresets,
                        labelText = { "x$it" },
                        onReset = {
                            context.symphony.settings.contentScale.setValue(
                                context.symphony.settings.contentScale.defaultValue,
                            )
                        },
                        onChange = { value ->
                            context.symphony.settings.contentScale.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Filled.Palette, null)
                        },
                        title = {
                            Text(context.symphony.t.Theme)
                        },
                        value = themeMode,
                        values = mapOf(
                            ThemeMode.SYSTEM to context.symphony.t.SystemLightDark,
                            ThemeMode.SYSTEM_BLACK to context.symphony.t.SystemLightBlack,
                            ThemeMode.LIGHT to context.symphony.t.Light,
                            ThemeMode.DARK to context.symphony.t.Dark,
                            ThemeMode.BLACK to context.symphony.t.Black,
                        ),
                        onChange = { value ->
                            context.symphony.settings.themeMode.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.Face, null)
                        },
                        title = {
                            Text(context.symphony.t.MaterialYou)
                        },
                        value = useMaterialYou,
                        onChange = { value ->
                            context.symphony.settings.useMaterialYou.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Filled.Colorize, null)
                        },
                        title = {
                            Text(context.symphony.t.PrimaryColor)
                        },
                        value = ThemeColors.resolvePrimaryColorKey(primaryColor),
                        values = PrimaryThemeColor.entries.associateWith { it.label(context) },
                        enabled = !useMaterialYou,
                        onChange = { value ->
                            context.symphony.settings.primaryColor.setValue(value.name)
                        }
                    )
                }
            }
        }
    )
}

fun PrimaryThemeColor.label(context: ViewContext) = when (this) {
    PrimaryThemeColor.Red -> context.symphony.t.Red
    PrimaryThemeColor.Orange -> context.symphony.t.Orange
    PrimaryThemeColor.Amber -> context.symphony.t.Amber
    PrimaryThemeColor.Yellow -> context.symphony.t.Yellow
    PrimaryThemeColor.Lime -> context.symphony.t.Lime
    PrimaryThemeColor.Green -> context.symphony.t.Green
    PrimaryThemeColor.Emerald -> context.symphony.t.Emerald
    PrimaryThemeColor.Teal -> context.symphony.t.Teal
    PrimaryThemeColor.Cyan -> context.symphony.t.Cyan
    PrimaryThemeColor.Sky -> context.symphony.t.Sky
    PrimaryThemeColor.Blue -> context.symphony.t.Blue
    PrimaryThemeColor.Indigo -> context.symphony.t.Indigo
    PrimaryThemeColor.Violet -> context.symphony.t.Violet
    PrimaryThemeColor.Purple -> context.symphony.t.Purple
    PrimaryThemeColor.Fuchsia -> context.symphony.t.Fuchsia
    PrimaryThemeColor.Pink -> context.symphony.t.Pink
    PrimaryThemeColor.Rose -> context.symphony.t.Rose
}
