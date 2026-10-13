package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.R


val bodyFontFamily = FontFamily(
    Font(R.font.jetbrainsmono_thin, FontWeight.Thin),
    Font(R.font.jetbrainsmono_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.jetbrainsmono_extralight, FontWeight.ExtraLight),
    Font(R.font.jetbrainsmono_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.jetbrainsmono_light, FontWeight.Light),
    Font(R.font.jetbrainsmono_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
    Font(R.font.jetbrainsmono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.jetbrainsmono_medium, FontWeight.Medium),
    Font(R.font.jetbrainsmono_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.jetbrainsmono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrainsmono_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold),
    Font(R.font.jetbrainsmono_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.jetbrainsmono_extrabold, FontWeight.ExtraBold),
    Font(R.font.jetbrainsmono_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic)
)

val displayFontFamily = FontFamily(
    Font(R.font.jetbrainsmono_thin, FontWeight.Thin),
    Font(R.font.jetbrainsmono_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.jetbrainsmono_extralight, FontWeight.ExtraLight),
    Font(R.font.jetbrainsmono_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.jetbrainsmono_light, FontWeight.Light),
    Font(R.font.jetbrainsmono_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
    Font(R.font.jetbrainsmono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.jetbrainsmono_medium, FontWeight.Medium),
    Font(R.font.jetbrainsmono_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.jetbrainsmono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrainsmono_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold),
    Font(R.font.jetbrainsmono_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.jetbrainsmono_extrabold, FontWeight.ExtraBold),
    Font(R.font.jetbrainsmono_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic)
)

// Default Material 3 typography values
val baseline = Typography()

val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)

