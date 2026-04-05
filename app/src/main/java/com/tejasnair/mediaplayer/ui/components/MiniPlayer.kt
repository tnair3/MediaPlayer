package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val currentPosition = playbackViewModel.currentPosition
    val duration = playbackViewModel.duration
    val progress =
        if (duration > 0L) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        else 0f

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val dismissThreshold = 300f
    var trayOpen by remember { mutableStateOf(false) }

    val trayHeight = 48.dp
    val trayWidth = 200.dp  // narrower than the 280dp card

    val traySlide by animateDpAsState(
        targetValue = if (trayOpen) 0.dp else trayHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "traySlide"
    )
    val trayAlpha by animateFloatAsState(
        targetValue = if (trayOpen) 1f else 0f,
        animationSpec = tween(200),
        label = "trayAlpha"
    )

    val cardAlpha = (1f - (offsetX.value / (dismissThreshold * 1.5f))).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Tray
        Box(
            modifier = Modifier
                .width(trayWidth)
                .height(trayHeight)
                .offset {
                    IntOffset(
                        x = offsetX.value.roundToInt(),
                        y = ((-88).dp + traySlide).roundToPx()
                    )
                }
                .graphicsLayer { this.alpha = trayAlpha * cardAlpha }
                .clip(shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(color = MaterialTheme.colorScheme.primary)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Close
                IconButton(
                    onClick = {
                        trayOpen = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = {
                        onPrevious()
                        trayOpen = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.song_previous),
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Favourite
                IconButton(
                    onClick = {
                        song.songId.let(block = libraryViewModel::toggleFavourite)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id =
                            if(song.isFavourite) R.drawable.song_favourite_true
                            else R.drawable.song_favourite),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = {
                        onNext()
                        trayOpen = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.song_next),
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Player card
        Box(
            modifier = Modifier
                .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                .graphicsLayer { this.alpha = cardAlpha }
                .width(280.dp)
                .height(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > dismissThreshold) {
                                    offsetX.animateTo(
                                        targetValue = 1000f,
                                        animationSpec = tween(200)
                                    )
                                    onDismiss()
                                } else {
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
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = { },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -20f && !trayOpen) trayOpen = true
                            else if (dragAmount > 20f && trayOpen) trayOpen = false
                        }
                    )
                }
                .pointerInput(trayOpen) {
                    detectTapGestures(
                        onTap = {
                            if (trayOpen) trayOpen = false
                            else onClick()
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

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp)
                ) {
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
        }
    }
}