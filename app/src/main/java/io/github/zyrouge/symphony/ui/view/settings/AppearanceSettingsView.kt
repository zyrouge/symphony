package io.github.zyrouge.symphony.ui.view.settings

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.i18n.CommonTranslation
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.SettingsFloatInputTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsOptionTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsSideHeading
import io.github.zyrouge.symphony.ui.components.settings.SettingsSwitchTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.PrimaryThemeColors
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.utils.ActivityUtils
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val language by context.symphony.settings.language.flow.collectAsState()
    val fontFamily by context.symphony.settings.fontFamily.flow.collectAsState()
    val themeMode by context.symphony.settings.themeMode.flow.collectAsState()
    val useMaterialYou by context.symphony.settings.useMaterialYou.flow.collectAsState()
    val primaryColor by context.symphony.settings.primaryColor.flow.collectAsState()
    val fontScale by context.symphony.settings.fontScale.flow.collectAsState()
    val contentScale by context.symphony.settings.contentScale.flow.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                AdaptiveSnackbar(it)
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text(context.symphony.t.Settings)
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
                    val contentColor = MaterialTheme.colorScheme.onPrimary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                ActivityUtils.startBrowserActivity(
                                    context.activity,
                                    Uri.parse(AppMeta.contributingUrl)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                null,
                                tint = contentColor,
                                modifier = Modifier.size(12.dp),
                            )
                            Box(modifier = Modifier.width(4.dp))
                            Text(
                                context.symphony.t.ConsiderContributing,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp, 0.dp)
                        ) {
                            Icon(
                                Icons.Filled.East,
                                null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
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
                        values = PrimaryThemeColors.entries.associateWith { it.label(context) },
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

fun PrimaryThemeColors.label(context: ViewContext) = when (this) {
    PrimaryThemeColors.Red -> context.symphony.t.Red
    PrimaryThemeColors.Orange -> context.symphony.t.Orange
    PrimaryThemeColors.Amber -> context.symphony.t.Amber
    PrimaryThemeColors.Yellow -> context.symphony.t.Yellow
    PrimaryThemeColors.Lime -> context.symphony.t.Lime
    PrimaryThemeColors.Green -> context.symphony.t.Green
    PrimaryThemeColors.Emerald -> context.symphony.t.Emerald
    PrimaryThemeColors.Teal -> context.symphony.t.Teal
    PrimaryThemeColors.Cyan -> context.symphony.t.Cyan
    PrimaryThemeColors.Sky -> context.symphony.t.Sky
    PrimaryThemeColors.Blue -> context.symphony.t.Blue
    PrimaryThemeColors.Indigo -> context.symphony.t.Indigo
    PrimaryThemeColors.Violet -> context.symphony.t.Violet
    PrimaryThemeColors.Purple -> context.symphony.t.Purple
    PrimaryThemeColors.Fuchsia -> context.symphony.t.Fuchsia
    PrimaryThemeColors.Pink -> context.symphony.t.Pink
    PrimaryThemeColors.Rose -> context.symphony.t.Rose
}
