package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.Symphony
import org.json.JSONObject

class Translations(private val symphony: Symphony) : _Translations() {
    val defaultLocaleCode = "en"

    fun supports(locale: String) = localeCodes.contains(locale)

    fun parse(locale: String): Translation {
        val content = symphony.applicationContext.assets
            .open("i18n/${locale}.json")
            .bufferedReader()
            .use { it.readText() }
        val json = JSONObject(content)
        return Translation(json)
    }
}
