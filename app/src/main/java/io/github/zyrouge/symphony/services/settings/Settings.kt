package io.github.zyrouge.symphony.services.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.repositories.AlbumArtistRepository
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import io.github.zyrouge.symphony.services.groove.repositories.ArtistRepository
import io.github.zyrouge.symphony.services.groove.repositories.GenreRepository
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.ui.view.HomePageBottomBarLabelVisibility
import io.github.zyrouge.symphony.ui.view.HomePages
import io.github.zyrouge.symphony.ui.view.NowPlayingControlsLayout
import io.github.zyrouge.symphony.ui.view.NowPlayingLyricsLayout
import io.github.zyrouge.symphony.ui.view.home.ForYou
import io.github.zyrouge.symphony.utils.StringListUtils
import io.github.zyrouge.symphony.utils.getEnum
import io.github.zyrouge.symphony.utils.parseEnumValue
import io.github.zyrouge.symphony.utils.putEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Suppress("MemberVisibilityCanBePrivate")
class Settings(private val symphony: Symphony) {
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(getLanguage())
    val language = _language.asStateFlow()

    private val _useMaterialYou = MutableStateFlow(getUseMaterialYou())
    val useMaterialYou = _useMaterialYou.asStateFlow()

    private val _lastUsedSongsSortBy = MutableStateFlow(getLastUsedSongsSortBy())
    val lastUsedSongsSortBy = _lastUsedSongsSortBy.asStateFlow()

    private val _lastUsedSongsSortReverse = MutableStateFlow(getLastUsedSongsSortReverse())
    val lastUsedSongsSortReverse = _lastUsedSongsSortReverse.asStateFlow()

    private val _lastUsedArtistsSortBy = MutableStateFlow(getLastUsedArtistsSortBy())
    val lastUsedArtistsSortBy = _lastUsedArtistsSortBy.asStateFlow()

    private val _lastUsedArtistsSortReverse = MutableStateFlow(getLastUsedArtistsSortReverse())
    val lastUsedArtistsSortReverse = _lastUsedArtistsSortReverse.asStateFlow()

    private val _lastUsedAlbumArtistsSortBy = MutableStateFlow(getLastUsedAlbumArtistsSortBy())
    val lastUsedAlbumArtistsSortBy = _lastUsedAlbumArtistsSortBy.asStateFlow()

    private val _lastUsedAlbumArtistsSortReverse =
        MutableStateFlow(getLastUsedAlbumArtistsSortReverse())
    val lastUsedAlbumArtistsSortReverse = _lastUsedAlbumArtistsSortReverse.asStateFlow()

    private val _lastUsedAlbumsSortBy = MutableStateFlow(getLastUsedAlbumsSortBy())
    val lastUsedAlbumsSortBy = _lastUsedAlbumsSortBy.asStateFlow()

    private val _lastUsedAlbumsSortReverse = MutableStateFlow(getLastUsedAlbumsSortReverse())
    val lastUsedAlbumsSortReverse = _lastUsedAlbumsSortReverse.asStateFlow()

    private val _lastUsedGenresSortBy = MutableStateFlow(getLastUsedGenresSortBy())
    val lastUsedGenresSortBy = _lastUsedGenresSortBy.asStateFlow()

    private val _lastUsedGenresSortReverse = MutableStateFlow(getLastUsedGenresSortReverse())
    val lastUsedGenresSortReverse = _lastUsedGenresSortReverse.asStateFlow()

    private val _lastUsedBrowserSortBy = MutableStateFlow(getLastUsedBrowserSortBy())
    val lastUsedBrowserSortBy = _lastUsedBrowserSortBy.asStateFlow()

    private val _lastUsedBrowserSortReverse = MutableStateFlow(getLastUsedBrowserSortReverse())
    val lastUsedBrowserSortReverse = _lastUsedBrowserSortReverse.asStateFlow()

    private val _lastUsedBrowserPath = MutableStateFlow(getLastUsedBrowserPath())
    val lastUsedBrowserPath = _lastUsedBrowserPath.asStateFlow()

    private val _lastUsedPlaylistsSortBy = MutableStateFlow(getLastUsedPlaylistsSortBy())
    val lastUsedPlaylistsSortBy = _lastUsedPlaylistsSortBy.asStateFlow()

    private val _lastUsedPlaylistsSortReverse = MutableStateFlow(getLastUsedPlaylistsSortReverse())
    val lastUsedPlaylistsSortReverse = _lastUsedPlaylistsSortReverse.asStateFlow()

    private val _lastUsedPlaylistSongsSortBy = MutableStateFlow(getLastUsedPlaylistSongsSortBy())
    val lastUsedPlaylistSongsSortBy = _lastUsedPlaylistSongsSortBy.asStateFlow()

    private val _lastUsedPlaylistSongsSortReverse =
        MutableStateFlow(getLastUsedPlaylistSongsSortReverse())
    val lastUsedPlaylistSongsSortReverse = _lastUsedPlaylistSongsSortReverse.asStateFlow()

    private val _lastUsedAlbumSongsSortBy = MutableStateFlow(getLastUsedAlbumSongsSortBy())
    val lastUsedAlbumSongsSortBy = _lastUsedAlbumSongsSortBy.asStateFlow()

    private val _lastUsedAlbumSongsSortReverse =
        MutableStateFlow(getLastUsedAlbumSongsSortReverse())
    val lastUsedAlbumSongsSortReverse = _lastUsedAlbumSongsSortReverse.asStateFlow()

    private val _lastUsedTreePathSortBy = MutableStateFlow(getLastUsedTreePathSortBy())
    val lastUsedTreePathSortBy = _lastUsedTreePathSortBy.asStateFlow()

    private val _lastUsedTreePathSortReverse = MutableStateFlow(getLastUsedTreePathSortReverse())
    val lastUsedTreePathSortReverse = _lastUsedTreePathSortReverse.asStateFlow()

    private val _lastUsedFoldersSortBy = MutableStateFlow(getLastUsedFoldersSortBy())
    val lastUsedFoldersSortBy = _lastUsedFoldersSortBy.asStateFlow()

    private val _lastUsedFoldersSortReverse = MutableStateFlow(getLastUsedFoldersSortReverse())
    val lastUsedFoldersSortReverse = _lastUsedFoldersSortReverse.asStateFlow()

    private val _lastDisabledTreePaths = MutableStateFlow(getLastDisabledTreePaths())
    val lastDisabledTreePaths = _lastDisabledTreePaths.asStateFlow()

    private val _homeLastTab = MutableStateFlow(getHomeLastTab())
    val homeLastTab = _homeLastTab.asStateFlow()

    private val _songsFilterPattern = MutableStateFlow(getSongsFilterPattern())
    val songsFilterPattern = _songsFilterPattern.asStateFlow()

    private val _checkForUpdates = MutableStateFlow(getCheckForUpdates())
    val checkForUpdates = _checkForUpdates.asStateFlow()

    private val _fadePlayback = MutableStateFlow(getFadePlayback())
    val fadePlayback = _fadePlayback.asStateFlow()

    private val _requireAudioFocus = MutableStateFlow(getRequireAudioFocus())
    val requireAudioFocus = _requireAudioFocus.asStateFlow()

    private val _ignoreAudioFocusLoss = MutableStateFlow(getIgnoreAudioFocusLoss())
    val ignoreAudioFocusLoss = _ignoreAudioFocusLoss.asStateFlow()

    private val _playOnHeadphonesConnect = MutableStateFlow(getPlayOnHeadphonesConnect())
    val playOnHeadphonesConnect = _playOnHeadphonesConnect.asStateFlow()

    private val _pauseOnHeadphonesDisconnect = MutableStateFlow(getPauseOnHeadphonesDisconnect())
    val pauseOnHeadphonesDisconnect = _pauseOnHeadphonesDisconnect.asStateFlow()

    private val _primaryColor = MutableStateFlow(getPrimaryColor())
    val primaryColor = _primaryColor.asStateFlow()

    private val _fadePlaybackDuration = MutableStateFlow(getFadePlaybackDuration())
    val fadePlaybackDuration = _fadePlaybackDuration.asStateFlow()

    private val _homeTabs = MutableStateFlow(getHomeTabs())
    val homeTabs = _homeTabs.asStateFlow()

    private val _homePageBottomBarLabelVisibility =
        MutableStateFlow(getHomePageBottomBarLabelVisibility())
    val homePageBottomBarLabelVisibility = _homePageBottomBarLabelVisibility.asStateFlow()

    private val _forYouContents = MutableStateFlow(getForYouContents())
    val forYouContents = _forYouContents.asStateFlow()

    private val _blacklistFolders = MutableStateFlow(getBlacklistFolders())
    val blacklistFolders = _blacklistFolders.asStateFlow()

    private val _whitelistFolders = MutableStateFlow(getWhitelistFolders())
    val whitelistFolders = _whitelistFolders.asStateFlow()

    private val _readIntroductoryMessage = MutableStateFlow(getReadIntroductoryMessage())
    val readIntroductoryMessage = _readIntroductoryMessage.asStateFlow()

    private val _nowPlayingAdditionalInfo = MutableStateFlow(getNowPlayingAdditionalInfo())
    val nowPlayingAdditionalInfo = _nowPlayingAdditionalInfo.asStateFlow()

    private val _nowPlayingSeekControls = MutableStateFlow(getNowPlayingSeekControls())
    val nowPlayingSeekControls = _nowPlayingSeekControls.asStateFlow()

    private val _seekBackDuration = MutableStateFlow(getSeekBackDuration())
    val seekBackDuration = _seekBackDuration.asStateFlow()

    private val _seekForwardDuration = MutableStateFlow(getSeekForwardDuration())
    val seekForwardDuration = _seekForwardDuration.asStateFlow()

    private val _miniPlayerTrackControls = MutableStateFlow(getMiniPlayerTrackControls())
    val miniPlayerTrackControls = _miniPlayerTrackControls.asStateFlow()

    private val _miniPlayerSeekControls = MutableStateFlow(getMiniPlayerSeekControls())
    val miniPlayerSeekControls = _miniPlayerSeekControls.asStateFlow()

    private val _fontFamily = MutableStateFlow(getFontFamily())
    val fontFamily = _fontFamily.asStateFlow()

    private val _nowPlayingControlsLayout = MutableStateFlow(getNowPlayingControlsLayout())
    val nowPlayingControlsLayout = _nowPlayingControlsLayout.asStateFlow()

    private val _showUpdateToast = MutableStateFlow(getShowUpdateToast())
    val showUpdateToast = _showUpdateToast.asStateFlow()

    private val _fontScale = MutableStateFlow(getFontScale())
    val fontScale = _fontScale.asStateFlow()

    private val _contentScale = MutableStateFlow(getContentScale())
    val contentScale = _contentScale.asStateFlow()

    private val _nowPlayingLyricsLayout = MutableStateFlow(getNowPlayingLyricsLayout())
    val nowPlayingLyricsLayout = _nowPlayingLyricsLayout.asStateFlow()

    private val _artistTagSeparators = MutableStateFlow(getArtistTagSeparators())
    val artistTagSeparators = _artistTagSeparators.asStateFlow()

    private val _genreTagSeparators = MutableStateFlow(getGenreTagSeparators())
    val genreTagSeparators = _genreTagSeparators.asStateFlow()

    private val _miniPlayerTextMarquee = MutableStateFlow(getMiniPlayerTextMarquee())
    val miniPlayerTextMarquee = _miniPlayerTextMarquee.asStateFlow()

    private val _mediaFolders = MutableStateFlow(getMediaFolders())
    val mediaFolders = _mediaFolders.asStateFlow()

    private fun getThemeMode() = getSharedPreferences().getString(SettingsKeys.themeMode, null)
        ?.let { ThemeMode.valueOf(it) }
        ?: SettingsDefaults.themeMode

    fun setThemeMode(themeMode: ThemeMode) {
        getSharedPreferences().edit {
            putString(SettingsKeys.themeMode, themeMode.name)
        }
        _themeMode.update { getThemeMode() }
    }

    private fun getLanguage() = getSharedPreferences().getString(SettingsKeys.language, null)

    fun setLanguage(language: String?) {
        getSharedPreferences().edit {
            putString(SettingsKeys.language, language)
        }
        _language.update { getLanguage() }
    }

    private fun getUseMaterialYou() = getSharedPreferences().getBoolean(
        SettingsKeys.useMaterialYou,
        SettingsDefaults.useMaterialYou,
    )

    fun setUseMaterialYou(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.useMaterialYou, value)
        }
        _useMaterialYou.update { getUseMaterialYou() }
    }

    private fun getLastUsedSongsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedSongsSortBy, null)
        ?: SettingsDefaults.lastUsedSongSortBy

    fun setLastUsedSongsSortBy(sortBy: SongRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedSongsSortBy, sortBy)
        }
        _lastUsedSongsSortBy.update { getLastUsedSongsSortBy() }
    }

    private fun getLastUsedSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedSongsSortReverse, false)

    fun setLastUsedSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedSongsSortReverse, reverse)
        }
        _lastUsedSongsSortReverse.update { getLastUsedSongsSortReverse() }
    }

    private fun getLastUsedArtistsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedArtistsSortBy, null)
        ?: SettingsDefaults.lastUsedArtistsSortBy

    fun setLastUsedArtistsSortBy(sortBy: ArtistRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedArtistsSortBy, sortBy)
        }
        _lastUsedArtistsSortBy.update { getLastUsedArtistsSortBy() }
    }

    private fun getLastUsedArtistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedArtistsSortReverse, false)

    fun setLastUsedArtistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedArtistsSortReverse, reverse)
        }
        _lastUsedArtistsSortReverse.update { getLastUsedArtistsSortReverse() }
    }

    private fun getLastUsedAlbumArtistsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedAlbumArtistsSortBy, null)
        ?: SettingsDefaults.lastUsedAlbumArtistsSortBy

    fun setLastUsedAlbumArtistsSortBy(sortBy: AlbumArtistRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumArtistsSortBy, sortBy)
        }
        _lastUsedAlbumArtistsSortBy.update { getLastUsedAlbumArtistsSortBy() }
    }

    private fun getLastUsedAlbumArtistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumArtistsSortReverse, false)

    fun setLastUsedAlbumArtistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumArtistsSortReverse, reverse)
        }
        _lastUsedAlbumArtistsSortReverse.update { getLastUsedAlbumArtistsSortReverse() }
    }

    private fun getLastUsedAlbumsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedAlbumsSortBy, null)
        ?: SettingsDefaults.lastUsedAlbumsSortBy

    fun setLastUsedAlbumsSortBy(sortBy: AlbumRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumsSortBy, sortBy)
        }
        _lastUsedAlbumsSortBy.update { getLastUsedAlbumsSortBy() }
    }

    private fun getLastUsedAlbumsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumsSortReverse, false)

    fun setLastUsedAlbumsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumsSortReverse, reverse)
        }
        _lastUsedAlbumsSortReverse.update { getLastUsedAlbumsSortReverse() }
    }

    private fun getLastUsedGenresSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedGenresSortBy, null)
        ?: SettingsDefaults.lastUsedGenresSortBy

    fun setLastUsedGenresSortBy(sortBy: GenreRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedGenresSortBy, sortBy)
        }
        _lastUsedGenresSortBy.update { getLastUsedGenresSortBy() }
    }

    private fun getLastUsedGenresSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedGenresSortReverse, false)

    fun setLastUsedGenresSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedGenresSortReverse, reverse)
        }
        _lastUsedGenresSortReverse.update { getLastUsedGenresSortReverse() }
    }

    private fun getLastUsedBrowserSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedBrowserSortBy, null)
        ?: SettingsDefaults.lastUsedBrowserSortBy

    fun setLastUsedBrowserSortBy(sortBy: SongRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedBrowserSortBy, sortBy)
        }
        _lastUsedBrowserSortBy.update { getLastUsedBrowserSortBy() }
    }

    private fun getLastUsedBrowserSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedBrowserSortReverse, false)

    fun setLastUsedBrowserSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedBrowserSortReverse, reverse)
        }
        _lastUsedBrowserSortReverse.update { getLastUsedBrowserSortReverse() }
    }

    private fun getLastUsedBrowserPath() =
        getSharedPreferences().getString(SettingsKeys.lastUsedBrowserPath, null)
            ?.split("/")?.toList()

    fun setLastUsedBrowserPath(path: List<String>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.lastUsedBrowserPath, path.joinToString("/"))
        }
        _lastUsedBrowserPath.update { getLastUsedBrowserPath() }
    }

    private fun getLastUsedPlaylistsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedPlaylistsSortBy, null)
        ?: SettingsDefaults.lastUsedPlaylistsSortBy

    fun setLastUsedPlaylistsSortBy(sortBy: PlaylistRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedPlaylistsSortBy, sortBy)
        }
        _lastUsedPlaylistsSortBy.update { getLastUsedPlaylistsSortBy() }
    }

    private fun getLastUsedPlaylistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedPlaylistsSortReverse, false)

    fun setLastUsedPlaylistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedPlaylistsSortReverse, reverse)
        }
        _lastUsedPlaylistsSortReverse.update { getLastUsedPlaylistsSortReverse() }
    }

    private fun getLastUsedPlaylistSongsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedPlaylistSongsSortBy, null)
        ?: SettingsDefaults.lastUsedPlaylistSongsSortBy

    fun setLastUsedPlaylistSongsSortBy(sortBy: SongRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedPlaylistSongsSortBy, sortBy)
        }
        _lastUsedPlaylistSongsSortBy.update { getLastUsedPlaylistSongsSortBy() }
    }

    private fun getLastUsedPlaylistSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedPlaylistSongsSortReverse, false)

    fun setLastUsedPlaylistSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedPlaylistSongsSortReverse, reverse)
        }
        _lastUsedPlaylistSongsSortReverse.update { getLastUsedPlaylistSongsSortReverse() }
    }

    private fun getLastUsedAlbumSongsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedAlbumSongsSortBy, null)
        ?: SettingsDefaults.lastUsedAlbumSongsSortBy

    fun setLastUsedAlbumSongsSortBy(sortBy: SongRepository.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumSongsSortBy, sortBy)
        }
        _lastUsedAlbumSongsSortBy.update { getLastUsedAlbumSongsSortBy() }
    }

    private fun getLastUsedAlbumSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumSongsSortReverse, false)

    fun setLastUsedAlbumSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumSongsSortReverse, reverse)
        }
        _lastUsedAlbumSongsSortReverse.update { getLastUsedAlbumSongsSortReverse() }
    }

    private fun getLastUsedTreePathSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedTreePathSortBy, null)
        ?: SettingsDefaults.lastUsedTreePathSortBy

    fun setLastUsedTreePathSortBy(sortBy: StringListUtils.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedTreePathSortBy, sortBy)
        }
        _lastUsedTreePathSortBy.update { getLastUsedTreePathSortBy() }
    }

    private fun getLastUsedTreePathSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedTreePathSortReverse, false)

    fun setLastUsedTreePathSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedTreePathSortReverse, reverse)
        }
        _lastUsedTreePathSortReverse.update { getLastUsedTreePathSortReverse() }
    }

    private fun getLastUsedFoldersSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedFoldersSortBy, null)
        ?: SettingsDefaults.lastUsedFoldersSortBy

    fun setLastUsedFoldersSortBy(sortBy: StringListUtils.SortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedFoldersSortBy, sortBy)
        }
        _lastUsedFoldersSortBy.update { getLastUsedFoldersSortBy() }
    }

    private fun getLastUsedFoldersSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedFoldersSortReverse, false)

    fun setLastUsedFoldersSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedFoldersSortReverse, reverse)
        }
        _lastUsedFoldersSortReverse.update { getLastUsedFoldersSortReverse() }
    }

    fun getPreviousSongQueue() = getSharedPreferences()
        .getString(SettingsKeys.previousSongQueue, null)
        ?.let { RadioQueue.Serialized.parse(it) }

    fun setPreviousSongQueue(queue: RadioQueue.Serialized) {
        getSharedPreferences().edit {
            putString(SettingsKeys.previousSongQueue, queue.serialize())
        }
    }

    private fun getHomeLastTab() = getSharedPreferences()
        .getEnum(SettingsKeys.homeLastTab, null)
        ?: HomePages.Songs

    fun setHomeLastTab(value: HomePages) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.homeLastTab, value)
        }
        _homeLastTab.update { getHomeLastTab() }
    }

    private fun getLastDisabledTreePaths(): List<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.lastDisabledTreePaths, null)
        ?.toList() ?: emptyList()

    fun setLastDisabledTreePaths(paths: List<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.lastDisabledTreePaths, paths.toSet())
        }
        _lastDisabledTreePaths.update { getLastDisabledTreePaths() }
    }

    private fun getSongsFilterPattern() =
        getSharedPreferences().getString(SettingsKeys.songsFilterPattern, null)

    fun setSongsFilterPattern(value: String?) {
        getSharedPreferences().edit {
            putString(SettingsKeys.songsFilterPattern, value)
        }
        _songsFilterPattern.update { getSongsFilterPattern() }
    }

    private fun getCheckForUpdates() = getSharedPreferences().getBoolean(
        SettingsKeys.checkForUpdates,
        SettingsDefaults.checkForUpdates,
    )

    fun setCheckForUpdates(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.checkForUpdates, value)
        }
        _checkForUpdates.update { getCheckForUpdates() }
    }

    private fun getFadePlayback() = getSharedPreferences().getBoolean(
        SettingsKeys.fadePlayback,
        SettingsDefaults.fadePlayback,
    )

    fun setFadePlayback(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.fadePlayback, value)
        }
        _fadePlayback.update { getFadePlayback() }
    }

    private fun getRequireAudioFocus() = getSharedPreferences().getBoolean(
        SettingsKeys.requireAudioFocus,
        SettingsDefaults.requireAudioFocus,
    )

    fun setRequireAudioFocus(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.requireAudioFocus, value)
        }
        _requireAudioFocus.update { getRequireAudioFocus() }
    }

    private fun getIgnoreAudioFocusLoss() = getSharedPreferences().getBoolean(
        SettingsKeys.ignoreAudioFocusLoss,
        SettingsDefaults.ignoreAudioFocusLoss,
    )

    fun setIgnoreAudioFocusLoss(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.ignoreAudioFocusLoss, value)
        }
        _ignoreAudioFocusLoss.update { getIgnoreAudioFocusLoss() }
    }

    private fun getPlayOnHeadphonesConnect() = getSharedPreferences().getBoolean(
        SettingsKeys.playOnHeadphonesConnect,
        SettingsDefaults.playOnHeadphonesConnect,
    )

    fun setPlayOnHeadphonesConnect(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.playOnHeadphonesConnect, value)
        }
        _playOnHeadphonesConnect.update { getPlayOnHeadphonesConnect() }
    }

    private fun getPauseOnHeadphonesDisconnect() = getSharedPreferences().getBoolean(
        SettingsKeys.pauseOnHeadphonesDisconnect,
        SettingsDefaults.pauseOnHeadphonesDisconnect,
    )

    fun setPauseOnHeadphonesDisconnect(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.pauseOnHeadphonesDisconnect, value)
        }
        _pauseOnHeadphonesDisconnect.update { getPauseOnHeadphonesDisconnect() }
    }

    private fun getPrimaryColor() =
        getSharedPreferences().getString(SettingsKeys.primaryColor, null)

    fun setPrimaryColor(value: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.primaryColor, value)
        }
        _primaryColor.update { getPrimaryColor() }
    }

    private fun getFadePlaybackDuration() = getSharedPreferences().getFloat(
        SettingsKeys.fadePlaybackDuration,
        SettingsDefaults.fadePlaybackDuration,
    )

    fun setFadePlaybackDuration(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.fadePlaybackDuration, value)
        }
        _fadePlaybackDuration.update { getFadePlaybackDuration() }
    }

    private fun getHomeTabs() = getSharedPreferences()
        .getString(SettingsKeys.homeTabs, null)
        ?.split(",")
        ?.mapNotNull { parseEnumValue<HomePages>(it) }
        ?.toSet()
        ?: SettingsDefaults.homeTabs

    fun setHomeTabs(values: Set<HomePages>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.homeTabs, values.joinToString(",") { it.name })
        }
        _homeTabs.update { getHomeTabs() }
        if (getHomeLastTab() !in values) {
            setHomeLastTab(values.first())
        }
    }

    private fun getHomePageBottomBarLabelVisibility() = getSharedPreferences()
        .getEnum(SettingsKeys.homePageBottomBarLabelVisibility, null)
        ?: SettingsDefaults.homePageBottomBarLabelVisibility

    fun setHomePageBottomBarLabelVisibility(value: HomePageBottomBarLabelVisibility) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.homePageBottomBarLabelVisibility, value)
        }
        _homePageBottomBarLabelVisibility.update { getHomePageBottomBarLabelVisibility() }
    }

    private fun getForYouContents() = getSharedPreferences()
        .getString(SettingsKeys.forYouContents, null)
        ?.split(",")
        ?.mapNotNull { parseEnumValue<ForYou>(it) }
        ?.toSet()
        ?: SettingsDefaults.forYouContents

    fun setForYouContents(values: Set<ForYou>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.forYouContents, values.joinToString(",") { it.name })
        }
        _forYouContents.update { getForYouContents() }
    }

    private fun getBlacklistFolders(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.blacklistFolders, null)
        ?: SettingsDefaults.blacklistFolders

    fun setBlacklistFolders(values: Set<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.blacklistFolders, values)
        }
        _blacklistFolders.update { getBlacklistFolders() }
    }

    private fun getWhitelistFolders(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.whitelistFolders, null)
        ?: SettingsDefaults.whitelistFolders

    fun setWhitelistFolders(values: Set<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.whitelistFolders, values)
        }
        _whitelistFolders.update { getWhitelistFolders() }
    }

    private fun getReadIntroductoryMessage() = getSharedPreferences().getBoolean(
        SettingsKeys.readIntroductoryMessage,
        SettingsDefaults.readIntroductoryMessage,
    )

    fun setReadIntroductoryMessage(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.readIntroductoryMessage, value)
        }
        _readIntroductoryMessage.update { getReadIntroductoryMessage() }
    }

    private fun getNowPlayingAdditionalInfo() = getSharedPreferences().getBoolean(
        SettingsKeys.nowPlayingAdditionalInfo,
        SettingsDefaults.showNowPlayingAdditionalInfo,
    )

    fun showNowPlayingAdditionalInfo(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.nowPlayingAdditionalInfo, value)
        }
        _nowPlayingAdditionalInfo.update { getNowPlayingAdditionalInfo() }
    }

    private fun getNowPlayingSeekControls() = getSharedPreferences().getBoolean(
        SettingsKeys.nowPlayingSeekControls,
        SettingsDefaults.enableSeekControls,
    )

    fun setNowPlayingSeekControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.nowPlayingSeekControls, value)
        }
        _nowPlayingSeekControls.update { getNowPlayingSeekControls() }
    }

    private fun getSeekBackDuration() = getSharedPreferences().getInt(
        SettingsKeys.seekBackDuration,
        SettingsDefaults.seekBackDuration,
    )

    fun setSeekBackDuration(value: Int) {
        getSharedPreferences().edit {
            putInt(SettingsKeys.seekBackDuration, value)
        }
        _seekBackDuration.update { getSeekBackDuration() }
    }

    private fun getSeekForwardDuration() = getSharedPreferences().getInt(
        SettingsKeys.seekForwardDuration,
        SettingsDefaults.seekForwardDuration,
    )

    fun setSeekForwardDuration(value: Int) {
        getSharedPreferences().edit {
            putInt(SettingsKeys.seekForwardDuration, value)
        }
        _seekForwardDuration.update { getSeekForwardDuration() }
    }

    private fun getMiniPlayerTrackControls() = getSharedPreferences().getBoolean(
        SettingsKeys.miniPlayerTrackControls,
        SettingsDefaults.miniPlayerTrackControls,
    )

    fun setMiniPlayerTrackControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.miniPlayerTrackControls, value)
        }
        _miniPlayerTrackControls.update { getMiniPlayerTrackControls() }
    }

    private fun getMiniPlayerSeekControls() = getSharedPreferences().getBoolean(
        SettingsKeys.miniPlayerSeekControls,
        SettingsDefaults.miniPlayerSeekControls,
    )

    fun setMiniPlayerSeekControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.miniPlayerSeekControls, value)
        }
        _miniPlayerSeekControls.update { getMiniPlayerSeekControls() }
    }

    private fun getFontFamily() = getSharedPreferences().getString(SettingsKeys.fontFamily, null)
    fun setFontFamily(language: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.fontFamily, language)
        }
        _fontFamily.update { getFontFamily() }
    }

    private fun getNowPlayingControlsLayout() = getSharedPreferences()
        .getEnum(SettingsKeys.nowPlayingControlsLayout, null)
        ?: SettingsDefaults.nowPlayingControlsLayout

    fun setNowPlayingControlsLayout(controlsLayout: NowPlayingControlsLayout) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.nowPlayingControlsLayout, controlsLayout)
        }
        _nowPlayingControlsLayout.update { getNowPlayingControlsLayout() }
    }

    private fun getShowUpdateToast() = getSharedPreferences().getBoolean(
        SettingsKeys.showUpdateToast,
        SettingsDefaults.showUpdateToast,
    )

    fun setShowUpdateToast(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.showUpdateToast, value)
        }
        _showUpdateToast.update { getShowUpdateToast() }
    }

    private fun getFontScale() = getSharedPreferences().getFloat(
        SettingsKeys.fontScale,
        SettingsDefaults.fontScale,
    )

    fun setFontScale(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.fontScale, value)
        }
        _fontScale.update { getFontScale() }
    }

    private fun getContentScale() = getSharedPreferences().getFloat(
        SettingsKeys.contentScale,
        SettingsDefaults.contentScale,
    )

    fun setContentScale(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.contentScale, value)
        }
        _contentScale.update { getContentScale() }
    }

    private fun getNowPlayingLyricsLayout() = getSharedPreferences()
        .getEnum(SettingsKeys.nowPlayingLyricsLayout, null)
        ?: SettingsDefaults.nowPlayingLyricsLayout

    fun setNowPlayingLyricsLayout(value: NowPlayingLyricsLayout) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.nowPlayingLyricsLayout, value)
        }
        _nowPlayingLyricsLayout.update { getNowPlayingLyricsLayout() }
    }

    private fun getArtistTagSeparators(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.artistTagSeparators, null)
        ?: SettingsDefaults.artistTagSeparators

    fun setArtistTagSeparators(values: List<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.artistTagSeparators, values.toSet())
        }
        _artistTagSeparators.update { getArtistTagSeparators() }
    }

    private fun getGenreTagSeparators(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.genreTagSeparators, null)
        ?: SettingsDefaults.genreTagSeparators

    fun setGenreTagSeparators(values: List<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.genreTagSeparators, values.toSet())
        }
        _genreTagSeparators.update { getGenreTagSeparators() }
    }

    private fun getMiniPlayerTextMarquee() = getSharedPreferences().getBoolean(
        SettingsKeys.miniPlayerTextMarquee,
        SettingsDefaults.miniPlayerTextMarquee,
    )

    fun setMiniPlayerTextMarquee(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.miniPlayerTextMarquee, value)
        }
        _miniPlayerTextMarquee.update { getMiniPlayerTextMarquee() }
    }

    private fun getMediaFolders(): Set<Uri> = getSharedPreferences()
        .getStringSet(SettingsKeys.mediaFolders, null)
        ?.map { Uri.parse(it) }
        ?.toSet()
        ?: setOf()

    fun setMediaFolders(values: Set<Uri>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.mediaFolders, values.map { it.toString() }.toSet())
        }
        _mediaFolders.update { getMediaFolders() }
    }

    private fun getSharedPreferences() = symphony.applicationContext.getSharedPreferences(
        SettingsKeys.identifier,
        Context.MODE_PRIVATE,
    )
}
