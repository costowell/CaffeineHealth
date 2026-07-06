package com.uc.caffeine.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.uc.caffeine.R
import androidx.compose.ui.text.googlefonts.Font

// Android Studio created downloadable font XML descriptors in res/font/
// We reference them directly — Android handles the downloading automatically
val fontName = GoogleFont("Montserrat")
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val MontserratFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Thin),
)

// Full M3 type scale — all Montserrat
private val BaseTypography = Typography(
    displayLarge   = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,     fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium  = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,     fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall   = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,     fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge     = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium     = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = MontserratFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)

// M3 Expressive emphasized roles: same metrics with a heavier Montserrat weight.
// Without these overrides the library defaults silently fall back to Roboto.
val Typography = BaseTypography.copy(
    displayLargeEmphasized   = BaseTypography.displayLarge,
    displayMediumEmphasized  = BaseTypography.displayMedium,
    displaySmallEmphasized   = BaseTypography.displaySmall,
    headlineLargeEmphasized  = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMediumEmphasized = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmallEmphasized  = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLargeEmphasized     = BaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMediumEmphasized    = BaseTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmallEmphasized     = BaseTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLargeEmphasized      = BaseTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
    bodyMediumEmphasized     = BaseTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
    bodySmallEmphasized      = BaseTypography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
    labelLargeEmphasized     = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMediumEmphasized    = BaseTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmallEmphasized     = BaseTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
)
