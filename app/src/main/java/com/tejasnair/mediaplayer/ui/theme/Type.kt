package com.tejasnair.mediaplayer.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import com.tejasnair.mediaplayer.R

private val Sora = FontFamily(
    Font(resId = R.font.sora_regular, weight = FontWeight.Normal),
    Font(resId = R.font.sora_medium, weight = FontWeight.Medium),
    Font(resId = R.font.sora_semibold, weight = FontWeight.SemiBold)
)

private val defaultStyle = TextStyle(
    fontFamily = Sora
)

val Typography = Typography(
    titleLarge = defaultStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp
    ),
    titleMedium = defaultStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    titleSmall = defaultStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    bodyLarge = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = defaultStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)