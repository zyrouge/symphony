package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.SettingsDataDefaults
import io.github.zyrouge.symphony.services.ThemeMode
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.PrimaryThemeColors
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.view.settings.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var settings by remember { mutableStateOf(context.symphony.settings.getSettings()) }

    val refetchLibrary = {
        coroutineScope.launch {
            context.symphony.groove.refetch()
        }
    }

    EventerEffect(context.symphony.settings.onChange) {
        settings = context.symphony.settings.getSettings()
    }

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
                        Text(context.symphony.t.settings)
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
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    SettingsSideHeading(context.symphony.t.appearance)
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Language, null)
                        },
                        title = {
                            Text(context.symphony.t.language_)
                        },
                        value = settings.language ?: context.symphony.t.language,
                        values = Translations.all.associate {
                            it.language to it.language
                        },
                        onChange = { value ->
                            context.symphony.settings.setLanguage(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Palette, null)
                        },
                        title = {
                            Text(context.symphony.t.theme)
                        },
                        value = settings.themeMode,
                        values = mapOf(
                            ThemeMode.SYSTEM to context.symphony.t.system,
                            ThemeMode.LIGHT to context.symphony.t.light,
                            ThemeMode.DARK to context.symphony.t.dark,
                            ThemeMode.BLACK to context.symphony.t.black,
                        ),
                        onChange = { value ->
                            context.symphony.settings.setThemeMode(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Face, null)
                        },
                        title = {
                            Text(context.symphony.t.materialYou)
                        },
                        value = settings.useMaterialYou,
                        onChange = { value ->
                            context.symphony.settings.setUseMaterialYou(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Colorize, null)
                        },
                        title = {
                            Text(context.symphony.t.primaryColor)
                        },
                        value = ThemeColors.resolvePrimaryColorKey(settings.primaryColor),
                        values = PrimaryThemeColors.values()
                            .associateWith { it.toHumanString() },
                        onChange = { value ->
                            context.symphony.settings.setPrimaryColor(value.name)
                        }
                    )
                    SettingsMultiOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Home, null)
                        },
                        title = {
                            Text(context.symphony.t.homeTabs)
                        },
                        note = {
                            Text(context.symphony.t.selectAtleast2orAtmost5Tabs)
                        },
                        value = settings.homeTabs,
                        values = HomePages.values().associateWith { it.label(context) },
                        satisfies = { it.size in 2..5 },
                        onChange = { value ->
                            context.symphony.settings.setHomeTabs(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Label, null)
                        },
                        title = {
                            Text(context.symphony.t.bottomBarLabelVisibility)
                        },
                        value = settings.homePageBottomBarLabelVisibility,
                        values = HomePageBottomBarLabelVisibility.values()
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setHomePageBottomBarLabelVisibility(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.SkipNext, null)
                        },
                        title = {
                            Text(context.symphony.t.miniPlayerExtendedControls)
                        },
                        value = settings.miniPlayerExtendedControls,
                        onChange = { value ->
                            context.symphony.settings.setMiniPlayerExtendedControls(value)
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.player)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.fadePlaybackInOut)
                        },
                        value = settings.fadePlayback,
                        onChange = { value ->
                            context.symphony.settings.setFadePlayback(value)
                        }
                    )
                    SettingsSliderTile(
                        context,
                        icon = {
                            Icon(Icons.Default.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.fadePlaybackInOut)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value))
                        },
                        range = 0.5f..6f,
                        initialValue = settings.fadePlaybackDuration,
                        onValue = { value ->
                            value.times(2).roundToInt().toFloat().div(2)
                        },
                        onChange = { value ->
                            context.symphony.settings.setFadePlaybackDuration(value)
                        },
                        onReset = {
                            context.symphony.settings.setFadePlaybackDuration(
                                SettingsDataDefaults.fadePlaybackDuration
                            )
                        },
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.requireAudioFocus)
                        },
                        value = settings.requireAudioFocus,
                        onChange = { value ->
                            context.symphony.settings.setRequireAudioFocus(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.ignoreAudioFocusLoss)
                        },
                        value = settings.ignoreAudioFocusLoss,
                        onChange = { value ->
                            context.symphony.settings.setIgnoreAudioFocusLoss(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Headset, null)
                        },
                        title = {
                            Text(context.symphony.t.playOnHeadphonesConnect)
                        },
                        value = settings.playOnHeadphonesConnect,
                        onChange = { value ->
                            context.symphony.settings.setPlayOnHeadphonesConnect(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.HeadsetOff, null)
                        },
                        title = {
                            Text(context.symphony.t.pauseOnHeadphonesDisconnect)
                        },
                        value = settings.pauseOnHeadphonesDisconnect,
                        onChange = { value ->
                            context.symphony.settings.setPauseOnHeadphonesDisconnect(value)
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.groove)
                    val defaultSongsFilterPattern = ".*"
                    SettingsTextInputTile(
                        context,
                        icon = {
                            Icon(Icons.Default.FilterAlt, null)
                        },
                        title = {
                            Text(context.symphony.t.songsFilterPattern)
                        },
                        value = settings.songsFilterPattern ?: defaultSongsFilterPattern,
                        onReset = {
                            context.symphony.settings.setSongsFilterPattern(null)
                        },
                        onChange = { value ->
                            context.symphony.settings.setSongsFilterPattern(
                                when (value) {
                                    defaultSongsFilterPattern -> null
                                    else -> value
                                }
                            )
                            refetchLibrary()
                        }
                    )
                    SettingsMultiFolderTile(
                        context,
                        icon = {
                            Icon(Icons.Default.RuleFolder, null)
                        },
                        title = {
                            Text(context.symphony.t.blacklistFolders)
                        },
                        explorer = context.symphony.groove.song.foldersExplorer,
                        initialValues = settings.blacklistFolders,
                        onChange = { values ->
                            context.symphony.settings.setBlacklistFolders(values)
                            refetchLibrary()
                        }
                    )
                    SettingsMultiFolderTile(
                        context,
                        icon = {
                            Icon(Icons.Default.RuleFolder, null)
                        },
                        title = {
                            Text(context.symphony.t.whitelistFolders)
                        },
                        explorer = context.symphony.groove.song.foldersExplorer,
                        initialValues = settings.whitelistFolders,
                        onChange = { values ->
                            context.symphony.settings.setWhitelistFolders(values)
                            refetchLibrary()
                        }
                    )
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Default.Storage, null)
                        },
                        title = {
                            Text(context.symphony.t.clearSongCache)
                        },
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    context.symphony.t.songCacheCleared,
                                    withDismissAction = true,
                                )
                                
                                context.symphony.database.songCache.update(mapOf())
                                refetchLibrary()
                            }
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.about)
                    val isLatestVersion = AppMeta.latestVersion
                        ?.let { it == AppMeta.version }
                        ?: true
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Default.MusicNote, null)
                        },
                        title = {
                            Text("${AppMeta.appName} ${AppMeta.version}")
                        },
                        subtitle = when {
                            !isLatestVersion -> ({
                                Text(context.symphony.t.newVersionAvailableX(AppMeta.latestVersion!!))
                            })
                            else -> null
                        },
                        onClick = {
                            context.symphony.shorty.startBrowserActivity(
                                context.activity,
                                when {
                                    isLatestVersion -> AppMeta.githubRepositoryUrl
                                    else -> AppMeta.githubLatestReleaseUrl
                                }
                            )
                        }
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Favorite, null, tint = Color.Red)
                        },
                        title = {
                            Text(context.symphony.t.madeByX(AppMeta.author))
                        },
                        url = AppMeta.githubProfileUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Code, null)
                        },
                        title = {
                            Text(context.symphony.t.github)
                        },
                        url = AppMeta.githubRepositoryUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Redeem, null)
                        },
                        title = {
                            Text(context.symphony.t.sponsorViaGitHub)
                        },
                        url = AppMeta.githubSponsorsUrl
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Update, null)
                        },
                        title = {
                            Text(context.symphony.t.checkForUpdates)
                        },
                        value = settings.checkForUpdates,
                        onChange = { value ->
                            context.symphony.settings.setCheckForUpdates(value)
                        }
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.BugReport, null)
                        },
                        title = {
                            Text(context.symphony.t.reportAnIssue)
                        },
                        url = AppMeta.githubIssuesUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Forum, null)
                        },
                        title = {
                            Text(context.symphony.t.discord)
                        },
                        url = AppMeta.discordUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Forum, null)
                        },
                        title = {
                            Text(context.symphony.t.reddit)
                        },
                        url = AppMeta.redditUrl
                    )
                }
            }
        }
    )
}

fun HomePageBottomBarLabelVisibility.label(context: ViewContext): String {
    return when (this) {
        HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE -> context.symphony.t.alwaysVisible
        HomePageBottomBarLabelVisibility.VISIBLE_WHEN_ACTIVE -> context.symphony.t.visibleWhenActive
        HomePageBottomBarLabelVisibility.INVISIBLE -> context.symphony.t.invisible
    }
}
