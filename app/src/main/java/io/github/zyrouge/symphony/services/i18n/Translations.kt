package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.services.i18n.translations.EnTranslation
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations

object Translations {
    val all = arrayOf<ITranslations>(EnTranslation())
    val default = all.first()

    fun fromLocale(locale: String) = all.find {
        it.Locale == locale
    }
}
