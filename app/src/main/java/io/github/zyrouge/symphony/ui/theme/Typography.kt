package io.github.zyrouge.symphony.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
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

    fun toTypography(font: SymphonyFont, textDirection: TextDirection): Typography {
        val fontFamily = font.fontFamily()
        return Typography().run {
            copy(
                displayLarge = displayLarge.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                displayMedium = displayMedium.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                displaySmall = displaySmall.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                headlineLarge = headlineLarge.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                headlineMedium = headlineMedium.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                headlineSmall = headlineSmall.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                titleLarge = titleLarge.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                titleMedium = titleMedium.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                titleSmall = titleSmall.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                bodyLarge = bodyLarge.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                bodyMedium = bodyMedium.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                bodySmall = bodySmall.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                labelLarge = labelLarge.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                labelMedium = labelMedium.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
                labelSmall = labelSmall.copy(
                    fontFamily = fontFamily,
                    textDirection = textDirection,
                ),
            )
        }
    }
}
