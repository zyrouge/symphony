package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.Symphony

class Translations(private val symphony: Symphony) : _Translations() {
    val defaultLocaleCode = "en"

    fun supports(locale: String) = localeCodes.contains(locale)

    fun parse(locale: String) = symphony.applicationContext.assets.open("i18n/${locale}.json").use {
        Translation.fromInputStream(it)
    }
}
