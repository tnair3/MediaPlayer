package com.tejasnair.mediaplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Waveform(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = Color.White
) {
    // TEMPORARY FIXED IMPLEMENTATION

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val animations = (0 until barCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400 + (index * 100), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Canvas(modifier = modifier.size(24.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / (barCount * 2f)
        val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

        animations.forEachIndexed { index, animState ->
            val xOffset = index * (barWidth * 2) + (barWidth / 2)
            val barHeight = canvasHeight * animState.value
            val yOffset = (canvasHeight - barHeight) / 2

            drawRoundRect(
                color = color,
                topLeft = Offset(xOffset, yOffset),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}