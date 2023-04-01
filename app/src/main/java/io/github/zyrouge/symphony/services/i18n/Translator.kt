package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.SettingsKeys
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations

class Translator(private val symphony: Symphony) {
    var t: ITranslations

    init {
        t = getCurrentTranslations()
        symphony.settings.onChange.subscribe { key ->
            when (key) {
                SettingsKeys.language -> {
                    t = getCurrentTranslations()
                }
            }
        }
    }

    fun getCurrentTranslations() = symphony.settings.getLanguage()
        ?.let { Translations.fromLocale(it) }
        ?: Translations.default
}
