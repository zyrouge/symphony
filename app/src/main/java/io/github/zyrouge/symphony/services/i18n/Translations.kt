package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.services.i18n.translations.BeTranslation
import io.github.zyrouge.symphony.services.i18n.translations.DeTranslation
import io.github.zyrouge.symphony.services.i18n.translations.EnTranslation
import io.github.zyrouge.symphony.services.i18n.translations.ITranslations
import io.github.zyrouge.symphony.services.i18n.translations.ItTranslation
import io.github.zyrouge.symphony.services.i18n.translations.RuTranslation
import io.github.zyrouge.symphony.services.i18n.translations.TrTranslation
import io.github.zyrouge.symphony.services.i18n.translations.UkTranslation
import io.github.zyrouge.symphony.services.i18n.translations.ZhTranslation

object Translations {
    private val enTranslation = EnTranslation()
    val all = map(
        BeTranslation(),
        DeTranslation(),
        enTranslation,
        ItTranslation(),
        RuTranslation(),
        TrTranslation(),
        UkTranslation(),
        ZhTranslation(),
    )

    fun get(locale: String) = all[locale]
    fun default() = enTranslation

    private fun map(vararg translations: ITranslations) = translations
        .sortedBy { it.Language }
        .associateBy { it.Locale }
}
