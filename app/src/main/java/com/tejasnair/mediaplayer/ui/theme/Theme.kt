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
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF1A1A1A),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFB0B0B0),
    onSurfaceVariant = Color(0xFF858585),
    outline = Color(0xFF4A4A4A),
    outlineVariant = Color(0xFF2E2E2E),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
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