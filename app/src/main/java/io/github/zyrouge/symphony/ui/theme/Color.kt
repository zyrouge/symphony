package io.github.zyrouge.symphony.ui.theme

import androidx.compose.ui.graphics.Color

enum class PrimaryThemeColors {
    SYMPHONY_BLUE,
    RED,
    ORANGE,
    AMBER,
    YELLOW,
    LIME,
    GREEN,
    EMERALD,
    TEAL,
    CYAN,
    SKY,
    BLUE,
    INDIGO,
    VIOLET,
    PURPLE,
    FUCHSIA,
    PINK,
    ROSE;

    fun toHumanString() = name
        .split("_")
        .joinToString(" ") {
            it[0].uppercase() + it.substring(1).lowercase()
        }
}

object ThemeColors {
    val SymphonyBlue = Color(0xFF545DFF)
    val Red = Color(0xFFEF4444)
    val Orange = Color(0xFFF97316)
    val Amber = Color(0xFFF59E0B)
    val Yellow = Color(0xFFEAB308)
    val Lime = Color(0xFF84CC16)
    val Green = Color(0xFF22C55E)
    val Emerald = Color(0xFF10B981)
    val Teal = Color(0xFF14B8A6)
    val Cyan = Color(0xFF06B6D4)
    val Sky = Color(0xFF0EA5E9)
    val Blue = Color(0xFF3B82F6)
    val Indigo = Color(0xFF6366f1)
    val Violet = Color(0xFF8B5CF6)
    val Purple = Color(0xFFA855F7)
    val Fuchsia = Color(0xFFD946EF)
    val Pink = Color(0xFFEC4899)
    val Rose = Color(0xFFF43f5E)

    val Neutral50 = Color(0xFFFAFAFA)
    val Neutral100 = Color(0xFFF5F5F5)
    val Neutral200 = Color(0xFFE5E5E5)
    val Neutral800 = Color(0xFF262626)
    val Neutral900 = Color(0xFF171717)

    val DefaultPrimaryColor = PrimaryThemeColors.SYMPHONY_BLUE
    val PrimaryColorsMap = mapOf(
        PrimaryThemeColors.SYMPHONY_BLUE to SymphonyBlue,
        PrimaryThemeColors.RED to Red,
        PrimaryThemeColors.ORANGE to Orange,
        PrimaryThemeColors.AMBER to Amber,
        PrimaryThemeColors.YELLOW to Yellow,
        PrimaryThemeColors.LIME to Lime,
        PrimaryThemeColors.GREEN to Green,
        PrimaryThemeColors.EMERALD to Emerald,
        PrimaryThemeColors.TEAL to Teal,
        PrimaryThemeColors.CYAN to Cyan,
        PrimaryThemeColors.SKY to Sky,
        PrimaryThemeColors.BLUE to Blue,
        PrimaryThemeColors.INDIGO to Indigo,
        PrimaryThemeColors.VIOLET to Violet,
        PrimaryThemeColors.PURPLE to Purple,
        PrimaryThemeColors.FUCHSIA to Fuchsia,
        PrimaryThemeColors.PINK to Pink,
        PrimaryThemeColors.ROSE to Rose,
    )

    fun resolvePrimaryColorKey(value: String?) =
        PrimaryThemeColors.values().find { it.name == value } ?: DefaultPrimaryColor

    fun resolvePrimaryColor(value: PrimaryThemeColors) = PrimaryColorsMap[value]!!
    fun resolvePrimaryColor(value: String?) = resolvePrimaryColor(resolvePrimaryColorKey(value))
}
