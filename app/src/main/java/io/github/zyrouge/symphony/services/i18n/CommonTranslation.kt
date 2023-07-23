package io.github.zyrouge.symphony.services.i18n

object CommonTranslation {
    val SomethingWentHorriblyWrong = "Something went horribly wrong!"
    val System = "System"

    fun ErrorX(x: String) = "Error: $x"
    fun SomethingWentHorriblyWrongErrorX(x: String) =
        "$SomethingWentHorriblyWrong (${ErrorX(x)})"
}
