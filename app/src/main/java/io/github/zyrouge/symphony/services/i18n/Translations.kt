package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.services.i18n.translations.BeTranslation
import io.github.zyrouge.symphony.services.i18n.translations.EnTranslation
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations
import io.github.zyrouge.symphony.services.i18n.translations.ItTranslation
import io.github.zyrouge.symphony.services.i18n.translations.RuTranslation
import io.github.zyrouge.symphony.services.i18n.translations.TrTranslation
import io.github.zyrouge.symphony.services.i18n.translations.UkTranslation

object Translations {
    val all = map(
        EnTranslation(),
        BeTranslation(),
        ItTranslation(),
        RuTranslation(),
        TrTranslation(),
        UkTranslation(),
    )

    fun get(locale: String) = all[locale]
    fun default() = all.values.first()

    private fun map(vararg translations: ITranslations) = translations.associateBy { it.Locale }
}
