package io.github.zyrouge.symphony.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.zyrouge.symphony.R

class SymphonyFont(
    val fontName: String,
    val fontFamily: () -> FontFamily,
) {
    companion object {
        fun fromValue(fontName: String, fontFamily: FontFamily) = SymphonyFont(
            fontName = fontName,
            fontFamily = { fontFamily }
        )
    }
}

object SymphonyBuiltinFonts {
    val Inter = SymphonyFont.fromValue(
        fontName = "Inter",
        fontFamily = FontFamily(
            Font(R.font.inter_regular, FontWeight.Normal),
            Font(R.font.inter_bold, FontWeight.Bold),
        ),
    );

    val Poppins = SymphonyFont.fromValue(
        fontName = "Poppins",
        fontFamily = FontFamily(
            Font(R.font.roboto_regular, FontWeight.Normal),
            Font(R.font.roboto_bold, FontWeight.Bold)
        ),
    );

    val DMSans = SymphonyFont.fromValue(
        fontName = "DM Sans",
        fontFamily = FontFamily(
            Font(R.font.dmsans_regular, FontWeight.Normal),
            Font(R.font.dmsans_bold, FontWeight.Bold)
        ),
    );

    val Roboto = SymphonyFont.fromValue(
        fontName = "Roboto",
        fontFamily = FontFamily(
            Font(R.font.roboto_regular, FontWeight.Normal),
            Font(R.font.roboto_bold, FontWeight.Bold)
        ),
    );

    val ProductSans = SymphonyFont.fromValue(
        fontName = "Product Sans",
        fontFamily = FontFamily(
            Font(R.font.productsans_regular, FontWeight.Normal),
            Font(R.font.productsans_bold, FontWeight.Bold)
        ),
    );
}

object SymphonyTypography {
    val defaultFont = SymphonyBuiltinFonts.Inter
    val all = mapOf(
        SymphonyBuiltinFonts.Inter.fontName to SymphonyBuiltinFonts.Inter,
        SymphonyBuiltinFonts.Poppins.fontName to SymphonyBuiltinFonts.Poppins,
        SymphonyBuiltinFonts.DMSans.fontName to SymphonyBuiltinFonts.DMSans,
        SymphonyBuiltinFonts.Roboto.fontName to SymphonyBuiltinFonts.Roboto,
        SymphonyBuiltinFonts.ProductSans.fontName to SymphonyBuiltinFonts.ProductSans,
    )

    fun resolveFont(name: String?) = all[name] ?: defaultFont

    fun toTypography(font: SymphonyFont): Typography {
        val fontFamily = font.fontFamily()
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
