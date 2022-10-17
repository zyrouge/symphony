package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.Symphony

class Translator(private val symphony: Symphony) {
    var t: Translations

    init {
        t = Translations.default
    }
}