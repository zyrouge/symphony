package io.github.zyrouge.symphony.services

import android.content.Context
import androidx.core.content.edit
import io.github.zyrouge.symphony.Symphony

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    BLACK;

    companion object {
        val defaultThemeMode = SYSTEM
        val all = ThemeMode.values()
    }
}

object SettingsKeys {
    const val identifier = "settings"
    const val themeMode = "theme_mode"
    const val language = "language"
    const val materialYou = "material_you"
}

class SettingsManager(private val symphony: Symphony) {
    fun getThemeMode() = getSharedPreferences().getString(SettingsKeys.themeMode, null)
        ?.let { ThemeMode.valueOf(it) }
        ?: ThemeMode.defaultThemeMode

    fun setThemeMode(themeMode: ThemeMode) {
        getSharedPreferences().edit {
            putString(SettingsKeys.themeMode, themeMode.name)
        }
    }

    fun getLanguage() = getSharedPreferences().getString(SettingsKeys.language, null)
    fun setLangauge(language: String) {
        getSharedPreferences().edit {
            putString(SettingsKeys.language, language)
        }
    }

    fun getUseMaterialYou() = getSharedPreferences().getBoolean(SettingsKeys.materialYou, true)
    fun setUseMaterialYou(value: Boolean) {
        getSharedPreferences().edit {
            putBoolean(SettingsKeys.materialYou, value)
        }
    }

    private fun getSharedPreferences() =
        symphony.applicationContext.getSharedPreferences(
            SettingsKeys.identifier,
            Context.MODE_PRIVATE
        )
}