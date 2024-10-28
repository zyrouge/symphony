package io.github.zyrouge.symphony.services.settings

import android.os.Environment
import io.github.zyrouge.symphony.services.groove.repositories.AlbumArtistRepository
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import io.github.zyrouge.symphony.services.groove.repositories.ArtistRepository
import io.github.zyrouge.symphony.services.groove.repositories.GenreRepository
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.ui.view.HomePageBottomBarLabelVisibility
import io.github.zyrouge.symphony.ui.view.HomePages
import io.github.zyrouge.symphony.ui.view.NowPlayingControlsLayout
import io.github.zyrouge.symphony.ui.view.NowPlayingLyricsLayout
import io.github.zyrouge.symphony.ui.view.home.ForYou
import io.github.zyrouge.symphony.utils.StringListUtils

@Suppress("ConstPropertyName")
data object SettingsDefaults {
    val themeMode = ThemeMode.SYSTEM
    const val useMaterialYou = true
    val lastUsedSongSortBy = SongRepository.SortBy.TITLE
    val lastUsedArtistsSortBy = ArtistRepository.SortBy.ARTIST_NAME
    val lastUsedAlbumArtistsSortBy = AlbumArtistRepository.SortBy.ARTIST_NAME
    val lastUsedAlbumsSortBy = AlbumRepository.SortBy.ALBUM_NAME
    val lastUsedGenresSortBy = GenreRepository.SortBy.GENRE
    val lastUsedBrowserSortBy = SongRepository.SortBy.FILENAME
    val lastUsedPlaylistsSortBy = PlaylistRepository.SortBy.TITLE
    val lastUsedPlaylistSongsSortBy = SongRepository.SortBy.CUSTOM
    val lastUsedAlbumSongsSortBy = SongRepository.SortBy.TRACK_NUMBER
    val lastUsedTreePathSortBy = StringListUtils.SortBy.NAME
    val lastUsedFoldersSortBy = StringListUtils.SortBy.NAME
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
