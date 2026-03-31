package com.tejasnair.mediaplayer.ui.components

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// 2. Compose Runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope

// 3. Material3 (including Experimental APIs)
import androidx.compose.material3.*

// 4. External Libraries
import coil.compose.AsyncImage

// 5. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

import kotlinx.coroutines.launch
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.remember

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
    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val dismissThreshold = 300f
    var isDismissing by remember { mutableStateOf(false) }

    val alpha = (1f - (offsetX.value / (dismissThreshold * 1.5f))).coerceIn(0f, 1f)
    val backgroundAlpha = when {
        isDismissing -> (1f - (offsetX.value - dismissThreshold) / 700f).coerceIn(0f, 1f)
        offsetX.value > 0f -> 1f
        else -> 0f
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(88.dp)
                .graphicsLayer { this.alpha = backgroundAlpha }
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF490505),
                            Color(0xFF2F0604),
                            Color(0xFF150101).copy(alpha = 0.6f)
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

        // The player card
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
                                    // Animate off-screen then dismiss
                                    isDismissing = true
                                    offsetX.animateTo(
                                        targetValue = 1000f,
                                        animationSpec = tween(200)
                                    )
                                    onDismiss()
                                } else {
                                    // Snap back
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
                            // Only allow dragging right, not left
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
                                    else if (currentPosition >= duration && duration > 0L) R.drawable.song_restart
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