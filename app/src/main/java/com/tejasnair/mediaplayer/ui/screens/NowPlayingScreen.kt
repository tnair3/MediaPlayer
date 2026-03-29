package com.tejasnair.mediaplayer.ui.screens

import android.annotation.SuppressLint
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
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.components.SongRow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

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

    // --- SCREEN STATES ---
    var isFullScreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- PERSISTENT QUEUE STATES (Moved out of Tab to prevent lag/reset) ---
    val queueListState = rememberLazyListState()
    var lockedIndex by remember { mutableIntStateOf(-1) }
    var lockedOffset by remember { mutableIntStateOf(0) }

    // Scroll Anchor Fix (The "Neutralizer")
    LaunchedEffect(queueListState) {
        snapshotFlow { queueListState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (lockedIndex != -1 && currentIndex != lockedIndex) {
                    queueListState.scrollToItem(lockedIndex, lockedOffset)
                }
            }
    }

    ThemedScreen {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. TOP SECTION (The Player)
            AnimatedVisibility(
                visible = !isFullScreen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX > threshold) viewModel.skipToPreviousForce()
                                    else if (offsetX < -threshold) viewModel.skipToNext()
                                    scope.launch {
                                        animate(initialValue = offsetX, targetValue = 0f) { value, _ -> offsetX = value }
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
                        IconButton(onClick = { /* Fav */ }) {
                            Icon(painterResource(R.drawable.song_favourite), "Fav", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = { viewModel.skipToPrevious() }) {
                            Icon(painterResource(R.drawable.song_previous), "Prev", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(
                                    if(isPlaying) R.drawable.song_pause
                                    else if(currentPosition == duration) R.drawable.song_restart
                                    else R.drawable.song_play
                                ),
                                contentDescription = "Play/Pause",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.skipToNext() }) {
                            Icon(painterResource(R.drawable.song_next), "Next", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                            val iconRes = when (repeatMode) {
                                Player.REPEAT_MODE_ALL -> R.drawable.song_repeat_all
                                Player.REPEAT_MODE_ONE -> R.drawable.song_repeat_one
                                else -> R.drawable.song_repeat_off
                            }
                            Icon(painterResource(iconRes), "Repeat", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. BOTTOM SECTION
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column {
                    // --- HEADER ZONE ---
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
                                .padding(top = 12.dp)
                                .size(width = 40.dp, height = 4.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                listOf("Up next", "Lyrics", "Details").forEachIndexed { index, title ->
                                    TextButton(onClick = { selectedTab = index }) {
                                        Text(
                                            text = title,
                                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { isFullScreen = !isFullScreen }) {
                                Icon(
                                    painter = painterResource(if(isFullScreen) R.drawable.chevron_down else R.drawable.chevron_up),
                                    contentDescription = "Toggle Expand",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // --- CONTENT ZONE ---
                    Crossfade(
                        targetState = selectedTab,
                        label = "TabTransition") { tab ->
                        when (tab) {
                            0 -> { // QUEUE TAB
                                LazyColumn(
                                    state = queueListState,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    itemsIndexed(
                                        items = queue,
                                        key = { _, song -> song.filePath }
                                    ) { index, song ->

                                        val isCurrent = remember(currentSong?.filePath, song.filePath) {
                                            song.filePath == currentSong?.filePath
                                        }

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
                                                    if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
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
                                                                lockedIndex = queueListState.firstVisibleItemIndex
                                                                lockedOffset = queueListState.firstVisibleItemScrollOffset
                                                                viewModel.moveQueueItem(index, index - 1)
                                                                scope.launch { delay(300); lockedIndex = -1 }
                                                            }
                                                        },
                                                        modifier = Modifier.size(28.dp),
                                                        enabled = upEnabled
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.chevron_up),
                                                            contentDescription = null,
                                                            tint = if (upEnabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            if (downEnabled) {
                                                                lockedIndex = queueListState.firstVisibleItemIndex
                                                                lockedOffset = queueListState.firstVisibleItemScrollOffset
                                                                viewModel.moveQueueItem(index, index + 1)
                                                                scope.launch { delay(300); lockedIndex = -1 }
                                                            }
                                                        },
                                                        modifier = Modifier.size(28.dp),
                                                        enabled = downEnabled
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.chevron_down),
                                                            contentDescription = null,
                                                            tint = if (downEnabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    SongRow(
                                                        song = song,
                                                        onClick = { viewModel.playFromQueue(index) },
                                                        showTrackNumbers = false
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.removeFromQueue(index) },
                                                    modifier = Modifier.padding(end = 8.dp).size(40.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.options_delete),
                                                        contentDescription = "Remove",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> { // LYRICS TAB
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Lyrics coming soon...", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            2 -> { // DETAILS TAB
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(text = "Title: ${currentSong?.title ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Artist: ${currentSong?.artists ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = "Technical Details", color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Path: ${currentSong?.filePath}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}