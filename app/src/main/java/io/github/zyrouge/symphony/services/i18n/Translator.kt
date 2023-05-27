package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations

class Translator(private val symphony: Symphony) {
    suspend fun onChange(fn: (ITranslations) -> Unit) {
        symphony.settings.language.collect {
            fn(getCurrentTranslations())
        }
    }

    fun getCurrentTranslations() = symphony.settings.language.value
        ?.let { Translations.fromLocale(it) }
        ?: Translations.default
}
