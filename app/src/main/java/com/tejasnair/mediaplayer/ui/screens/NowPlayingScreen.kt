package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.formatBytes
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NowPlayingScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    onBackClick: () -> Unit
) {
    val allSongs by libraryViewModel.allSongs.collectAsState()
    val currentSong = allSongs.find { it.songId == playbackViewModel.currentSongId }
    val queue = playbackViewModel.currentQueue.mapNotNull { id -> allSongs.find { it.songId == id } }

    val isPlaying = playbackViewModel.isPlaying
    val currentPosition = playbackViewModel.currentPosition
    val duration = playbackViewModel.duration
    val repeatMode = playbackViewModel.repeatMode

    val fileSizeString = remember(key1 = currentSong?.filePath) {
        val size = getFileSize(currentSong?.filePath)
        if (size > 0) formatBytes(size) else "Unknown"
    }

    val scope = rememberCoroutineScope()

    var isFullScreen by remember { mutableStateOf(value = false) }
    var selectedTab by remember { mutableIntStateOf(value = 0) }
    var showSkipLeft by remember { mutableStateOf(value = false) }
    var showSkipRight by remember { mutableStateOf(value = false) }

    var offsetX by remember { mutableFloatStateOf(value = 0f) }
    val threshold = 300f

    val queueListState = rememberLazyListState()
    val detailsScrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {

        // --- FULL BLEED BLURRED BACKGROUND ---
        AsyncImage(
            model = currentSong?.songArtUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 24.dp),
            contentScale = ContentScale.Crop
        )

        // Dark overlay so content is always readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // --- MAIN CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentSong?.album ?: "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // TOP PLAYER SECTION
            AnimatedVisibility(
                visible = !isFullScreen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
                        .pointerInput(key1 = Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX > threshold) playbackViewModel.skipToPreviousForce()
                                    else if (offsetX < -threshold) playbackViewModel.skipToNext()
                                    scope.launch {
                                        animate(
                                            initialValue = offsetX,
                                            targetValue = 0f
                                        ) { value, _ ->
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
                            alpha =
                                1f - (kotlin.math.abs(x = offsetX) / (threshold * 2f)).coerceAtMost(maximumValue = 0.5f)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LaunchedEffect(key1 = showSkipLeft) {
                        if (showSkipLeft) {
                            delay(timeMillis = 600)
                            showSkipLeft = false
                        }
                    }

                    LaunchedEffect(key1 = showSkipRight) {
                        if (showSkipRight) {
                            delay(timeMillis = 600)
                            showSkipRight = false
                        }
                    }

                    Box(
                        modifier = Modifier
                            .pointerInput(key1 = Unit) {
                                detectTapGestures(
                                    onDoubleTap = { offset ->
                                        if (offset.x < size.width / 2) {
                                            playbackViewModel.incrementSong(incrementVal = -5000)
                                            showSkipLeft = true
                                        } else {
                                            playbackViewModel.incrementSong(incrementVal = 5000)
                                            showSkipRight = true
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(size = 28.dp)
                                )
                                .blur(radius = 20.dp)
                        )

                        AsyncImage(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(shape = RoundedCornerShape(size = 24.dp)),
                            model = currentSong?.songArtUri,
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(shape = RoundedCornerShape(size = 24.dp))
                        ) {
                            SkipIndicator(
                                visible = showSkipLeft,
                                text = "-5s",
                                alignment = Alignment.CenterStart
                            )
                            SkipIndicator(
                                visible = showSkipRight,
                                text = "+5s",
                                alignment = Alignment.CenterEnd
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = currentSong?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                    Text(
                        text = currentSong?.artists ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { playbackViewModel.seekTo(position = it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(minimumValue = 1f),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(ms = currentPosition),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatTime(ms = duration),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                currentSong?.songId?.let(block = libraryViewModel::toggleFavourite)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id =
                                    if (currentSong?.isFavourite == true) R.drawable.song_favourite_true
                                    else R.drawable.song_favourite
                                ),
                                contentDescription = "Favourite",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(onClick = { playbackViewModel.skipToPrevious() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.song_previous),
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Play Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .blur(radius = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.White, CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { playbackViewModel.togglePlayPause() },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        painter = painterResource(id =
                                            if (isPlaying) R.drawable.song_pause
                                            else if (duration in 1..currentPosition) R.drawable.song_restart
                                            else R.drawable.song_play
                                        ),
                                        contentDescription = "Play/Pause",
                                        tint = Color.Black,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { playbackViewModel.skipToNext() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.song_next),
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(onClick = { playbackViewModel.toggleRepeatMode() }) {
                            val iconRes = when (repeatMode) {
                                Player.REPEAT_MODE_ALL -> R.drawable.song_repeat_all
                                Player.REPEAT_MODE_ONE -> R.drawable.song_repeat_one
                                else -> R.drawable.song_repeat_off
                            }
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = "Repeat",
                                tint =
                                    if (repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(alpha = 0.8f)
                                    else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // SEAMLESS BOTTOM SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(key1 = Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount > 20) isFullScreen = false
                                else if (dragAmount < -20) isFullScreen = true
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            listOf("Up next", "Lyrics", "Details").forEachIndexed { index, title ->
                                TextButton(onClick = { selectedTab = index }) {
                                    Text(
                                        text = title,
                                        color =
                                            if (selectedTab == index) Color.White
                                            else Color.White.copy(alpha = 0.4f),
                                        fontWeight =
                                            if (selectedTab == index) FontWeight.Bold
                                            else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { playbackViewModel.toggleShuffle() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.song_shuffle),
                                    contentDescription = "Shuffle",
                                    tint =
                                        if (playbackViewModel.isShuffleOn) Color.White
                                        else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = { isFullScreen = !isFullScreen }) {
                                Icon(
                                    painter = painterResource(id =
                                        if (isFullScreen) R.drawable.chevron_down
                                        else R.drawable.chevron_up
                                    ),
                                    contentDescription = "Toggle",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                    when (tab) {
                        0 -> { // Queue
                            LazyColumn(
                                state = queueListState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(
                                    items = queue,
                                    key = { _, song -> song.filePath }
                                ) { index, song ->
                                    val isCurrent = song.filePath == currentSong?.filePath
                                    val upEnabled = index > 0
                                    val downEnabled = index < queue.size - 1

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .animateItem(
                                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                                fadeInSpec = null,
                                                fadeOutSpec = spring(stiffness = Spring.StiffnessLow)
                                            )
                                            .clip(shape = RoundedCornerShape(size = 16.dp))
                                            .background(color =
                                                if (isCurrent) Color.White.copy(alpha = 0.2f)
                                                else Color.White.copy(alpha = 0.07f)
                                            )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(start = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (upEnabled) {
                                                            val firstVisible = queueListState.firstVisibleItemIndex
                                                            val firstOffset = queueListState.firstVisibleItemScrollOffset
                                                            playbackViewModel.moveQueueItem(index, index - 1)
                                                            scope.launch {
                                                                delay(timeMillis = 300)
                                                                queueListState.scrollToItem(index = firstVisible, scrollOffset = firstOffset)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    enabled = upEnabled
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.chevron_up),
                                                        contentDescription = null,
                                                        tint = if (upEnabled) Color.White else Color.White.copy(alpha = 0.2f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (downEnabled) {
                                                            val firstVisible = queueListState.firstVisibleItemIndex
                                                            val firstOffset = queueListState.firstVisibleItemScrollOffset
                                                            playbackViewModel.moveQueueItem(index, index + 1)
                                                            scope.launch {
                                                                delay(timeMillis = 300)
                                                                queueListState.scrollToItem(index = firstVisible, scrollOffset = firstOffset)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    enabled = downEnabled
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.chevron_down),
                                                        contentDescription = null,
                                                        tint = if (downEnabled) Color.White else Color.White.copy(alpha = 0.2f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Box(modifier = Modifier.weight(1f)) {
                                                SongRow(
                                                    song = song,
                                                    onClick = { playbackViewModel.playFromQueue(index) },
                                                    showTrackNumbers = false
                                                )
                                            }

                                            IconButton(
                                                onClick = { playbackViewModel.removeFromQueue(index) },
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .size(40.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.options_delete),
                                                    contentDescription = "Remove",
                                                    tint = Color.White.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> { // Lyrics
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Lyrics coming soon...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                        2 -> { // Details
                            SelectionContainer {
                                Column(modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(detailsScrollState)
                                    .padding(all = 24.dp)
                                ) {
                                    Text(
                                        text = "Title: ${currentSong?.title ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Album: ${currentSong?.album ?: "Unknown"} • ${currentSong?.albumArtists ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Artist: ${currentSong?.artists ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MetadataRow(label = "Year", value = currentSong?.year ?: "N/A")
                                    MetadataRow(label = "Duration", value = formatTime(ms = currentSong?.duration ?: 0L))
                                    MetadataRow(label = "Disc", value = "${currentSong?.discNumber ?: 1}")
                                    MetadataRow(label = "Track", value = "${currentSong?.trackNumber ?: 1}")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Technical Details",
                                        color = Color.White.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Size: $fileSizeString",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Path: ${currentSong?.filePath}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkipIndicator(
    visible: Boolean,
    text: String,
    alignment: Alignment,
) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(shape = RoundedCornerShape(size = 24.dp)),
        contentAlignment = alignment
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 80)) + scaleIn(animationSpec = tween(durationMillis = 80), initialScale = 0.7f),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(size = 8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

fun getFileSize(path: String?): Long {
    if (path == null) return 0L
    val file = java.io.File(path)
    return if (file.exists()) file.length() else 0L
}
@Composable
fun MetadataRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.6f)
    )
}