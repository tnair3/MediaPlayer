package com.tejasnair.mediaplayer.ui.screens

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

// 2. Compose Animation
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring

// 3. Compose Runtime
import androidx.compose.runtime.*

// 4. Material3
import androidx.compose.material3.*
import androidx.compose.ui.Alignment

// 5. Media3
import androidx.media3.common.Player

// 6. External Libraries
import coil.compose.AsyncImage

// 7. Coroutines
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 8. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun NowPlayingScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    onBackClick: () -> Unit
) {
    val allSongs by libraryViewModel.allSongs.collectAsState()

    val currentSong = allSongs.find {
        it.songId == playbackViewModel.currentSongId
    }

    val queue = playbackViewModel.currentQueue.mapNotNull { id ->
        allSongs.find { it.songId == id }
    }

    val isPlaying = playbackViewModel.isPlaying
    val currentPosition = playbackViewModel.currentPosition
    val duration = playbackViewModel.duration
    val repeatMode = playbackViewModel.repeatMode

    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val threshold = 300f

    var isFullScreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val queueListState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {

        // --- FULL BLEED BLURRED BACKGROUND ---
        AsyncImage(
            model = currentSong?.songArtUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp),
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
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX > threshold) playbackViewModel.skipToPreviousForce()
                                    else if (offsetX < -threshold) playbackViewModel.skipToNext()
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
                    // Crisp art card floating over blurred background
                    Box(contentAlignment = Alignment.Center) {
                        // Soft glow behind the art
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .background(
                                    Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(28.dp)
                                )
                                .blur(20.dp)
                        )
                        AsyncImage(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            model = currentSong?.songArtUri,
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop
                        )
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
                        onValueChange = { playbackViewModel.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
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
                            text = formatTime(currentPosition),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatTime(duration),
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
                                currentSong?.songId?.let(libraryViewModel::toggleFavourite)
                            }
                        ) {
                            Icon(
                                painter = painterResource(
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
                                painterResource(R.drawable.song_previous),
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
                                    .blur(8.dp)
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
                                        painter = painterResource(
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
                                painterResource(R.drawable.song_next),
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
                                painterResource(iconRes),
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
                        .pointerInput(Unit) {
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
                                        color = if (selectedTab == index) Color.White
                                        else Color.White.copy(alpha = 0.4f),
                                        fontWeight = if (selectedTab == index) FontWeight.Bold
                                        else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { playbackViewModel.toggleShuffle() }) {
                                Icon(
                                    painter = painterResource(R.drawable.song_shuffle),
                                    contentDescription = "Shuffle",
                                    tint =
                                        if (playbackViewModel.isShuffleOn) Color.White
                                        else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = { isFullScreen = !isFullScreen }) {
                                Icon(
                                    painter = painterResource(
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

                // A subtle separator line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                    when (tab) {
                        0 -> {
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
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
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
                                                                delay(300)
                                                                queueListState.scrollToItem(firstVisible, firstOffset)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    enabled = upEnabled
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.chevron_up),
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
                                                                delay(300)
                                                                queueListState.scrollToItem(firstVisible, firstOffset)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    enabled = downEnabled
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.chevron_down),
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
                                                    painter = painterResource(R.drawable.options_delete),
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
                        1 -> {
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
                        2 -> {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Title: ${currentSong?.title ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Artist: ${currentSong?.artists ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Technical Details",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
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