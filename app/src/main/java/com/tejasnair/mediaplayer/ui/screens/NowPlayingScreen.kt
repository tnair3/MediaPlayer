package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.formatBytes
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.components.StyledDropdownItem
import com.tejasnair.mediaplayer.ui.components.Waveform
import com.tejasnair.mediaplayer.ui.components.MetadataRow
import com.tejasnair.mediaplayer.ui.components.SkipIndicator
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    onBackClick: () -> Unit,
    expansionFraction: Float = 1f
) {
    val allSongs by libraryViewModel.allSongs.collectAsState()
    val currentSong = allSongs.find { it.songId == playbackViewModel.currentSongId }
    val queue = playbackViewModel.currentQueue.mapNotNull { id -> allSongs.find { it.songId == id } }

    val isPlaying = playbackViewModel.isPlaying
    val currentPosition = playbackViewModel.currentPosition
    val duration = playbackViewModel.duration
    val repeatMode = playbackViewModel.repeatMode

    val interactionSource = remember { MutableInteractionSource() }
    val isDragging by interactionSource.collectIsDraggedAsState()

    var sliderThumbValue by remember { mutableFloatStateOf(0f) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition, isDragging) {
        if (!isDragging) {
            sliderThumbValue = currentPosition.toFloat()
        }
    }

    val fileSizeString = remember(currentSong?.filePath) {
        val size = getFileSize(currentSong?.filePath)
        if (size > 0) formatBytes(size) else "Unknown"
    }

    val scope = rememberCoroutineScope()
    var showSkipLeft by remember { mutableStateOf(false) }
    var showSkipRight by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val threshold = 300f

    val queueListState = rememberLazyListState()
    val detailsScrollState = rememberScrollState()

    val pagerState = rememberPagerState(pageCount = { 2 })

    val currentQueueIndex = remember(queue, currentSong) {
        queue.indexOfFirst { it.songId == currentSong?.songId }
    }
    val nextSong = remember(queue, currentQueueIndex) {
        if (currentQueueIndex >= 0 && currentQueueIndex < queue.size - 1)
            queue[currentQueueIndex + 1]
        else null
    }

    val dynamicArtSize = (44 + (216 * expansionFraction)).dp

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        AsyncImage(
            model = currentSong?.songArtUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(24.dp),
            contentScale = ContentScale.Crop
        )

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

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> {
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
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    painter = painterResource(R.drawable.chevron_down),
                                    contentDescription = "Collapse",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = currentSong?.album ?: "Now Playing",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Context Menu Container
                            Box {
                                IconButton(onClick = { showOptionsMenu = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.options),
                                        contentDescription = "Playback Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showOptionsMenu,
                                    onDismissRequest = { showOptionsMenu = false },
                                    offset = DpOffset(x = (-10).dp, y = 0.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                    modifier = Modifier.width(200.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "Queue Options",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    StyledDropdownItem(
                                        icon = R.drawable.options_delete,
                                        label = "Dismiss Queue",
                                        tint = MaterialTheme.colorScheme.error
                                    ) {
                                        showOptionsMenu = false
                                        playbackViewModel.stopPlayback()
                                        onBackClick()
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }

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
                                    alpha = 1f - (kotlin.math.abs(offsetX) / (threshold * 2f)).coerceAtMost(0.5f)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LaunchedEffect(showSkipLeft) {
                                if (showSkipLeft) {
                                    delay(600.milliseconds); showSkipLeft = false
                                }
                            }
                            LaunchedEffect(showSkipRight) {
                                if (showSkipRight) {
                                    delay(600.milliseconds); showSkipRight = false
                                }
                            }

                            Box(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { offset ->
                                            if (offset.x < size.width / 2) {
                                                playbackViewModel.incrementSong(-10000)
                                                showSkipLeft = true
                                            } else {
                                                playbackViewModel.incrementSong(10000)
                                                showSkipRight = true
                                            }
                                        }
                                    )
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dynamicArtSize + 20.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.08f),
                                            RoundedCornerShape(28.dp)
                                        )
                                        .blur(20.dp)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .size(dynamicArtSize)
                                        .clip(RoundedCornerShape(24.dp)),
                                    model = currentSong?.songArtUri,
                                    contentDescription = "Song Art",
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier.size(dynamicArtSize).clip(RoundedCornerShape(24.dp))
                                ) {
                                    SkipIndicator(
                                        visible = showSkipLeft,
                                        text = "-10s",
                                        alignment = Alignment.CenterStart
                                    )
                                    SkipIndicator(
                                        visible = showSkipRight,
                                        text = "+10s",
                                        alignment = Alignment.CenterEnd
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

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
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(20.dp))

                            Slider(
                                value = sliderThumbValue,
                                onValueChange = { sliderThumbValue = it },
                                onValueChangeFinished = {
                                    playbackViewModel.seekTo(sliderThumbValue.toLong())
                                },
                                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                                interactionSource = interactionSource,
                                modifier = Modifier.fillMaxWidth(),
                                track = { sliderState ->
                                    val fraction = (sliderState.value - sliderState.valueRange.start) /
                                            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                },
                                thumb = {
                                    Spacer(Modifier.size(24.dp))
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    formatTime(currentPosition),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatTime(duration),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                IconButton(onClick = { currentSong?.songId?.let(libraryViewModel::toggleFavourite) }) {
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
                                        "Previous",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(72.dp).background(
                                            Color.White.copy(alpha = 0.15f),
                                            CircleShape
                                        ).blur(8.dp)
                                    )
                                    Box(
                                        modifier = Modifier.size(60.dp)
                                            .background(Color.White, CircleShape).clip(CircleShape),
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
                                        "Next",
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
                                        painterResource(iconRes), "Repeat",
                                        tint = if (repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(
                                            alpha = 0.8f
                                        ) else Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (nextSong != null) {
                                    AsyncImage(
                                        model = nextSong.songArtUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Up Next",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = nextSong.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = nextSong.artists,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Up Next",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "End of queue",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.4f)
                                        )
                                    }
                                }

                                IconButton(onClick = { playbackViewModel.toggleShuffle() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.song_shuffle),
                                        contentDescription = "Shuffle",
                                        tint = if (playbackViewModel.isShuffleOn) Color.White else Color.White.copy(
                                            alpha = 0.4f
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.chevron_right),
                                        contentDescription = "More",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (currentQueueIndex >= 0) "Song ${currentQueueIndex + 1} of ${queue.size}" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        SelectionContainer {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .heightIn(max = 120.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentSong?.year ?: "N/A",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Disc ${currentSong?.discNumber ?: "1"} • Track ${currentSong?.trackNumber ?: "1"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "$fileSizeString • ${formatTime(currentSong?.duration ?: 0L)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Waveform(modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterHorizontally))
                    }
                }

                1 -> {
                    var selectedTab by remember { mutableIntStateOf(0) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(0) } }) {
                                Icon(
                                    painter = painterResource(R.drawable.chevron_left),
                                    contentDescription = "Back to player",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Row {
                                listOf("Up next", "Lyrics", "Details").forEachIndexed { index, title ->
                                    TextButton(onClick = { selectedTab = index }) {
                                        Text(
                                            text = title,
                                            color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.4f),
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.size(48.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )

                        Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                            when (tab) {
                                0 -> {
                                    LazyColumn(state = queueListState, modifier = Modifier.fillMaxSize()) {
                                        itemsIndexed(items = queue, key = { _, song -> song.filePath }) { index, song ->
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
                                                                        delay(300.milliseconds)
                                                                        queueListState.scrollToItem(firstVisible, firstOffset)
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.size(28.dp),
                                                            enabled = upEnabled
                                                        ) {
                                                            Icon(
                                                                painterResource(R.drawable.chevron_up), null,
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
                                                                        delay(300.milliseconds)
                                                                        queueListState.scrollToItem(firstVisible, firstOffset)
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.size(28.dp),
                                                            enabled = downEnabled
                                                        ) {
                                                            Icon(
                                                                painterResource(R.drawable.chevron_down), null,
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
                                                        modifier = Modifier.padding(end = 8.dp).size(40.dp)
                                                    ) {
                                                        Icon(
                                                            painterResource(R.drawable.options_delete), "Remove",
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
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Lyrics coming soon...", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                                2 -> {
                                    SelectionContainer {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(detailsScrollState)
                                                .padding(24.dp)
                                        ) {
                                            Text("Title: ${currentSong?.title ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                            Text("Album: ${currentSong?.album ?: "Unknown"} • ${currentSong?.albumArtists ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                            Text("Artist: ${currentSong?.artists ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                            Spacer(Modifier.height(4.dp))
                                            MetadataRow("Year", currentSong?.year ?: "N/A")
                                            MetadataRow("Duration", formatTime(currentSong?.duration ?: 0L))
                                            MetadataRow("Disc", "${currentSong?.discNumber ?: 1}")
                                            MetadataRow("Track", "${currentSong?.trackNumber ?: 1}")
                                            Spacer(Modifier.height(16.dp))
                                            Text("Technical Details", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium)
                                            Spacer(Modifier.height(4.dp))
                                            Text("Size: $fileSizeString", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                                            Text("Path: ${currentSong?.filePath}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getFileSize(path: String?): Long {
    if (path == null) return 0L
    val file = java.io.File(path)
    return if (file.exists()) file.length() else 0L
}