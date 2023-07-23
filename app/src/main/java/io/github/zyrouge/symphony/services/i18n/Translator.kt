package io.github.zyrouge.symphony.services.i18n

import androidx.core.os.LocaleListCompat
import io.github.zyrouge.symphony.Symphony

class Translator(private val symphony: Symphony) {
    val translations = Translations(symphony)

    suspend fun onChange(fn: (Translation) -> Unit) {
        symphony.settings.language.collect {
            fn(getCurrentTranslation())
        }
    }

    fun getCurrentTranslation() = symphony.settings.language.value
        ?.let { translations.parse(it) }
        ?: getDefaultTranslation()

    fun getDefaultTranslation(): Translation {
        val localeCode = getDefaultLocaleCode()
        return translations.parse(localeCode)
    }

    fun getLocaleDisplayName(localeCode: String) =
        translations.localeDisplayNames[localeCode]

    fun getLocaleNativeName(localeCode: String) =
        translations.localeNativeNames[localeCode]

    fun getDefaultLocaleDisplayName() = getLocaleDisplayName(getDefaultLocaleCode())!!
    fun getDefaultLocaleNativeName() = getLocaleNativeName(getDefaultLocaleCode())!!
    fun getDefaultLocaleCode() = LocaleListCompat.getDefault()[0]?.language
        ?.takeIf { translations.supports(it) }
        ?: translations.defaultLocaleCode
}
