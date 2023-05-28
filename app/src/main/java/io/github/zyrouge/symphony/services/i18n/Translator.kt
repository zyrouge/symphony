package io.github.zyrouge.symphony.services.i18n

import androidx.core.os.LocaleListCompat
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations

class Translator(private val symphony: Symphony) {
    suspend fun onChange(fn: (ITranslations) -> Unit) {
        symphony.settings.language.collect {
            fn(getCurrentTranslations())
        }
    }

    fun getCurrentTranslations() = symphony.settings.language.value
        ?.let { Translations.get(it) }
        ?: getDefaultTranslation()

    private fun getDefaultTranslation(): ITranslations {
        val systemLocale = LocaleListCompat.getDefault()[0]?.language
        return systemLocale?.let { Translations.get(it) } ?: Translations.default()
    }
}
