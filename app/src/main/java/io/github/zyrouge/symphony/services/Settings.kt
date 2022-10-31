package io.github.zyrouge.symphony.services

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.AlbumSortBy
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.utils.Eventer

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    BLACK;

    companion object {
        val defaultThemeMode = SYSTEM
        val all = values()
    }
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
}

data class SettingsData(
    val themeMode: ThemeMode,
    val language: String?,
    val useMaterialYou: Boolean
)

class SettingsManager(private val symphony: Symphony) {
    val onChange = Eventer<String>()

    fun getSettings() = SettingsData(
        themeMode = getThemeMode(),
        language = getLanguage(),
        useMaterialYou = getUseMaterialYou()
    )

    fun getThemeMode() = getSharedPreferences().getString(SettingsKeys.themeMode, null)
        ?.let { ThemeMode.valueOf(it) }
        ?: ThemeMode.defaultThemeMode

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

    fun getUseMaterialYou() = getSharedPreferences().getBoolean(SettingsKeys.materialYou, true)
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

    fun getLastUsedSongsSortReverse() =
        getSharedPreferences().getBoolean(SettingsKeys.lastUsedSongsSortReverse, false)

    fun setLastUsedSongsSortReverse(reverse: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.lastUsedSongsSortReverse, reverse)
        }
        onChange.dispatch(SettingsKeys.lastUsedSongsSortReverse)
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
        T::class.java.enumConstants?.forEach {
            if (it.name == value) {
                result = it
            }
        }
    }
    return result
}

private inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T?
) = putString(key, value?.name)
