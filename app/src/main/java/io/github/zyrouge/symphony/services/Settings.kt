package io.github.zyrouge.symphony.services

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.edit
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.AlbumArtistSortBy
import io.github.zyrouge.symphony.services.groove.AlbumSortBy
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.GenreSortBy
import io.github.zyrouge.symphony.services.groove.PathSortBy
import io.github.zyrouge.symphony.services.groove.PlaylistSortBy
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.ui.view.HomePageBottomBarLabelVisibility
import io.github.zyrouge.symphony.ui.view.HomePages
import io.github.zyrouge.symphony.ui.view.NowPlayingControlsLayout
import io.github.zyrouge.symphony.ui.view.NowPlayingLyricsLayout
import io.github.zyrouge.symphony.ui.view.home.ForYou
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SettingsKeys {
    const val identifier = "settings"
    const val themeMode = "theme_mode"
    const val language = "language"
    const val useMaterialYou = "material_you"
    const val lastUsedSongsSortBy = "last_used_song_sort_by"
    const val lastUsedSongsSortReverse = "last_used_song_sort_reverse"
    const val lastUsedArtistsSortBy = "last_used_artists_sort_by"
    const val lastUsedArtistsSortReverse = "last_used_artists_sort_reverse"
    const val lastUsedAlbumArtistsSortBy = "last_used_album_artists_sort_by"
    const val lastUsedAlbumArtistsSortReverse = "last_used_album_artists_sort_reverse"
    const val lastUsedAlbumsSortBy = "last_used_albums_sort_by"
    const val lastUsedAlbumsSortReverse = "last_used_albums_sort_reverse"
    const val lastUsedGenresSortBy = "last_used_genres_sort_by"
    const val lastUsedGenresSortReverse = "last_used_genres_sort_reverse"
    const val lastUsedBrowserSortBy = "last_used_folder_sort_by"
    const val lastUsedBrowserSortReverse = "last_used_folder_sort_reverse"
    const val lastUsedBrowserPath = "last_used_folder_path"
    const val lastUsedPlaylistsSortBy = "last_used_playlists_sort_by"
    const val lastUsedPlaylistsSortReverse = "last_used_playlists_sort_reverse"
    const val lastUsedPlaylistSongsSortBy = "last_used_playlist_songs_sort_by"
    const val lastUsedPlaylistSongsSortReverse = "last_used_playlist_songs_sort_reverse"
    const val lastUsedAlbumSongsSortBy = "last_used_album_songs_sort_by"
    const val lastUsedAlbumSongsSortReverse = "last_used_album_songs_sort_reverse"
    const val lastUsedTreePathSortBy = "last_used_tree_path_sort_by"
    const val lastUsedTreePathSortReverse = "last_used_tree_path_sort_reverse"
    const val lastUsedFoldersSortBy = "last_used_folders_sort_by"
    const val lastUsedFoldersSortReverse = "last_used_folders_sort_reverse"
    const val lastDisabledTreePaths = "last_disabled_tree_paths"
    const val previousSongQueue = "previous_song_queue"
    const val homeLastTab = "home_last_page"
    const val songsFilterPattern = "songs_filter_pattern"
    const val checkForUpdates = "check_for_updates"
    const val fadePlayback = "fade_playback"
    const val requireAudioFocus = "require_audio_focus"
    const val ignoreAudioFocusLoss = "ignore_audio_focus_loss"
    const val playOnHeadphonesConnect = "play_on_headphones_connect"
    const val pauseOnHeadphonesDisconnect = "pause_on_headphones_disconnect"
    const val primaryColor = "primary_color"
    const val fadePlaybackDuration = "fade_playback_duration"
    const val homeTabs = "home_tabs"
    const val homePageBottomBarLabelVisibility = "home_page_bottom_bar_label_visibility"
    const val forYouContents = "for_you_contents"
    const val blacklistFolders = "blacklist_folders"
    const val whitelistFolders = "whitelist_folders"
    const val readIntroductoryMessage = "introductory_message"
    const val nowPlayingAdditionalInfo = "show_now_playing_additional_info"
    const val nowPlayingSeekControls = "enable_seek_controls"
    const val seekBackDuration = "seek_back_duration"
    const val seekForwardDuration = "seek_forward_duration"
    const val miniPlayerTrackControls = "mini_player_extended_controls"
    const val miniPlayerSeekControls = "mini_player_seek_controls"
    const val fontFamily = "font_family"
    const val nowPlayingControlsLayout = "now_playing_controls_layout"
    const val showUpdateToast = "show_update_toast"
    const val fontScale = "font_scale"
    const val contentScale = "content_scale"
    const val nowPlayingLyricsLayout = "now_playing_lyrics_layout"
    const val artistTagSeparators = "artist_tag_separators"
    const val genreTagSeparators = "genre_tag_separators"
    const val miniPlayerTextMarquee = "mini_player_text_marquee"
}

object SettingsDefaults {
    val themeMode = ThemeMode.SYSTEM
    const val useMaterialYou = true
    val lastUsedSongSortBy = SongSortBy.TITLE
    val lastUsedArtistsSortBy = ArtistSortBy.ARTIST_NAME
    val lastUsedAlbumArtistsSortBy = AlbumArtistSortBy.ARTIST_NAME
    val lastUsedAlbumsSortBy = AlbumSortBy.ALBUM_NAME
    val lastUsedGenresSortBy = GenreSortBy.GENRE
    val lastUsedBrowserSortBy = SongSortBy.FILENAME
    val lastUsedPlaylistsSortBy = PlaylistSortBy.TITLE
    val lastUsedPlaylistSongsSortBy = SongSortBy.CUSTOM
    val lastUsedAlbumSongsSortBy = SongSortBy.TRACK_NUMBER
    val lastUsedTreePathSortBy = PathSortBy.NAME
    val lastUsedFoldersSortBy = PathSortBy.NAME
    const val checkForUpdates = false
    const val fadePlayback = false
    const val requireAudioFocus = true
    const val ignoreAudioFocusLoss = false
    const val playOnHeadphonesConnect = false
    const val pauseOnHeadphonesDisconnect = true
    const val fadePlaybackDuration = 1f
    val homeTabs = setOf(
        HomePages.ForYou,
        HomePages.Songs,
        HomePages.Albums,
        HomePages.Artists,
        HomePages.Playlists,
    )
    val homePageBottomBarLabelVisibility = HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE
    val forYouContents = setOf(
        ForYou.Albums,
        ForYou.Artists
    )
    val blacklistFolders = setOf<String>(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).path,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS).path,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).path,
    )
    val whitelistFolders = setOf<String>()
    const val readIntroductoryMessage = false
    const val showNowPlayingAdditionalInfo = true
    const val enableSeekControls = false
    const val seekBackDuration = 15
    const val seekForwardDuration = 30
    const val miniPlayerTrackControls = false
    const val miniPlayerSeekControls = false
    val nowPlayingControlsLayout = NowPlayingControlsLayout.Default
    const val showUpdateToast = true
    const val fontScale = 1.0f
    const val contentScale = 1.0f
    val nowPlayingLyricsLayout = NowPlayingLyricsLayout.ReplaceArtwork
    val artistTagSeparators = setOf(";", "/", ",", "+")
    val genreTagSeparators = setOf(";", "/", ",", "+")
    const val miniPlayerTextMarquee = true
}

@Suppress("MemberVisibilityCanBePrivate")
class SettingsManager(private val symphony: Symphony) {
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

    fun getThemeMode() = getSharedPreferences().getString(SettingsKeys.themeMode, null)
        ?.let { ThemeMode.valueOf(it) }
        ?: SettingsDefaults.themeMode

    fun setThemeMode(themeMode: ThemeMode) {
        getSharedPreferences().edit {
            putString(SettingsKeys.themeMode, themeMode.name)
        }
        _themeMode.updateUsingValue(getThemeMode())
    }

    fun getLanguage() = getSharedPreferences().getString(SettingsKeys.language, null)
    fun setLanguage(language: String?) {
        getSharedPreferences().edit {
            putString(SettingsKeys.language, language)
        }
        _language.updateUsingValue(getLanguage())
    }

    fun getUseMaterialYou() = getSharedPreferences().getBoolean(
        SettingsKeys.useMaterialYou,
        SettingsDefaults.useMaterialYou,
    )

    fun setUseMaterialYou(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.useMaterialYou, value)
        }
        _useMaterialYou.updateUsingValue(getUseMaterialYou())
    }

    fun getLastUsedSongsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedSongsSortBy, null)
        ?: SettingsDefaults.lastUsedSongSortBy

    fun setLastUsedSongsSortBy(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedSongsSortBy, sortBy)
        }
        _lastUsedSongsSortBy.updateUsingValue(getLastUsedSongsSortBy())
    }

    fun getLastUsedSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedSongsSortReverse, false)

    fun setLastUsedSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedSongsSortReverse, reverse)
        }
        _lastUsedSongsSortReverse.updateUsingValue(getLastUsedSongsSortReverse())
    }

    fun getLastUsedArtistsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedArtistsSortBy, null)
        ?: SettingsDefaults.lastUsedArtistsSortBy

    fun setLastUsedArtistsSortBy(sortBy: ArtistSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedArtistsSortBy, sortBy)
        }
        _lastUsedArtistsSortBy.updateUsingValue(getLastUsedArtistsSortBy())
    }

    fun getLastUsedArtistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedArtistsSortReverse, false)

    fun setLastUsedArtistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedArtistsSortReverse, reverse)
        }
        _lastUsedArtistsSortReverse.updateUsingValue(getLastUsedArtistsSortReverse())
    }

    fun getLastUsedAlbumArtistsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedAlbumArtistsSortBy, null)
        ?: SettingsDefaults.lastUsedAlbumArtistsSortBy

    fun setLastUsedAlbumArtistsSortBy(sortBy: AlbumArtistSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumArtistsSortBy, sortBy)
        }
        _lastUsedAlbumArtistsSortBy.updateUsingValue(getLastUsedAlbumArtistsSortBy())
    }

    fun getLastUsedAlbumArtistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumArtistsSortReverse, false)

    fun setLastUsedAlbumArtistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumArtistsSortReverse, reverse)
        }
        _lastUsedAlbumArtistsSortReverse.updateUsingValue(getLastUsedAlbumArtistsSortReverse())
    }

    fun getLastUsedAlbumsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedAlbumsSortBy, null)
        ?: SettingsDefaults.lastUsedAlbumsSortBy

    fun setLastUsedAlbumsSortBy(sortBy: AlbumSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumsSortBy, sortBy)
        }
        _lastUsedAlbumsSortBy.updateUsingValue(getLastUsedAlbumsSortBy())
    }

    fun getLastUsedAlbumsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumsSortReverse, false)

    fun setLastUsedAlbumsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumsSortReverse, reverse)
        }
        _lastUsedAlbumsSortReverse.updateUsingValue(getLastUsedAlbumsSortReverse())
    }

    fun getLastUsedGenresSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedGenresSortBy, null)
        ?: SettingsDefaults.lastUsedGenresSortBy

    fun setLastUsedGenresSortBy(sortBy: GenreSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedGenresSortBy, sortBy)
        }
        _lastUsedGenresSortBy.updateUsingValue(getLastUsedGenresSortBy())
    }

    fun getLastUsedGenresSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedGenresSortReverse, false)

    fun setLastUsedGenresSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedGenresSortReverse, reverse)
        }
        _lastUsedGenresSortReverse.updateUsingValue(getLastUsedGenresSortReverse())
    }

    fun getLastUsedBrowserSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedBrowserSortBy, null)
        ?: SettingsDefaults.lastUsedBrowserSortBy

    fun setLastUsedBrowserSortBy(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedBrowserSortBy, sortBy)
        }
        _lastUsedBrowserSortBy.updateUsingValue(getLastUsedBrowserSortBy())
    }

    fun getLastUsedBrowserSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedBrowserSortReverse, false)

    fun setLastUsedBrowserSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedBrowserSortReverse, reverse)
        }
        _lastUsedBrowserSortReverse.updateUsingValue(getLastUsedBrowserSortReverse())
    }

    fun getLastUsedBrowserPath() =
        getSharedPreferences().getString(SettingsKeys.lastUsedBrowserPath, null)
            ?.split("/")?.toList()

    fun setLastUsedBrowserPath(path: List<String>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.lastUsedBrowserPath, path.joinToString("/"))
        }
        _lastUsedBrowserPath.updateUsingValue(getLastUsedBrowserPath())
    }

    fun getLastUsedPlaylistsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedPlaylistsSortBy, null)
        ?: SettingsDefaults.lastUsedPlaylistsSortBy

    fun setLastUsedPlaylistsSortBy(sortBy: PlaylistSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedPlaylistsSortBy, sortBy)
        }
        _lastUsedPlaylistsSortBy.updateUsingValue(getLastUsedPlaylistsSortBy())
    }

    fun getLastUsedPlaylistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedPlaylistsSortReverse, false)

    fun setLastUsedPlaylistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedPlaylistsSortReverse, reverse)
        }
        _lastUsedPlaylistsSortReverse.updateUsingValue(getLastUsedPlaylistsSortReverse())
    }

    fun getLastUsedPlaylistSongsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedPlaylistSongsSortBy, null)
        ?: SettingsDefaults.lastUsedPlaylistSongsSortBy

    fun setLastUsedPlaylistSongsSortBy(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedPlaylistSongsSortBy, sortBy)
        }
        _lastUsedPlaylistSongsSortBy.updateUsingValue(getLastUsedPlaylistSongsSortBy())
    }

    fun getLastUsedPlaylistSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedPlaylistSongsSortReverse, false)

    fun setLastUsedPlaylistSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedPlaylistSongsSortReverse, reverse)
        }
        _lastUsedPlaylistSongsSortReverse.updateUsingValue(getLastUsedPlaylistSongsSortReverse())
    }

    fun getLastUsedAlbumSongsSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedAlbumSongsSortBy, null)
        ?: SettingsDefaults.lastUsedAlbumSongsSortBy

    fun setLastUsedAlbumSongsSortBy(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumSongsSortBy, sortBy)
        }
        _lastUsedAlbumSongsSortBy.updateUsingValue(getLastUsedAlbumSongsSortBy())
    }

    fun getLastUsedAlbumSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumSongsSortReverse, false)

    fun setLastUsedAlbumSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumSongsSortReverse, reverse)
        }
        _lastUsedAlbumSongsSortReverse.updateUsingValue(getLastUsedAlbumSongsSortReverse())
    }

    fun getLastUsedTreePathSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedTreePathSortBy, null)
        ?: SettingsDefaults.lastUsedTreePathSortBy

    fun setLastUsedTreePathSortBy(sortBy: PathSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedTreePathSortBy, sortBy)
        }
        _lastUsedTreePathSortBy.updateUsingValue(getLastUsedTreePathSortBy())
    }

    fun getLastUsedTreePathSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedTreePathSortReverse, false)

    fun setLastUsedTreePathSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedTreePathSortReverse, reverse)
        }
        _lastUsedTreePathSortReverse.updateUsingValue(getLastUsedTreePathSortReverse())
    }

    fun getLastUsedFoldersSortBy() = getSharedPreferences()
        .getEnum(SettingsKeys.lastUsedFoldersSortBy, null)
        ?: SettingsDefaults.lastUsedFoldersSortBy

    fun setLastUsedFoldersSortBy(sortBy: PathSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedFoldersSortBy, sortBy)
        }
        _lastUsedFoldersSortBy.updateUsingValue(getLastUsedFoldersSortBy())
    }

    fun getLastUsedFoldersSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedFoldersSortReverse, false)

    fun setLastUsedFoldersSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedFoldersSortReverse, reverse)
        }
        _lastUsedFoldersSortReverse.updateUsingValue(getLastUsedFoldersSortReverse())
    }

    fun getPreviousSongQueue() = getSharedPreferences()
        .getString(SettingsKeys.previousSongQueue, null)
        ?.let { RadioQueue.Serialized.parse(it) }

    fun setPreviousSongQueue(queue: RadioQueue.Serialized) {
        getSharedPreferences().edit {
            putString(SettingsKeys.previousSongQueue, queue.serialize())
        }
    }

    fun getHomeLastTab() = getSharedPreferences()
        .getEnum(SettingsKeys.homeLastTab, null)
        ?: HomePages.Songs

    fun setHomeLastTab(value: HomePages) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.homeLastTab, value)
        }
        _homeLastTab.updateUsingValue(getHomeLastTab())
    }

    fun getLastDisabledTreePaths(): List<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.lastDisabledTreePaths, null)
        ?.toList() ?: emptyList()

    fun setLastDisabledTreePaths(paths: List<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.lastDisabledTreePaths, paths.toSet())
        }
        _lastDisabledTreePaths.updateUsingValue(getLastDisabledTreePaths())
    }

    fun getSongsFilterPattern() =
        getSharedPreferences().getString(SettingsKeys.songsFilterPattern, null)

    fun setSongsFilterPattern(value: String?) {
        getSharedPreferences().edit {
            putString(SettingsKeys.songsFilterPattern, value)
        }
        _songsFilterPattern.updateUsingValue(getSongsFilterPattern())
    }

    fun getCheckForUpdates() = getSharedPreferences().getBoolean(
        SettingsKeys.checkForUpdates,
        SettingsDefaults.checkForUpdates,
    )

    fun setCheckForUpdates(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.checkForUpdates, value)
        }
        _checkForUpdates.updateUsingValue(getCheckForUpdates())
    }

    fun getFadePlayback() = getSharedPreferences().getBoolean(
        SettingsKeys.fadePlayback,
        SettingsDefaults.fadePlayback,
    )

    fun setFadePlayback(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.fadePlayback, value)
        }
        _fadePlayback.updateUsingValue(getFadePlayback())
    }

    fun getRequireAudioFocus() = getSharedPreferences().getBoolean(
        SettingsKeys.requireAudioFocus,
        SettingsDefaults.requireAudioFocus,
    )

    fun setRequireAudioFocus(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.requireAudioFocus, value)
        }
        _requireAudioFocus.updateUsingValue(getRequireAudioFocus())
    }

    fun getIgnoreAudioFocusLoss() = getSharedPreferences().getBoolean(
        SettingsKeys.ignoreAudioFocusLoss,
        SettingsDefaults.ignoreAudioFocusLoss,
    )

    fun setIgnoreAudioFocusLoss(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.ignoreAudioFocusLoss, value)
        }
        _ignoreAudioFocusLoss.updateUsingValue(getIgnoreAudioFocusLoss())
    }

    fun getPlayOnHeadphonesConnect() = getSharedPreferences().getBoolean(
        SettingsKeys.playOnHeadphonesConnect,
        SettingsDefaults.playOnHeadphonesConnect,
    )

    fun setPlayOnHeadphonesConnect(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.playOnHeadphonesConnect, value)
        }
        _playOnHeadphonesConnect.updateUsingValue(getPlayOnHeadphonesConnect())
    }

    fun getPauseOnHeadphonesDisconnect() = getSharedPreferences().getBoolean(
        SettingsKeys.pauseOnHeadphonesDisconnect,
        SettingsDefaults.pauseOnHeadphonesDisconnect,
    )

    fun setPauseOnHeadphonesDisconnect(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.pauseOnHeadphonesDisconnect, value)
        }
        _pauseOnHeadphonesDisconnect.updateUsingValue(getPauseOnHeadphonesDisconnect())
    }

    fun getPrimaryColor() = getSharedPreferences().getString(SettingsKeys.primaryColor, null)

    fun setPrimaryColor(value: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.primaryColor, value)
        }
        _primaryColor.updateUsingValue(getPrimaryColor())
    }

    fun getFadePlaybackDuration() = getSharedPreferences().getFloat(
        SettingsKeys.fadePlaybackDuration,
        SettingsDefaults.fadePlaybackDuration,
    )

    fun setFadePlaybackDuration(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.fadePlaybackDuration, value)
        }
        _fadePlaybackDuration.updateUsingValue(getFadePlaybackDuration())
    }

    fun getHomeTabs() = getSharedPreferences()
        .getString(SettingsKeys.homeTabs, null)
        ?.split(",")
        ?.mapNotNull { parseEnumValue<HomePages>(it) }
        ?.toSet()
        ?: SettingsDefaults.homeTabs

    fun setHomeTabs(values: Set<HomePages>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.homeTabs, values.joinToString(",") { it.name })
        }
        _homeTabs.updateUsingValue(getHomeTabs())
        if (getHomeLastTab() !in values) {
            setHomeLastTab(values.first())
        }
    }

    fun getHomePageBottomBarLabelVisibility() = getSharedPreferences()
        .getEnum(SettingsKeys.homePageBottomBarLabelVisibility, null)
        ?: SettingsDefaults.homePageBottomBarLabelVisibility

    fun setHomePageBottomBarLabelVisibility(value: HomePageBottomBarLabelVisibility) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.homePageBottomBarLabelVisibility, value)
        }
        _homePageBottomBarLabelVisibility.updateUsingValue(getHomePageBottomBarLabelVisibility())
    }

    fun getForYouContents() = getSharedPreferences()
        .getString(SettingsKeys.forYouContents, null)
        ?.split(",")
        ?.mapNotNull { parseEnumValue<ForYou>(it) }
        ?.toSet()
        ?: SettingsDefaults.forYouContents

    fun setForYouContents(values: Set<ForYou>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.forYouContents, values.joinToString(",") { it.name })
        }
        _forYouContents.updateUsingValue(getForYouContents())
    }

    fun getBlacklistFolders(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.blacklistFolders, null)
        ?: SettingsDefaults.blacklistFolders

    fun setBlacklistFolders(values: Set<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.blacklistFolders, values)
        }
        _blacklistFolders.updateUsingValue(getBlacklistFolders())
    }

    fun getWhitelistFolders(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.whitelistFolders, null)
        ?: SettingsDefaults.whitelistFolders

    fun setWhitelistFolders(values: Set<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.whitelistFolders, values)
        }
        _whitelistFolders.updateUsingValue(getWhitelistFolders())
    }

    fun getReadIntroductoryMessage() = getSharedPreferences().getBoolean(
        SettingsKeys.readIntroductoryMessage,
        SettingsDefaults.readIntroductoryMessage,
    )

    fun setReadIntroductoryMessage(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.readIntroductoryMessage, value)
        }
        _readIntroductoryMessage.updateUsingValue(getReadIntroductoryMessage())
    }

    fun getNowPlayingAdditionalInfo() = getSharedPreferences().getBoolean(
        SettingsKeys.nowPlayingAdditionalInfo,
        SettingsDefaults.showNowPlayingAdditionalInfo,
    )

    fun showNowPlayingAdditionalInfo(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.nowPlayingAdditionalInfo, value)
        }
        _nowPlayingAdditionalInfo.updateUsingValue(getNowPlayingAdditionalInfo())
    }

    fun getNowPlayingSeekControls() = getSharedPreferences().getBoolean(
        SettingsKeys.nowPlayingSeekControls,
        SettingsDefaults.enableSeekControls,
    )

    fun setNowPlayingSeekControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.nowPlayingSeekControls, value)
        }
        _nowPlayingSeekControls.updateUsingValue(getNowPlayingSeekControls())
    }

    fun getSeekBackDuration() = getSharedPreferences().getInt(
        SettingsKeys.seekBackDuration,
        SettingsDefaults.seekBackDuration,
    )

    fun setSeekBackDuration(value: Int) {
        getSharedPreferences().edit {
            putInt(SettingsKeys.seekBackDuration, value)
        }
        _seekBackDuration.updateUsingValue(getSeekBackDuration())
    }

    fun getSeekForwardDuration() = getSharedPreferences().getInt(
        SettingsKeys.seekForwardDuration,
        SettingsDefaults.seekForwardDuration,
    )

    fun setSeekForwardDuration(value: Int) {
        getSharedPreferences().edit {
            putInt(SettingsKeys.seekForwardDuration, value)
        }
        _seekForwardDuration.updateUsingValue(getSeekForwardDuration())
    }

    fun getMiniPlayerTrackControls() = getSharedPreferences().getBoolean(
        SettingsKeys.miniPlayerTrackControls,
        SettingsDefaults.miniPlayerTrackControls,
    )

    fun setMiniPlayerTrackControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.miniPlayerTrackControls, value)
        }
        _miniPlayerTrackControls.updateUsingValue(getMiniPlayerTrackControls())
    }

    fun getMiniPlayerSeekControls() = getSharedPreferences().getBoolean(
        SettingsKeys.miniPlayerSeekControls,
        SettingsDefaults.miniPlayerSeekControls,
    )

    fun setMiniPlayerSeekControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.miniPlayerSeekControls, value)
        }
        _miniPlayerSeekControls.updateUsingValue(getMiniPlayerSeekControls())
    }

    fun getFontFamily() = getSharedPreferences().getString(SettingsKeys.fontFamily, null)
    fun setFontFamily(language: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.fontFamily, language)
        }
        _fontFamily.updateUsingValue(getFontFamily())
    }

    fun getNowPlayingControlsLayout() = getSharedPreferences()
        .getEnum(SettingsKeys.nowPlayingControlsLayout, null)
        ?: SettingsDefaults.nowPlayingControlsLayout

    fun setNowPlayingControlsLayout(controlsLayout: NowPlayingControlsLayout) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.nowPlayingControlsLayout, controlsLayout)
        }
        _nowPlayingControlsLayout.updateUsingValue(getNowPlayingControlsLayout())
    }

    fun getShowUpdateToast() = getSharedPreferences().getBoolean(
        SettingsKeys.showUpdateToast,
        SettingsDefaults.showUpdateToast,
    )

    fun setShowUpdateToast(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.showUpdateToast, value)
        }
        _showUpdateToast.updateUsingValue(getShowUpdateToast())
    }

    fun getFontScale() = getSharedPreferences().getFloat(
        SettingsKeys.fontScale,
        SettingsDefaults.fontScale,
    )

    fun setFontScale(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.fontScale, value)
        }
        _fontScale.updateUsingValue(getFontScale())
    }

    fun getContentScale() = getSharedPreferences().getFloat(
        SettingsKeys.contentScale,
        SettingsDefaults.contentScale,
    )

    fun setContentScale(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.contentScale, value)
        }
        _contentScale.updateUsingValue(getContentScale())
    }

    fun getNowPlayingLyricsLayout() = getSharedPreferences()
        .getEnum(SettingsKeys.nowPlayingLyricsLayout, null)
        ?: SettingsDefaults.nowPlayingLyricsLayout

    fun setNowPlayingLyricsLayout(value: NowPlayingLyricsLayout) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.nowPlayingLyricsLayout, value)
        }
        _nowPlayingLyricsLayout.updateUsingValue(getNowPlayingLyricsLayout())
    }

    fun getArtistTagSeparators(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.artistTagSeparators, null)
        ?: SettingsDefaults.artistTagSeparators

    fun setArtistTagSeparators(values: List<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.artistTagSeparators, values.toSet())
        }
        _artistTagSeparators.updateUsingValue(getArtistTagSeparators())
    }

    fun getGenreTagSeparators(): Set<String> = getSharedPreferences()
        .getStringSet(SettingsKeys.genreTagSeparators, null)
        ?: SettingsDefaults.genreTagSeparators

    fun setGenreTagSeparators(values: List<String>) {
        getSharedPreferences().edit {
            putStringSet(SettingsKeys.genreTagSeparators, values.toSet())
        }
        _genreTagSeparators.updateUsingValue(getGenreTagSeparators())
    }

    fun getMiniPlayerTextMarquee() = getSharedPreferences().getBoolean(
        SettingsKeys.miniPlayerTextMarquee,
        SettingsDefaults.miniPlayerTextMarquee,
    )

    fun setMiniPlayerTextMarquee(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.miniPlayerTextMarquee, value)
        }
        _miniPlayerTextMarquee.updateUsingValue(getMiniPlayerTextMarquee())
    }

    private fun getSharedPreferences() = symphony.applicationContext.getSharedPreferences(
        SettingsKeys.identifier,
        Context.MODE_PRIVATE,
    )
}

private inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T?,
): T? {
    var result = defaultValue
    getString(key, null)?.let { value ->
        result = parseEnumValue<T>(value)
    }
    return result
}

private inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T?,
) = putString(key, value?.name)

private inline fun <reified T : Enum<T>> parseEnumValue(value: String): T? =
    T::class.java.enumConstants?.find { it.name == value }

private fun <T> MutableStateFlow<T>.updateUsingValue(value: T) = update {
    value
}
