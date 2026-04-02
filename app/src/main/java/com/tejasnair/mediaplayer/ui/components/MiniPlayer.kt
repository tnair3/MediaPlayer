package com.tejasnair.mediaplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.drawable.BitmapDrawable
import android.util.Log

@Composable
fun rememberDominantColor(artUri: String?): Color {
    val context = LocalContext.current
    val defaultColor = Color(0xFF1D1D1D)
    var dominantColor by remember(artUri) { mutableStateOf(defaultColor) }

    LaunchedEffect(artUri) {
        Log.d("DominantColor", "Loading art from: $artUri")
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
                Log.d("DominantColor", "Swatch: $swatch")
                swatch?.let { dominantColor = Color(it.rgb) }
            } catch (e: Exception) {
                Log.e("DominantColor", "Failed", e)
                dominantColor = defaultColor
            }
        }
    }

    return dominantColor
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    playbackViewModel: PlaybackViewModel
) {
    val currentPosition = playbackViewModel.currentPosition
    val duration = playbackViewModel.duration

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val dismissThreshold = 300f
    var isDismissing by remember { mutableStateOf(false) }

    val alpha = (1f - (offsetX.value / (dismissThreshold * 1.5f))).coerceIn(0f, 1f)
    val backgroundAlpha = when {
        isDismissing -> (1f - (offsetX.value - dismissThreshold) / 700f).coerceIn(0f, 1f)
        offsetX.value > 0f -> 1f
        else -> 0f
    }

    val dominantColor = rememberDominantColor(song.songArtUri)
    val darkVariant = dominantColor.copy(
        red = (dominantColor.red * 0.5f).coerceIn(0f, 1f),
        green = (dominantColor.green * 0.5f).coerceIn(0f, 1f),
        blue = (dominantColor.blue * 0.5f).coerceIn(0f, 1f)
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        // Dismiss background
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(88.dp)
                .graphicsLayer { this.alpha = backgroundAlpha }
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.9f),
                            darkVariant.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                modifier = Modifier.padding(start = 12.dp),
                painter = painterResource(R.drawable.close),
                contentDescription = "Dismiss",
                tint = Color.White.copy(
                    alpha = (offsetX.value / dismissThreshold).coerceIn(0f, 1f)
                )
            )
        }

        // Player card
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.toInt(), 0) }
                .graphicsLayer { this.alpha = alpha }
                .width(280.dp)
                .height(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > dismissThreshold) {
                                    isDismissing = true
                                    offsetX.animateTo(
                                        targetValue = 1000f,
                                        animationSpec = tween(200)
                                    )
                                    onDismiss()
                                } else {
                                    isDismissing = false
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceAtLeast(0f)
                            scope.launch { offsetX.snapTo(newOffset) }
                        }
                    )
                }
                .clickable(onClick = onClick)
        ) {
            // Blurred art background
            AsyncImage(
                model = song.songArtUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp),
                contentScale = ContentScale.Crop
            )

            // Dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )

            // Content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.songArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        color = Color.White
                    )
                    Text(
                        text = song.artists,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatTime(currentPosition)} · ${formatTime(duration)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }

                // Circular glow play button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .blur(6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isPlaying) R.drawable.song_pause
                                    else if (duration in 1..currentPosition) R.drawable.song_restart
                                    else R.drawable.song_play
                                ),
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomStart)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        if (duration > 0L) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                        else 0f
                    )
                    .height(3.dp)
                    .align(Alignment.BottomStart)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}