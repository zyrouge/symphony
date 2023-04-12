package io.github.zyrouge.symphony.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.zyrouge.symphony.R

enum class SymphonyFont(
    val fontName: String,
    val fontFamily: FontFamily,
) {
    Inter(
        fontName = "Inter",
        fontFamily = FontFamily(
            Font(R.font.inter_regular, FontWeight.Normal),
            Font(R.font.inter_bold, FontWeight.Bold),
        ),
    ),
    Poppins(
        fontName = "Poppins",
        fontFamily = FontFamily(
            Font(R.font.roboto_regular, FontWeight.Normal),
            Font(R.font.roboto_bold, FontWeight.Bold)
        ),
    ),
    DMSans(
        fontName = "DM Sans",
        fontFamily = FontFamily(
            Font(R.font.dmsans_regular, FontWeight.Normal),
            Font(R.font.dmsans_bold, FontWeight.Bold)
        ),
    ),
    Roboto(
        fontName = "Roboto",
        fontFamily = FontFamily(
            Font(R.font.roboto_regular, FontWeight.Normal),
            Font(R.font.roboto_bold, FontWeight.Bold)
        ),
    ),
    ProductSans(
        fontName = "Product Sans",
        fontFamily = FontFamily(
            Font(R.font.productsans_regular, FontWeight.Normal),
            Font(R.font.productsans_bold, FontWeight.Bold)
        ),
    ),
}

object SymphonyTypography {
    val defaultFont = SymphonyFont.Inter
    val all = mapOf(
        SymphonyFont.Inter.fontName to SymphonyFont.Inter,
        SymphonyFont.Poppins.fontName to SymphonyFont.Poppins,
        SymphonyFont.DMSans.fontName to SymphonyFont.DMSans,
        SymphonyFont.Roboto.fontName to SymphonyFont.Roboto,
        SymphonyFont.ProductSans.fontName to SymphonyFont.ProductSans,
    )

    fun resolveFont(name: String?) = all[name] ?: defaultFont

    fun toTypography(font: SymphonyFont): Typography {
        font.run {
            return Typography().run {
                copy(
                    displayLarge = displayLarge.copy(fontFamily = fontFamily),
                    displayMedium = displayMedium.copy(fontFamily = fontFamily),
                    displaySmall = displaySmall.copy(fontFamily = fontFamily),
                    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
                    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
                    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
                    titleLarge = titleLarge.copy(fontFamily = fontFamily),
                    titleMedium = titleMedium.copy(fontFamily = fontFamily),
                    titleSmall = titleSmall.copy(fontFamily = fontFamily),
                    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
                    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
                    bodySmall = bodySmall.copy(fontFamily = fontFamily),
                    labelLarge = labelLarge.copy(fontFamily = fontFamily),
                    labelMedium = labelMedium.copy(fontFamily = fontFamily),
                    labelSmall = labelSmall.copy(fontFamily = fontFamily)
                )
            }
        }
    }
}
