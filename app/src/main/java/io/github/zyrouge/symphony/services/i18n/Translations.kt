package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.services.i18n.translations.*

object Translations {
    val all = arrayOf(
        EnTranslation(),
        BeTranslation(),
        ItTranslation(),
        RuTranslation(),
        TrTranslation(),
        UkTranslation(),
    )
    val default = all.first()

    fun fromLocale(locale: String) = all.find {
        it.Locale == locale
    }
}
