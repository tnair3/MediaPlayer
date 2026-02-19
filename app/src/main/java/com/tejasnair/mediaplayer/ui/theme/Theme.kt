package com.tejasnair.mediaplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),       // soft purple accent
    onPrimary = Color.White,           // text/icons on primary
    secondary = Color(0xFF03DAC6),    // teal accent
    onSecondary = Color.Black,
    background = Color(0xFF121212),   // deep dark gray
    onBackground = Color(0xFFE0E0E0), // light gray text
    surface = Color(0xFF1E1E1E),      // slightly lighter dark for cards
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFCF6679),        // reddish alert
    onError = Color.Black
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),       // vibrant purple accent
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),     // teal accent
    onSecondary = Color.Black,
    background = Color(0xFFFFFFFF),    // white background
    onBackground = Color(0xFF1C1C1C),  // dark text
    surface = Color(0xFFF5F5F5),       // light gray for cards
    onSurface = Color(0xFF1C1C1C),
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun ThemedScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

@Composable
fun MediaPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}