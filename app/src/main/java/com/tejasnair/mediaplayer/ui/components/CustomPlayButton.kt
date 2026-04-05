package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.R

@Composable
fun PlayButtonWithRing(
    progress: Float,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onTogglePlay: () -> Unit,
    size: Int,
    buttonSize: Int,
    iconSize: Int,
    ringStrokeWidth: Float,
    fillParent: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .drawBehind {
                val strokeWidthPx = ringStrokeWidth.dp.toPx()
                val inset = strokeWidthPx / 2f
                val arcSize = Size(
                    this.size.width - strokeWidthPx,
                    this.size.height - strokeWidthPx
                )
                val topLeft = Offset(inset, inset)
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .blur(6.dp)
        )
        Box(
            modifier = Modifier
                .size(buttonSize.dp)
                .background(Color.White, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onTogglePlay,
                modifier = if (fillParent) Modifier.fillMaxSize() else Modifier.size(buttonSize.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.song_pause
                        else if (duration in 1..currentPosition) R.drawable.song_restart
                        else R.drawable.song_play
                    ),
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(iconSize.dp)
                )
            }
        }
    }
}