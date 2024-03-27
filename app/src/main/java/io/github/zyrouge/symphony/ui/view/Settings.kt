package io.github.zyrouge.symphony.ui.view

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
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Wysiwyg
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.RuleFolder
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.SettingsDefaults
import io.github.zyrouge.symphony.services.i18n.CommonTranslation
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.PrimaryThemeColors
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.ui.view.home.ForYou
import io.github.zyrouge.symphony.ui.view.settings.SettingsFloatInputTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsLinkTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsMultiFolderTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsMultiOptionTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsMultiTextOptionTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsOptionTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsSideHeading
import io.github.zyrouge.symphony.ui.view.settings.SettingsSimpleTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsSliderTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsSwitchTile
import io.github.zyrouge.symphony.ui.view.settings.SettingsTextInputTile
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val scalingPresets = listOf(
    0.25f, 0.5f, 0.75f, 0.9f, 1f,
    1.1f, 1.25f, 1.5f, 1.75f, 2f,
    2.25f, 2.5f, 2.75f, 3f,
)

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
    val miniPlayerTextMarquee by context.symphony.settings.miniPlayerTextMarquee.collectAsState()
    val nowPlayingControlsLayout by context.symphony.settings.nowPlayingControlsLayout.collectAsState()
    val nowPlayingAdditionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.collectAsState()
    val nowPlayingSeekControls by context.symphony.settings.nowPlayingSeekControls.collectAsState()
    val nowPlayingLyricsLayout by context.symphony.settings.nowPlayingLyricsLayout.collectAsState()
    val songsFilterPattern by context.symphony.settings.songsFilterPattern.collectAsState()
    val blacklistFolders by context.symphony.settings.blacklistFolders.collectAsState()
    val whitelistFolders by context.symphony.settings.whitelistFolders.collectAsState()
    val artistTagSeparators by context.symphony.settings.artistTagSeparators.collectAsState()
    val genreTagSeparators by context.symphony.settings.genreTagSeparators.collectAsState()
    val checkForUpdates by context.symphony.settings.checkForUpdates.collectAsState()
    val showUpdateToast by context.symphony.settings.showUpdateToast.collectAsState()
    val fontScale by context.symphony.settings.fontScale.collectAsState()
    val contentScale by context.symphony.settings.contentScale.collectAsState()

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
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val contentColor = MaterialTheme.colorScheme.onPrimary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                context.symphony.shorty.startBrowserActivity(
                                    context.activity,
                                    AppMeta.contributingUrl,
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
                            context.symphony.settings.setLanguage(value.takeUnless { it == "" })
                        }
                    )
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
                            context.symphony.settings.setFontFamily(value)
                        }
                    )
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
                            context.symphony.settings.setFontScale(
                                SettingsDefaults.fontScale
                            )
                        },
                        onChange = { value ->
                            context.symphony.settings.setFontScale(value)
                        }
                    )
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
                            context.symphony.settings.setContentScale(
                                SettingsDefaults.contentScale
                            )
                        },
                        onChange = { value ->
                            context.symphony.settings.setContentScale(value)
                        }
                    )
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
                            context.symphony.settings.setThemeMode(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.Face, null)
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
                            Icon(Icons.Filled.Colorize, null)
                        },
                        title = {
                            Text(context.symphony.t.PrimaryColor)
                        },
                        value = ThemeColors.resolvePrimaryColorKey(primaryColor),
                        values = PrimaryThemeColors.entries.associateWith { it.label(context) },
                        enabled = !useMaterialYou,
                        onChange = { value ->
                            context.symphony.settings.setPrimaryColor(value.name)
                        }
                    )
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.Interface)
                    SettingsMultiOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Home, null)
                        },
                        title = {
                            Text(context.symphony.t.HomeTabs)
                        },
                        note = {
                            Text(context.symphony.t.SelectAtleast2orAtmost5Tabs)
                        },
                        value = homeTabs,
                        values = HomePages.entries.associateWith { it.label(context) },
                        satisfies = { it.size in 2..5 },
                        onChange = { value ->
                            context.symphony.settings.setHomeTabs(value)
                        }
                    )
                    SettingsMultiOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Recommend, null)
                        },
                        title = {
                            Text(context.symphony.t.ForYou)
                        },
                        value = forYouContents,
                        values = ForYou.entries.associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setForYouContents(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Label, null)
                        },
                        title = {
                            Text(context.symphony.t.BottomBarLabelVisibility)
                        },
                        value = homePageBottomBarLabelVisibility,
                        values = HomePageBottomBarLabelVisibility.entries
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setHomePageBottomBarLabelVisibility(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.Player)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.GraphicEq, null)
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
                            Icon(Icons.Filled.GraphicEq, null)
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
                            Icon(Icons.Filled.CenterFocusWeak, null)
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
                            Icon(Icons.Filled.CenterFocusWeak, null)
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
                            Icon(Icons.Filled.Headset, null)
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
                            Icon(Icons.Filled.HeadsetOff, null)
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
                            Icon(Icons.Filled.FastRewind, null)
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
                            Icon(Icons.Filled.FastForward, null)
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
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.MiniPlayer)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.SkipNext, null)
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
                            Icon(Icons.Filled.Forward30, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowSeekControls)
                        },
                        value = miniPlayerSeekControls,
                        onChange = { value ->
                            context.symphony.settings.setMiniPlayerSeekControls(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.KeyboardDoubleArrowRight, null)
                        },
                        title = {
                            Text(context.symphony.t.MiniPlayerTextMarquee)
                        },
                        value = miniPlayerTextMarquee,
                        onChange = { value ->
                            context.symphony.settings.setMiniPlayerTextMarquee(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.NowPlaying)
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.Filled.Dashboard, null)
                        },
                        title = {
                            Text(context.symphony.t.ControlsLayout)
                        },
                        value = nowPlayingControlsLayout,
                        values = NowPlayingControlsLayout.entries
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setNowPlayingControlsLayout(value)
                        }
                    )
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.AutoMirrored.Outlined.Article, null)
                        },
                        title = {
                            Text(context.symphony.t.LyricsLayout)
                        },
                        value = nowPlayingLyricsLayout,
                        values = NowPlayingLyricsLayout.entries
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.setNowPlayingLyricsLayout(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Wysiwyg, null)
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
                            Icon(Icons.Filled.Forward30, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowSeekControls)
                        },
                        value = nowPlayingSeekControls,
                        onChange = { value ->
                            context.symphony.settings.setNowPlayingSeekControls(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.Groove)
                    val defaultSongsFilterPattern = ".*"
                    SettingsTextInputTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.FilterAlt, null)
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
                            Icon(Icons.Filled.RuleFolder, null)
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
                            Icon(Icons.Filled.RuleFolder, null)
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
                    SettingsMultiTextOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.SpaceBar, null)
                        },
                        title = {
                            Text(context.symphony.t.ArtistTagValueSeparators)
                        },
                        values = artistTagSeparators.toList(),
                        onChange = {
                            context.symphony.settings.setArtistTagSeparators(it)
                            refetchLibrary()
                        },
                    )
                    SettingsMultiTextOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.SpaceBar, null)
                        },
                        title = {
                            Text(context.symphony.t.GenreTagValueSeparators)
                        },
                        values = genreTagSeparators.toList(),
                        onChange = {
                            context.symphony.settings.setGenreTagSeparators(it)
                            refetchLibrary()
                        },
                    )
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.Storage, null)
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
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.Updates)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.Update, null)
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
                            Icon(Icons.Filled.Update, null)
                        },
                        title = {
                            Text(context.symphony.t.ShowUpdateToast)
                        },
                        value = showUpdateToast,
                        onChange = { value ->
                            context.symphony.settings.setShowUpdateToast(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.Help)
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.BugReport, null)
                        },
                        title = {
                            Text(context.symphony.t.ReportAnIssue)
                        },
                        url = AppMeta.githubIssuesUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Forum, null)
                        },
                        title = {
                            Text(context.symphony.t.Discord)
                        },
                        url = AppMeta.discordUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Forum, null)
                        },
                        title = {
                            Text(context.symphony.t.Reddit)
                        },
                        url = AppMeta.redditUrl
                    )
                    HorizontalDivider()
                    SettingsSideHeading(context.symphony.t.About)
                    val isLatestVersion = AppMeta.latestVersion
                        ?.let { it == AppMeta.version }
                        ?: true
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.MusicNote, null)
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
                            Icon(
                                Icons.Filled.Favorite,
                                null,
                                tint = Color.Red,
                            )
                        },
                        title = {
                            Text(context.symphony.t.MadeByX(AppMeta.author))
                        },
                        url = AppMeta.githubProfileUrl
                    )
                    SettingsLinkTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Code, null)
                        },
                        title = {
                            Text(context.symphony.t.Github)
                        },
                        url = AppMeta.githubRepositoryUrl
                    )
                }
            }
        }
    )
}

fun HomePageBottomBarLabelVisibility.label(context: ViewContext) = when (this) {
    HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE -> context.symphony.t.AlwaysVisible
    HomePageBottomBarLabelVisibility.VISIBLE_WHEN_ACTIVE -> context.symphony.t.VisibleWhenActive
    HomePageBottomBarLabelVisibility.INVISIBLE -> context.symphony.t.Invisible
}

fun NowPlayingControlsLayout.label(context: ViewContext) = when (this) {
    NowPlayingControlsLayout.Default -> context.symphony.t.Default
    NowPlayingControlsLayout.Traditional -> context.symphony.t.Traditional
}

fun NowPlayingLyricsLayout.label(context: ViewContext) = when (this) {
    NowPlayingLyricsLayout.ReplaceArtwork -> context.symphony.t.ReplaceArtwork
    NowPlayingLyricsLayout.SeparatePage -> context.symphony.t.SeparatePage
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
