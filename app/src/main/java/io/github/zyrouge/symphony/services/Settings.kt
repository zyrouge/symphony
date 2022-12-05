package io.github.zyrouge.symphony.services

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.AlbumSortBy
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.GenreSortBy
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.view.HomePages
import io.github.zyrouge.symphony.utils.Eventer

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    BLACK,
}

object SettingsKeys {
    const val identifier = "settings"
    const val themeMode = "theme_mode"
    const val language = "language"
    const val materialYou = "material_you"
    const val lastUsedSongsSortBy = "last_used_song_sort_by"
    const val lastUsedSongsSortReverse = "last_used_song_sort_reverse"
    const val lastUsedArtistsSortBy = "last_used_artists_sort_by"
    const val lastUsedArtistsSortReverse = "last_used_artists_sort_reverse"
    const val lastUsedAlbumsSortBy = "last_used_albums_sort_by"
    const val lastUsedAlbumsSortReverse = "last_used_albums_sort_reverse"
    const val lastUsedGenresSortBy = "last_used_genres_sort_by"
    const val lastUsedGenresSortReverse = "last_used_genres_sort_reverse"
    const val lastUsedFolderSortBy = "last_used_folder_sort_by"
    const val lastUsedFolderSortReverse = "last_used_folder_sort_reverse"
    const val lastUsedFolderPath = "last_used_folder_path"
    const val previousSongQueue = "previous_song_queue"
    const val home_last_tab = "home_last_tab"
    const val songs_filter_pattern = "songs_filter_pattern"
    const val check_for_updates = "check_for_updates"
    const val mini_player_extended_controls = "mini_player_extended_controls"
    const val fade_playback = "fade_playback"
    const val require_audio_focus = "require_audio_focus"
    const val ignore_audio_focus_loss = "ignore_audio_focus_loss"
    const val play_on_headphones_connect = "play_on_headphones_connect"
    const val pause_on_headphones_disconnect = "pause_on_headphones_disconnect"
    const val primary_color = "primary_color"
    const val fade_playback_duration = "fade_playback_duration"
    const val home_tabs = "home_tabs"
}

data class SettingsData(
    val themeMode: ThemeMode,
    val language: String?,
    val useMaterialYou: Boolean,
    val songsFilterPattern: String?,
    val checkForUpdates: Boolean,
    val miniPlayerExtendedControls: Boolean,
    val fadePlayback: Boolean,
    val requireAudioFocus: Boolean,
    val ignoreAudioFocusLoss: Boolean,
    val playOnHeadphonesConnect: Boolean,
    val pauseOnHeadphonesDisconnect: Boolean,
    val primaryColor: String?,
    val fadePlaybackDuration: Float,
    val homeTabs: Set<HomePages>,
)

object SettingsDataDefaults {
    val themeMode = ThemeMode.SYSTEM
    const val useMaterialYou = true
    const val checkForUpdates = true
    const val miniPlayerExtendedControls = false
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
        HomePages.Artists
    )
}

class SettingsManager(private val symphony: Symphony) {
    val onChange = Eventer<String>()

    fun getSettings() = SettingsData(
        themeMode = getThemeMode(),
        language = getLanguage(),
        useMaterialYou = getUseMaterialYou(),
        songsFilterPattern = getSongsFilterPattern(),
        checkForUpdates = getCheckForUpdates(),
        miniPlayerExtendedControls = getMiniPlayerExtendedControls(),
        fadePlayback = getFadePlayback(),
        requireAudioFocus = getRequireAudioFocus(),
        ignoreAudioFocusLoss = getIgnoreAudioFocusLoss(),
        playOnHeadphonesConnect = getPlayOnHeadphonesConnect(),
        pauseOnHeadphonesDisconnect = getPauseOnHeadphonesDisconnect(),
        primaryColor = getPrimaryColor(),
        fadePlaybackDuration = getFadePlaybackDuration(),
        homeTabs = getHomeTabs(),
    )

    fun getThemeMode() = getSharedPreferences().getString(SettingsKeys.themeMode, null)
        ?.let { ThemeMode.valueOf(it) }
        ?: SettingsDataDefaults.themeMode

    fun setThemeMode(themeMode: ThemeMode) {
        getSharedPreferences().edit {
            putString(SettingsKeys.themeMode, themeMode.name)
        }
        onChange.dispatch(SettingsKeys.themeMode)
    }

    fun getLanguage() = getSharedPreferences().getString(SettingsKeys.language, null)
    fun setLanguage(language: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.language, language)
        }
        onChange.dispatch(SettingsKeys.language)
    }

    fun getUseMaterialYou() = getSharedPreferences().getBoolean(
        SettingsKeys.materialYou,
        SettingsDataDefaults.useMaterialYou,
    )

    fun setUseMaterialYou(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.materialYou, value)
        }
        onChange.dispatch(SettingsKeys.materialYou)
    }

    fun getLastUsedSongsSortBy() =
        getSharedPreferences().getEnum<SongSortBy>(SettingsKeys.lastUsedSongsSortBy, null)

    fun setLastUsedSongsSortBy(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedSongsSortBy, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedSongsSortBy)
    }

    fun getLastUsedSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedSongsSortReverse, false)

    fun setLastUsedSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedSongsSortReverse, reverse)
        }
        onChange.dispatch(SettingsKeys.lastUsedSongsSortReverse)
    }

    fun getLastUsedArtistsSortBy() =
        getSharedPreferences().getEnum<ArtistSortBy>(SettingsKeys.lastUsedArtistsSortBy, null)

    fun setLastUsedArtistsSortBy(sortBy: ArtistSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedArtistsSortBy, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedArtistsSortBy)
    }

    fun getLastUsedArtistsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedArtistsSortReverse, false)

    fun setLastUsedArtistsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedArtistsSortReverse, reverse)
        }
        onChange.dispatch(SettingsKeys.lastUsedArtistsSortReverse)
    }

    fun getLastUsedAlbumsSortBy() =
        getSharedPreferences().getEnum<AlbumSortBy>(SettingsKeys.lastUsedAlbumsSortBy, null)

    fun setLastUsedAlbumsSortBy(sortBy: AlbumSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumsSortBy, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedAlbumsSortBy)
    }

    fun getLastUsedAlbumsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedAlbumsSortReverse, false)

    fun setLastUsedAlbumsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedAlbumsSortReverse, reverse)
        }
        onChange.dispatch(SettingsKeys.lastUsedAlbumsSortReverse)
    }

    fun getLastUsedGenresSortBy() =
        getSharedPreferences().getEnum<GenreSortBy>(SettingsKeys.lastUsedGenresSortBy, null)

    fun setLastUsedGenresSortBy(sortBy: GenreSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedGenresSortBy, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedGenresSortBy)
    }

    fun getLastUsedGenresSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedGenresSortReverse, false)

    fun setLastUsedGenresSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedGenresSortReverse, reverse)
        }
        onChange.dispatch(SettingsKeys.lastUsedGenresSortReverse)
    }

    fun getLastUsedFolderSortBy() =
        getSharedPreferences().getEnum<SongSortBy>(SettingsKeys.lastUsedFolderSortBy, null)

    fun setLastUsedFolderSortBy(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedFolderSortBy, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedFolderSortBy)
    }

    fun getLastUsedFolderSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedFolderSortReverse, false)

    fun setLastUsedFolderSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedFolderSortReverse, reverse)
        }
        onChange.dispatch(SettingsKeys.lastUsedFolderSortReverse)
    }

    fun getLastUsedFolderPath() =
        getSharedPreferences().getString(SettingsKeys.lastUsedFolderPath, null)?.split("/")

    fun setLastUsedFolderPath(path: List<String>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.lastUsedFolderPath, path.joinToString("/"))
        }
        onChange.dispatch(SettingsKeys.lastUsedFolderPath)
    }

    fun getPreviousSongQueue(): RadioQueue.Serialized? {
        val raw = getSharedPreferences().getString(SettingsKeys.previousSongQueue, null)
        return raw?.let { RadioQueue.Serialized.parse(it) }
    }

    fun setPreviousSongQueue(queue: RadioQueue.Serialized) {
        getSharedPreferences().edit {
            putString(SettingsKeys.previousSongQueue, queue.serialize())
        }
        onChange.dispatch(SettingsKeys.previousSongQueue)
    }

    fun getHomeLastTab() = getSharedPreferences().getString(SettingsKeys.home_last_tab, null)
    fun setHomeLastTab(value: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.home_last_tab, value)
        }
        onChange.dispatch(SettingsKeys.home_last_tab)
    }

    fun getSongsFilterPattern() =
        getSharedPreferences().getString(SettingsKeys.songs_filter_pattern, null)

    fun setSongsFilterPattern(value: String?) {
        getSharedPreferences().edit {
            putString(SettingsKeys.songs_filter_pattern, value)
        }
        onChange.dispatch(SettingsKeys.songs_filter_pattern)
    }

    fun getCheckForUpdates() =
        getSharedPreferences().getBoolean(
            SettingsKeys.check_for_updates,
            SettingsDataDefaults.checkForUpdates,
        )

    fun setCheckForUpdates(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.check_for_updates, value)
        }
        onChange.dispatch(SettingsKeys.check_for_updates)
    }

    fun getMiniPlayerExtendedControls() =
        getSharedPreferences().getBoolean(
            SettingsKeys.mini_player_extended_controls,
            SettingsDataDefaults.miniPlayerExtendedControls,
        )

    fun setMiniPlayerExtendedControls(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.mini_player_extended_controls, value)
        }
        onChange.dispatch(SettingsKeys.mini_player_extended_controls)
    }

    fun getFadePlayback() =
        getSharedPreferences().getBoolean(
            SettingsKeys.fade_playback,
            SettingsDataDefaults.fadePlayback,
        )

    fun setFadePlayback(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.fade_playback, value)
        }
        onChange.dispatch(SettingsKeys.fade_playback)
    }

    fun getRequireAudioFocus() =
        getSharedPreferences().getBoolean(
            SettingsKeys.require_audio_focus,
            SettingsDataDefaults.requireAudioFocus,
        )

    fun setRequireAudioFocus(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.require_audio_focus, value)
        }
        onChange.dispatch(SettingsKeys.require_audio_focus)
    }

    fun getIgnoreAudioFocusLoss() =
        getSharedPreferences().getBoolean(
            SettingsKeys.ignore_audio_focus_loss,
            SettingsDataDefaults.ignoreAudioFocusLoss,
        )

    fun setIgnoreAudioFocusLoss(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.ignore_audio_focus_loss, value)
        }
        onChange.dispatch(SettingsKeys.ignore_audio_focus_loss)
    }

    fun getPlayOnHeadphonesConnect() =
        getSharedPreferences().getBoolean(
            SettingsKeys.play_on_headphones_connect,
            SettingsDataDefaults.playOnHeadphonesConnect,
        )

    fun setPlayOnHeadphonesConnect(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.play_on_headphones_connect, value)
        }
        onChange.dispatch(SettingsKeys.play_on_headphones_connect)
    }

    fun getPauseOnHeadphonesDisconnect() =
        getSharedPreferences().getBoolean(
            SettingsKeys.pause_on_headphones_disconnect,
            SettingsDataDefaults.pauseOnHeadphonesDisconnect,
        )

    fun setPauseOnHeadphonesDisconnect(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.pause_on_headphones_disconnect, value)
        }
        onChange.dispatch(SettingsKeys.pause_on_headphones_disconnect)
    }

    fun getPrimaryColor() =
        getSharedPreferences().getString(SettingsKeys.primary_color, null)

    fun setPrimaryColor(value: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.primary_color, value)
        }
        onChange.dispatch(SettingsKeys.primary_color)
    }

    fun getFadePlaybackDuration() =
        getSharedPreferences().getFloat(
            SettingsKeys.fade_playback_duration,
            SettingsDataDefaults.fadePlaybackDuration,
        )

    fun setFadePlaybackDuration(value: Float) {
        getSharedPreferences().edit {
            putFloat(SettingsKeys.fade_playback_duration, value)
        }
        onChange.dispatch(SettingsKeys.fade_playback_duration)
    }

    fun getHomeTabs() = getSharedPreferences()
        .getString(SettingsKeys.home_tabs, null)
        ?.split(",")
        ?.mapNotNull { parseEnumValue<HomePages>(it) }
        ?.toSet()
        ?: SettingsDataDefaults.homeTabs

    fun setHomeTabs(tabs: Set<HomePages>) {
        getSharedPreferences().edit {
            putString(SettingsKeys.home_tabs, tabs.joinToString(",") { it.name })
        }
        onChange.dispatch(SettingsKeys.home_tabs)
    }

    private fun getSharedPreferences() =
        symphony.applicationContext.getSharedPreferences(
            SettingsKeys.identifier,
            Context.MODE_PRIVATE
        )
}

private inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T?
): T? {
    var result = defaultValue
    getString(key, null)?.let { value ->
        result = parseEnumValue<T>(value)
    }
    return result
}

private inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T?
) = putString(key, value?.name)

private inline fun <reified T : Enum<T>> parseEnumValue(value: String): T? =
    T::class.java.enumConstants?.find { it.name == value }
