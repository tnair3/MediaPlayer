package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.animation.core.animate
import androidx.media3.common.Player
import coil.compose.AsyncImage
import android.annotation.SuppressLint
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.components.SongRow

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun NowPlayingScreen(
    viewModel: PlaybackViewModel,
    onBackClick: () -> Unit
) {
    val currentSong = viewModel.currentSong
    val isPlaying = viewModel.isPlaying
    val currentPosition = viewModel.currentPosition
    val duration = viewModel.duration
    val queue = viewModel.currentQueue
    val repeatMode = viewModel.repeatMode

    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val threshold = 300f

    ThemedScreen {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > threshold) viewModel.skipToPreviousForce()
                                else if (offsetX < -threshold) viewModel.skipToNext()

                                scope.launch {
                                    animate(initialValue = offsetX, targetValue = 0f) { value, _ ->
                                        offsetX = value
                                    }
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount
                            }
                        )
                    }
                    .graphicsLayer {
                        translationX = offsetX
                        alpha = 1f - (kotlin.math.abs(offsetX) / (threshold * 2f)).coerceAtMost(0.5f)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    model = currentSong?.songArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentSong?.title ?: "Unknown Title",
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong?.artists ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = formatTime(currentPosition), style = MaterialTheme.typography.labelSmall)
                    Text(text = formatTime(duration), style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(onClick = {  }) {
                        Icon(
                            painter = painterResource(id = R.drawable.song_favourite),
                            contentDescription = "Favourite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { viewModel.skipToPrevious() }) {
                        Icon(painterResource(
                            id = R.drawable.song_previous),
                            contentDescription = "Previous Song",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(color = MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if(isPlaying) R.drawable.song_pause
                                else if(currentPosition == duration) R.drawable.song_restart
                                else R.drawable.song_play
                            ),
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(painterResource(
                            id = R.drawable.song_next),
                            contentDescription = "Next Song",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                        val iconRes = when (repeatMode) {
                            Player.REPEAT_MODE_ALL -> R.drawable.song_repeat_all
                            Player.REPEAT_MODE_ONE -> R.drawable.song_repeat_one
                            else -> R.drawable.song_repeat_off
                        }
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = "Repeat",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp))
                    }
                }
            }

            Text(
                text = "Up Next",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(queue) { index, song ->
                    val isCurrent = song.filePath == currentSong?.filePath

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .then(
                                if (isCurrent) {
                                    Modifier
                                        .clip(RoundedCornerShape(16.dp)) // Defines the rounded rect
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        )
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                    ) {
                        SongRow(
                            song = song,
                            onClick = { viewModel.playFromQueue(index) },
                            showTrackNumbers = false
                        )
                    }
                }
            }
        }
    }
}