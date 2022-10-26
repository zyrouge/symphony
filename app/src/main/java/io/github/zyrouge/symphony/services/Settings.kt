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
    const val lastUsedSongsSort = "last_used_song_sort"
    const val lastUsedArtistsSort = "last_used_artists_sort"
    const val lastUsedAlbumsSort = "last_used_albums_sort"
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

    fun getLastUsedSongsSort() =
        getSharedPreferences().getEnum<SongSortBy>(SettingsKeys.lastUsedSongsSort, null)

    fun setLastUsedSongsSort(sortBy: SongSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedSongsSort, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedSongsSort)
    }

    fun getLastUsedArtistsSort() =
        getSharedPreferences().getEnum<ArtistSortBy>(SettingsKeys.lastUsedArtistsSort, null)

    fun setLastUsedArtistsSort(sortBy: ArtistSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedArtistsSort, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedArtistsSort)
    }

    fun getLastUsedAlbumsSort() =
        getSharedPreferences().getEnum<AlbumSortBy>(SettingsKeys.lastUsedAlbumsSort, null)

    fun setLastUsedAlbumsSort(sortBy: AlbumSortBy) {
        getSharedPreferences().edit {
            putEnum(SettingsKeys.lastUsedAlbumsSort, sortBy)
        }
        onChange.dispatch(SettingsKeys.lastUsedAlbumsSort)
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
