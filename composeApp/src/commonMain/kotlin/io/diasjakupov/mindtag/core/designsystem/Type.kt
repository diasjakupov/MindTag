package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mindtag.composeapp.generated.resources.Res
import mindtag.composeapp.generated.resources.lexend_bold
import mindtag.composeapp.generated.resources.lexend_light
import mindtag.composeapp.generated.resources.lexend_medium
import mindtag.composeapp.generated.resources.lexend_regular
import mindtag.composeapp.generated.resources.lexend_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun LexendFontFamily(): FontFamily = FontFamily(
    Font(Res.font.lexend_light, FontWeight.Light),
    Font(Res.font.lexend_regular, FontWeight.Normal),
    Font(Res.font.lexend_medium, FontWeight.Medium),
    Font(Res.font.lexend_semibold, FontWeight.SemiBold),
    Font(Res.font.lexend_bold, FontWeight.Bold),
)

@Composable
fun MindTagTypography(): Typography {
    val lexend = LexendFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            lineHeight = 52.sp,
            letterSpacing = (-0.5).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.25).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.25).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.25).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.015).sp,
        ),
        titleSmall = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
        ),
    )
}

/** Caption style (10sp medium) used for bottom nav labels and figure captions. */
@Composable
fun captionTextStyle(): TextStyle {
    val lexend = LexendFontFamily()
    return TextStyle(
        fontFamily = lexend,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    )
}
