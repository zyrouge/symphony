package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations

class Translator(private val symphony: Symphony) {
    var t: ITranslations

    init {
        t = symphony.settings.getLanguage()
            ?.let { Translations.fromLocale(it) }
            ?: Translations.default
    }
}
