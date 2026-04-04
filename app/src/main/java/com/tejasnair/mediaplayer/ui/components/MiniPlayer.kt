package com.tejasnair.mediaplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

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
    val progress = if (duration > 0L)
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    else 0f

    val scope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }
    var isMinimal by remember { mutableStateOf(false) }

    // Vertical offset for dismiss (swipe down) and NowPlaying (swipe up)
    val offsetY = remember { Animatable(0f) }
    val dismissDownThreshold = 150f
    val openUpThreshold = -100f

    val verticalAlpha = (1f - (offsetY.value / (dismissDownThreshold * 1.5f))).coerceIn(0f, 1f)

    val dominantColor = rememberDominantColor(song.songArtUri)
    val darkVariant = dominantColor.copy(
        red = (dominantColor.red * 0.5f).coerceIn(0f, 1f),
        green = (dominantColor.green * 0.5f).coerceIn(0f, 1f),
        blue = (dominantColor.blue * 0.5f).coerceIn(0f, 1f)
    )

    // Animated size transition
    val playerWidth by animateDpAsState(
        targetValue = if (isMinimal) 72.dp else 280.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "playerWidth"
    )
    val playerHeight by animateDpAsState(
        targetValue = if (isMinimal) 72.dp else 88.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "playerHeight"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isMinimal) 0f else 1f,
        animationSpec = tween(150),
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        // Player card
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(0, offsetY.value.toInt()) }
                .graphicsLayer { this.alpha = verticalAlpha }
                .width(playerWidth)
                .height(playerHeight)
                .clip(RoundedCornerShape(20.dp))
                // Vertical gestures — swipe up opens NowPlaying, swipe down dismisses
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetY.value > dismissDownThreshold -> {
                                        // Animate down then dismiss
                                        isDismissing = true
                                        offsetY.animateTo(
                                            targetValue = 600f,
                                            animationSpec = tween(200)
                                        )
                                        onDismiss()
                                    }
                                    offsetY.value < openUpThreshold -> {
                                        // Animate back then open NowPlaying
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                        onClick()
                                    }
                                    else -> {
                                        // Snap back
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            // Allow dragging down freely, restrict upward
                            val newOffset = offsetY.value + dragAmount
                            scope.launch {
                                offsetY.snapTo(newOffset.coerceAtLeast(-150f))
                            }
                        }
                    )
                }
                // Horizontal gestures — swipe right shrinks, swipe left expands
                .pointerInput(isMinimal) {
                    detectHorizontalDragGestures(
                        onDragEnd = { },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!isMinimal && dragAmount > 30f) {
                                isMinimal = true
                            } else if (isMinimal && dragAmount < -30f) {
                                isMinimal = false
                            }
                        }
                    )
                }
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

            // Tap anywhere to open NowPlaying — sits under content so play button still works
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onClick() }
                        )
                    }
            )

            // Full mode content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = contentAlpha }
            ) {
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

                    PlayButtonWithRing(
                        progress = progress,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        onTogglePlay = onTogglePlay,
                        size = 48,
                        buttonSize = 36,
                        iconSize = 18,
                        ringStrokeWidth = 3f
                    )
                }
            }

            // Minimal mode content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = 1f - contentAlpha },
                contentAlignment = Alignment.Center
            ) {
                PlayButtonWithRing(
                    progress = progress,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    onTogglePlay = onTogglePlay,
                    size = 60,
                    buttonSize = 48,
                    iconSize = 22,
                    ringStrokeWidth = 4f,
                    fillParent = false
                )
            }
        }
    }
}