package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.services.i18n.translations.BeTranslation
import io.github.zyrouge.symphony.services.i18n.translations.EnTranslation
import io.github.zyrouge.symphony.services.i18n.translations.RuTranslation
import io.github.zyrouge.symphony.services.i18n.translations.UkTranslation

object Translations {
    val all = arrayOf(
        EnTranslation(),
        BeTranslation(),
        RuTranslation(),
        UkTranslation(),
    )
    val default = all.first()

    fun fromLocale(locale: String) = all.find {
        it.Locale == locale
    }
}
