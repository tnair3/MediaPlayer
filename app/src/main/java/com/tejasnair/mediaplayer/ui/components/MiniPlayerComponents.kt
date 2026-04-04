package com.tejasnair.mediaplayer.ui.components

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tejasnair.mediaplayer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

@Composable
fun rememberDominantColor(artUri: String?): Color {
    val context = LocalContext.current
    val defaultColor = Color(0xFF1D1D1D)
    var dominantColor by remember(artUri) { mutableStateOf(defaultColor) }

    LaunchedEffect(artUri) {
        if (artUri == null) {
            dominantColor = defaultColor
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artUri)
                    .bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888)
                    .build()
                val result = loader.execute(request)
                val bitmap = ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
                    ?: return@withContext
                val palette = Palette.from(bitmap).generate()
                val swatch = palette.darkVibrantSwatch
                    ?: palette.vibrantSwatch
                    ?: palette.darkMutedSwatch
                    ?: palette.dominantSwatch
                swatch?.let { dominantColor = Color(it.rgb) }
            } catch (e: Exception) {
                Log.e("DominantColor", "Failed", e)
                dominantColor = defaultColor
            }
        }
    }

    return dominantColor
}