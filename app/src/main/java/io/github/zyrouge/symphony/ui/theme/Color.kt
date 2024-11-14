package io.github.zyrouge.symphony.ui.theme

import androidx.compose.ui.graphics.Color

enum class PrimaryThemeColor {
    Red,
    Orange,
    Amber,
    Yellow,
    Lime,
    Green,
    Emerald,
    Teal,
    Cyan,
    Sky,
    Blue,
    Indigo,
    Violet,
    Purple,
    Fuchsia,
    Pink,
    Rose;
}

object ThemeColors {
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

    val DefaultPrimaryColor = PrimaryThemeColor.Purple
    val PrimaryColorsMap = mapOf(
        PrimaryThemeColor.Red to Red,
        PrimaryThemeColor.Orange to Orange,
        PrimaryThemeColor.Amber to Amber,
        PrimaryThemeColor.Yellow to Yellow,
        PrimaryThemeColor.Lime to Lime,
        PrimaryThemeColor.Green to Green,
        PrimaryThemeColor.Emerald to Emerald,
        PrimaryThemeColor.Teal to Teal,
        PrimaryThemeColor.Cyan to Cyan,
        PrimaryThemeColor.Sky to Sky,
        PrimaryThemeColor.Blue to Blue,
        PrimaryThemeColor.Indigo to Indigo,
        PrimaryThemeColor.Violet to Violet,
        PrimaryThemeColor.Purple to Purple,
        PrimaryThemeColor.Fuchsia to Fuchsia,
        PrimaryThemeColor.Pink to Pink,
        PrimaryThemeColor.Rose to Rose,
    )

    fun resolvePrimaryColorKey(value: String?) =
        PrimaryThemeColor.entries.find { it.name == value } ?: DefaultPrimaryColor

    fun resolvePrimaryColor(value: PrimaryThemeColor) = PrimaryColorsMap[value]!!
    fun resolvePrimaryColor(value: String?) = resolvePrimaryColor(resolvePrimaryColorKey(value))
}
