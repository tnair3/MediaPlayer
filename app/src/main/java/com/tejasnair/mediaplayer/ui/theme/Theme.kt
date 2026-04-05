package com.tejasnair.mediaplayer.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

private val ColorScheme = darkColorScheme(
    primary = Color(0xFFF5F5F5),
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF787878),
    outline = Color(0xFFBBBBBB),
    outlineVariant = Color(0xFF444444),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun MediaPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ThemedScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}