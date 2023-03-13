package io.github.zyrouge.symphony.services.i18n.translations

object CommonTranslations {
    val SomethingWentHorriblyWrong = "Something went horribly wrong!"

    fun ErrorX(x: String) = "Error: $x"
    fun SomethingWentHorriblyWrongErrorX(x: String) =
        "$SomethingWentHorriblyWrong (${ErrorX(x)})"
}
