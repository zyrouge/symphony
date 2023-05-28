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
import io.github.zyrouge.symphony.services.SettingsDefaults
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.PrimaryThemeColors
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.ui.view.home.ForYou
import io.github.zyrouge.symphony.ui.view.settings.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val language by context.symphony.settings.language.collectAsState()
    val fontFamily by context.symphony.settings.fontFamily.collectAsState()
    val themeMode by context.symphony.settings.themeMode.collectAsState()
    val useMaterialYou by context.symphony.settings.useMaterialYou.collectAsState()
    val primaryColor by context.symphony.settings.primaryColor.collectAsState()
    val homeTabs by context.symphony.settings.homeTabs.collectAsState()
    val forYouContents by context.symphony.settings.forYouContents.collectAsState()
    val homePageBottomBarLabelVisibility by context.symphony.settings.homePageBottomBarLabelVisibility.collectAsState()
    val fadePlayback by context.symphony.settings.fadePlayback.collectAsState()
    val fadePlaybackDuration by context.symphony.settings.fadePlaybackDuration.collectAsState()
    val requireAudioFocus by context.symphony.settings.requireAudioFocus.collectAsState()
    val ignoreAudioFocusLoss by context.symphony.settings.ignoreAudioFocusLoss.collectAsState()
    val playOnHeadphonesConnect by context.symphony.settings.playOnHeadphonesConnect.collectAsState()
    val pauseOnHeadphonesDisconnect by context.symphony.settings.pauseOnHeadphonesDisconnect.collectAsState()
    val seekBackDuration by context.symphony.settings.seekBackDuration.collectAsState()
    val seekForwardDuration by context.symphony.settings.seekForwardDuration.collectAsState()
    val miniPlayerTrackControls by context.symphony.settings.miniPlayerTrackControls.collectAsState()
    val miniPlayerSeekControls by context.symphony.settings.miniPlayerSeekControls.collectAsState()
    val nowPlayingControlsLayout by context.symphony.settings.nowPlayingControlsLayout.collectAsState()
    val nowPlayingAdditionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.collectAsState()
    val nowPlayingSeekControls by context.symphony.settings.nowPlayingSeekControls.collectAsState()
    val songsFilterPattern by context.symphony.settings.songsFilterPattern.collectAsState()
    val blacklistFolders by context.symphony.settings.blacklistFolders.collectAsState()
    val whitelistFolders by context.symphony.settings.whitelistFolders.collectAsState()
    val checkForUpdates by context.symphony.settings.checkForUpdates.collectAsState()
    val showUpdateToast by context.symphony.settings.showUpdateToast.collectAsState()

    val refetchLibrary = {
        context.symphony.radio.stop()
        coroutineScope.launch {
            context.symphony.groove.refetch()
        }
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
                    SettingsSideHeading(context.symphony.t.Appearance)
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Language, null)
                        },
                        title = {
                            Text(context.symphony.t.Language_)
                        },
                        value = language ?: "",
                        values = run {
                            val defaultTranslation =
                                context.symphony.translator.getDefaultTranslation()
                            mapOf(
                                "" to "${context.symphony.t.System} (${defaultTranslation.Language})"
                            ) + Translations.all.values.associate { it.Locale to it.Language }
                        },
                        onChange = { value ->
                            context.symphony.settings.setLanguage(value.takeUnless { it == "" })
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.TextFormat, null)
                        },
                        title = {
                            Text(context.symphony.t.Font)
                        },
                        value = SymphonyTypography.resolveFont(fontFamily).fontName,
                        values = SymphonyTypography.all.keys.associateWith { it },
                        onChange = { value ->
                            context.symphony.settings.setFontFamily(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Palette, null)
                        },
                        title = {
                            Text(context.symphony.t.Theme)
                        },
                        value = themeMode,
                        values = mapOf(
                            ThemeMode.SYSTEM to context.symphony.t.System,
                            ThemeMode.LIGHT to context.symphony.t.Light,
                            ThemeMode.DARK to context.symphony.t.Dark,
                            ThemeMode.BLACK to context.symphony.t.Black,
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
                            Text(context.symphony.t.MaterialYou)
                        },
                        value = useMaterialYou,
                        onChange = { value ->
                            context.symphony.settings.setUseMaterialYou(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Colorize, null)
                        },
                        title = {
                            Text(context.symphony.t.PrimaryColor)
                        },
                        value = ThemeColors.resolvePrimaryColorKey(primaryColor),
                        values = PrimaryThemeColors.values().associateWith { it.toHumanString() },
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
                            Text(context.symphony.t.HomeTabs)
                        },
                        note = {
                            Text(context.symphony.t.SelectAtleast2orAtmost5Tabs)
                        },
                        value = homeTabs,
                        values = HomePages.values().associateWith { it.label(context) },
                        satisfies = { it.size in 2..5 },
                        onChange = { value ->
                            context.symphony.settings.setHomeTabs(value)
                        }
                    )
                    SettingsMultiOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Recommend, null)
                        },
                        title = {
                            Text(context.symphony.t.ForYou)
                        },
                        value = forYouContents,
                        values = ForYou.values().associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setForYouContents(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Label, null)
                        },
                        title = {
                            Text(context.symphony.t.BottomBarLabelVisibility)
                        },
                        value = homePageBottomBarLabelVisibility,
                        values = HomePageBottomBarLabelVisibility.values()
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setHomePageBottomBarLabelVisibility(value)
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.Player)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.FadePlaybackInOut)
                        },
                        value = fadePlayback,
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
                            Text(context.symphony.t.FadePlaybackInOut)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value.toString()))
                        },
                        range = 0.5f..6f,
                        initialValue = fadePlaybackDuration,
                        onValue = { value ->
                            value.times(2).roundToInt().toFloat().div(2)
                        },
                        onChange = { value ->
                            context.symphony.settings.setFadePlaybackDuration(value)
                        },
                        onReset = {
                            context.symphony.settings.setFadePlaybackDuration(
                                SettingsDefaults.fadePlaybackDuration
                            )
                        },
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.RequireAudioFocus)
                        },
                        value = requireAudioFocus,
                        onChange = { value ->
                            context.symphony.settings.setRequireAudioFocus(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.IgnoreAudioFocusLoss)
                        },
                        value = ignoreAudioFocusLoss,
                        onChange = { value ->
                            context.symphony.settings.setIgnoreAudioFocusLoss(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Headset, null)
                        },
                        title = {
                            Text(context.symphony.t.PlayOnHeadphonesConnect)
                        },
                        value = playOnHeadphonesConnect,
                        onChange = { value ->
                            context.symphony.settings.setPlayOnHeadphonesConnect(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.HeadsetOff, null)
                        },
                        title = {
                            Text(context.symphony.t.PauseOnHeadphonesDisconnect)
                        },
                        value = pauseOnHeadphonesDisconnect,
                        onChange = { value ->
                            context.symphony.settings.setPauseOnHeadphonesDisconnect(value)
                        }
                    )
                    val seekDurationRange = 3f..60f
                    SettingsSliderTile(
                        context,
                        icon = {
                            Icon(Icons.Default.FastRewind, null)
                        },
                        title = {
                            Text(context.symphony.t.FastRewindDuration)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value.toString()))
                        },
                        range = seekDurationRange,
                        initialValue = seekBackDuration.toFloat(),
                        onValue = { value ->
                            value.roundToInt().toFloat()
                        },
                        onChange = { value ->
                            context.symphony.settings.setSeekBackDuration(value.toInt())
                        },
                        onReset = {
                            context.symphony.settings.setSeekBackDuration(
                                SettingsDefaults.seekBackDuration
                            )
                        },
                    )
                    SettingsSliderTile(
                        context,
                        icon = {
                            Icon(Icons.Default.FastForward, null)
                        },
                        title = {
                            Text(context.symphony.t.FastForwardDuration)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value.toString()))
                        },
                        range = seekDurationRange,
                        initialValue = seekForwardDuration.toFloat(),
                        onValue = { value ->
                            value.roundToInt().toFloat()
                        },
                        onChange = { value ->
                            context.symphony.settings.setSeekForwardDuration(value.toInt())
                        },
                        onReset = {
                            context.symphony.settings.setSeekForwardDuration(
                                SettingsDefaults.seekForwardDuration
                            )
                        },
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.MiniPlayer)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.SkipNext, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowTrackControls)
                        },
                        value = miniPlayerTrackControls,
                        onChange = { value ->
                            context.symphony.settings.setMiniPlayerTrackControls(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Forward30, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowSeekControls)
                        },
                        value = miniPlayerSeekControls,
                        onChange = { value ->
                            context.symphony.settings.setMiniPlayerSeekControls(value)
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.NowPlaying)
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Default.Dashboard, null)
                        },
                        title = {
                            Text(context.symphony.t.ControlsLayout)
                        },
                        value = nowPlayingControlsLayout,
                        values = NowPlayingControlsLayout.values()
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setNowPlayingControlsLayout(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Wysiwyg, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowAudioInformation)
                        },
                        value = nowPlayingAdditionalInfo,
                        onChange = { value ->
                            context.symphony.settings.showNowPlayingAdditionalInfo(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Forward30, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowSeekControls)
                        },
                        value = nowPlayingSeekControls,
                        onChange = { value ->
                            context.symphony.settings.setNowPlayingSeekControls(value)
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.Groove)
                    val defaultSongsFilterPattern = ".*"
                    SettingsTextInputTile(
                        context,
                        icon = {
                            Icon(Icons.Default.FilterAlt, null)
                        },
                        title = {
                            Text(context.symphony.t.SongsFilterPattern)
                        },
                        value = songsFilterPattern ?: defaultSongsFilterPattern,
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
                            Text(context.symphony.t.BlacklistFolders)
                        },
                        explorer = context.symphony.groove.mediaStore.explorer,
                        initialValues = blacklistFolders,
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
                            Text(context.symphony.t.WhitelistFolders)
                        },
                        explorer = context.symphony.groove.mediaStore.explorer,
                        initialValues = whitelistFolders,
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
                            Text(context.symphony.t.ClearSongCache)
                        },
                        onClick = {
                            coroutineScope.launch {
                                context.symphony.database.songCache.update(emptyMap())
                                refetchLibrary()
                                snackbarHostState.showSnackbar(
                                    context.symphony.t.SongCacheCleared,
                                    withDismissAction = true,
                                )
                            }
                        }
                    )
                    Divider()
                    SettingsSideHeading(context.symphony.t.About)
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
                                Text(context.symphony.t.NewVersionAvailableX(AppMeta.latestVersion!!))
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
                            Text(context.symphony.t.MadeByX(AppMeta.author))
                        },
                        url = AppMeta.githubProfileUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Code, null)
                        },
                        title = {
                            Text(context.symphony.t.GitHub)
                        },
                        url = AppMeta.githubRepositoryUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Store, null)
                        },
                        title = {
                            Text(context.symphony.t.IzzyOnDroid)
                        },
                        url = AppMeta.izzyOnDroidUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Store, null)
                        },
                        title = {
                            Text(context.symphony.t.FDroid)
                        },
                        url = AppMeta.fdroidUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Redeem, null)
                        },
                        title = {
                            Text(context.symphony.t.SponsorViaGitHub)
                        },
                        url = AppMeta.githubSponsorsUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Redeem, null)
                        },
                        title = {
                            Text(context.symphony.t.SponsorViaKoFi)
                        },
                        url = AppMeta.koFiUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Redeem, null)
                        },
                        title = {
                            Text(context.symphony.t.SponsorViaPatreon)
                        },
                        url = AppMeta.patreonUrl
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Update, null)
                        },
                        title = {
                            Text(context.symphony.t.CheckForUpdates)
                        },
                        value = checkForUpdates,
                        onChange = { value ->
                            context.symphony.settings.setCheckForUpdates(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Default.Update, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowUpdateToast)
                        },
                        value = showUpdateToast,
                        onChange = { value ->
                            context.symphony.settings.setShowUpdateToast(value)
                        }
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.BugReport, null)
                        },
                        title = {
                            Text(context.symphony.t.ReportAnIssue)
                        },
                        url = AppMeta.githubIssuesUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Forum, null)
                        },
                        title = {
                            Text(context.symphony.t.Discord)
                        },
                        url = AppMeta.discordUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Forum, null)
                        },
                        title = {
                            Text(context.symphony.t.Reddit)
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
        HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE -> context.symphony.t.AlwaysVisible
        HomePageBottomBarLabelVisibility.VISIBLE_WHEN_ACTIVE -> context.symphony.t.VisibleWhenActive
        HomePageBottomBarLabelVisibility.INVISIBLE -> context.symphony.t.Invisible
    }
}

fun NowPlayingControlsLayout.label(context: ViewContext): String {
    return when (this) {
        NowPlayingControlsLayout.Default -> context.symphony.t.Default
        NowPlayingControlsLayout.Traditional -> context.symphony.t.Traditional
    }
}
