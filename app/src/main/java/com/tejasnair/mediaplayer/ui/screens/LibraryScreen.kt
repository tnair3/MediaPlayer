package com.tejasnair.mediaplayer.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.FilterRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.SortOption
import com.tejasnair.mediaplayer.ui.components.StyledDropdownItem
import com.tejasnair.mediaplayer.ui.components.TopNavigation
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

enum class SortDirection { ASC, DESC }

@SuppressLint("UnrememberedMutableState")
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController,
    showNowPlaying: MutableState<Boolean>
) {
    val songs by libraryViewModel.allSongs.collectAsState()
    val albums by libraryViewModel.albums.collectAsState()
    val playlists by libraryViewModel.allPlaylists.collectAsState()

    val options = listOf("Songs", "Albums", "Playlists")
    val focusManager = LocalFocusManager.current
    val pagerState = rememberPagerState(pageCount = { options.size })
    val coroutineScope = rememberCoroutineScope()

    var selectedSongId by remember { mutableStateOf<String?>(null) }
    val selectedSong by remember { derivedStateOf { songs.find { it.songId == selectedSongId } } }

    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    var songSortOption by remember { mutableStateOf<SortOption>(SortOption.SongName) }
    var songSortDirection by remember { mutableStateOf(SortDirection.ASC) }
    var albumSortOption by remember { mutableStateOf<SortOption>(SortOption.AlbumName) }
    var albumSortDirection by remember { mutableStateOf(SortDirection.ASC) }
    var playlistSortOption by remember { mutableStateOf<SortOption>(SortOption.PlaylistName) }
    var playlistSortDirection by remember { mutableStateOf(SortDirection.ASC) }

    val currentPage = pagerState.currentPage

    val playlistSongCounts by remember(playlists) {
        if (playlists.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(emptyMap())
        } else {
            combine(
                playlists.map { pl ->
                    libraryViewModel.getPlaylistSongCount(pl.playlistId)
                        .map { count -> pl.playlistId to count }
                }
            ) { pairs -> pairs.toMap() }
        }
    }.collectAsState(initial = emptyMap())

    val sortActive by derivedStateOf {
        when (currentPage) {
            0 -> songSortOption !is SortOption.SongName || songSortDirection != SortDirection.ASC
            1 -> albumSortOption !is SortOption.AlbumName || albumSortDirection != SortDirection.ASC
            2 -> playlistSortOption !is SortOption.PlaylistName || playlistSortDirection != SortDirection.ASC
            else -> false
        }
    }

    val currentSortDirection by derivedStateOf {
        when (currentPage) {
            0 -> songSortDirection
            1 -> albumSortDirection
            2 -> playlistSortDirection
            else -> SortDirection.ASC
        }
    }

    val filteredSongs = remember(searchQuery, songs, songSortOption, songSortDirection) {
        val filtered = if (searchQuery.isBlank()) songs
        else songs.filter { song ->
            song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artists.contains(searchQuery, ignoreCase = true) ||
                    song.album.contains(searchQuery, ignoreCase = true)
        }
        val sorted = when (songSortOption) {
            SortOption.SongName   -> filtered.sortedBy { it.title.lowercase() }
            SortOption.SongAlbum  -> filtered.sortedBy { it.album.lowercase() }
            SortOption.SongArtist -> filtered.sortedBy { it.artists.lowercase() }
            SortOption.SongYear   -> filtered.sortedBy { it.year ?: "" }
            else -> filtered
        }
        if (songSortDirection == SortDirection.DESC) sorted.reversed() else sorted
    }

    val filteredAlbums = remember(searchQuery, albums, albumSortOption, albumSortDirection) {
        val filtered = if (searchQuery.isBlank()) albums
        else albums.filter { album ->
            album.album.contains(searchQuery, ignoreCase = true) ||
                    album.albumArtists.contains(searchQuery, ignoreCase = true)
        }
        val sorted = when (albumSortOption) {
            SortOption.AlbumName   -> filtered.sortedBy { it.album.lowercase() }
            SortOption.AlbumArtist -> filtered.sortedBy { it.albumArtists.lowercase() }
            SortOption.AlbumYear   -> filtered.sortedBy { it.year ?: "" }
            else -> filtered
        }
        if (albumSortDirection == SortDirection.DESC) sorted.reversed() else sorted
    }

    val filteredPlaylists = remember(searchQuery, playlists, playlistSortOption, playlistSortDirection, playlistSongCounts) {
        val filtered = if (searchQuery.isBlank()) playlists
        else playlists.filter { it.playlistName.contains(searchQuery, ignoreCase = true) }
        val sorted = when (playlistSortOption) {
            SortOption.PlaylistName -> filtered.sortedBy { it.playlistName.lowercase() }
            SortOption.PlaylistSongCount -> filtered.sortedBy { playlistSongCounts[it.playlistId] ?: 0 }
            else -> filtered.sortedBy { it.playlistName.lowercase() }
        }
        if (playlistSortDirection == SortDirection.DESC) sorted.reversed() else sorted
    }

    // Create Playlist dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newPlaylistName.isNotBlank(),
                    onClick = {
                        libraryViewModel.createPlaylist(newPlaylistName.trim())
                        showCreatePlaylistDialog = false
                        newPlaylistName = ""
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) { Text("Cancel") }
            }
        )
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopNavigation(
                    title = "Library",
                    onFavouriteClick = { navController.navigate("favourites") },
                    onVinylClick = { navController.navigate("vinyls") },
                    onRecordedClick = { navController.navigate("record") },
                    onUploadClick = { navController.navigate("upload") },
                    onSettingsClick = { navController.navigate("settings") }
                )

                if (songs.isNotEmpty()) {
                    FilterRow(
                        options = options,
                        selectedIndex = pagerState.currentPage,
                        onOptionSelected = { index ->
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    playbackViewModel.playSong(songs.first(), songs)
                                    showNowPlaying.value = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(painterResource(R.drawable.song_play), null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Play")
                            }

                            OutlinedButton(
                                onClick = {
                                    val shuffled = songs.shuffled()
                                    playbackViewModel.playSong(shuffled.first(), shuffled)
                                    showNowPlaying.value = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(painterResource(R.drawable.song_shuffle), null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Shuffle")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    IconButton(
                                        onClick = { showSortMenu = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.sort),
                                            contentDescription = "Sort",
                                            modifier = Modifier.size(20.dp),
                                            tint = if (sortActive) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false },
                                        offset = DpOffset(x = (-10).dp, y = (-4).dp),
                                        shape = RoundedCornerShape(20.dp),
                                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                        modifier = Modifier.width(210.dp)
                                    ) {
                                        val directionIcon = if (currentSortDirection == SortDirection.ASC)
                                            R.drawable.sort_modeasc else R.drawable.sort_modedesc
                                        val directionLabel = if (currentSortDirection == SortDirection.ASC)
                                            "Ascending" else "Descending"

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = directionLabel,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                            RoundedCornerShape(8.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(directionIcon),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                val newDir = if (currentSortDirection == SortDirection.ASC)
                                                    SortDirection.DESC else SortDirection.ASC
                                                when (currentPage) {
                                                    0 -> songSortDirection = newDir
                                                    1 -> albumSortDirection = newDir
                                                    2 -> playlistSortDirection = newDir
                                                }
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(Modifier.height(4.dp))

                                        when (currentPage) {
                                            0 -> SortOptionGroup(
                                                sortOptions = listOf(SortOption.SongName, SortOption.SongAlbum, SortOption.SongArtist, SortOption.SongYear),
                                                currentOption = songSortOption,
                                                onOptionSelected = { songSortOption = it; showSortMenu = false }
                                            )
                                            1 -> SortOptionGroup(
                                                sortOptions = listOf(SortOption.AlbumName, SortOption.AlbumArtist, SortOption.AlbumYear),
                                                currentOption = albumSortOption,
                                                onOptionSelected = { albumSortOption = it; showSortMenu = false }
                                            )
                                            2 -> SortOptionGroup(
                                                sortOptions = listOf(SortOption.PlaylistName, SortOption.PlaylistSongCount),
                                                currentOption = playlistSortOption,
                                                onOptionSelected = { playlistSortOption = it; showSortMenu = false }
                                            )
                                            else -> {}
                                        }

                                        Spacer(Modifier.height(4.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        showSearch = !showSearch
                                        if (!showSearch) searchQuery = ""
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Crossfade(targetState = showSearch) { isOpen ->
                                        Icon(
                                            painter = painterResource(
                                                if (isOpen) R.drawable.close else R.drawable.search
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (showSearch) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = showSearch) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                ),
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.search), null, Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(painterResource(R.drawable.close), "Clear", Modifier.size(20.dp))
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                if (songs.isEmpty()) {
                    EmptyLibrary(R.drawable.disp_empty_library, "Library is Empty", "Upload media to listen")
                } else {
                    val isPlayerActive = playbackViewModel.currentSongId != null
                    val totalBottomPadding = if (isPlayerActive) 64.dp else 0.dp

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                        beyondViewportPageCount = 1
                    ) { pageIndex ->
                        when (pageIndex) {
                            0 -> {
                                if (filteredSongs.isEmpty()) {
                                    EmptyLibrary(R.drawable.disp_empty_library, "No Results", "No songs match \"$searchQuery\"")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            bottom = totalBottomPadding,
                                            top = 0.dp,
                                            start = 0.dp,
                                            end = 0.dp
                                        )
                                    ) {
                                        items(filteredSongs) { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedSongId = item.songId }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                val containerShape = RoundedCornerShape(12.dp)

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(containerShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                        .drawWithContent {
                                                            drawContent()
                                                            if (item.isFavourite) {
                                                                val gradientWidth = 20.dp.toPx()
                                                                val cornerRadius = 12.dp.toPx()

                                                                val leftPath = Path().apply {
                                                                    moveTo(0f, 0f)
                                                                    lineTo(gradientWidth, 0f)

                                                                    cubicTo(
                                                                        gradientWidth - cornerRadius, 0f,
                                                                        gradientWidth - cornerRadius, cornerRadius,
                                                                        gradientWidth - cornerRadius, cornerRadius
                                                                    )

                                                                    lineTo(gradientWidth - cornerRadius, size.height - cornerRadius)

                                                                    cubicTo(
                                                                        gradientWidth - cornerRadius, size.height - cornerRadius,
                                                                        gradientWidth - cornerRadius, size.height,
                                                                        gradientWidth, size.height
                                                                    )

                                                                    lineTo(0f, size.height)
                                                                    close()
                                                                }

                                                                drawPath(
                                                                    path = leftPath,
                                                                    brush = Brush.horizontalGradient(
                                                                        colors = listOf(
                                                                            Color(0xFFFF2A6D).copy(alpha = 0.4f),
                                                                            Color(0xFFE91E63).copy(alpha = 0.2f),
                                                                            Color.Transparent
                                                                        ),
                                                                        startX = 0f,
                                                                        endX = gradientWidth
                                                                    )
                                                                )

                                                                val rightPath = Path().apply {
                                                                    moveTo(size.width, 0f)
                                                                    lineTo(size.width - gradientWidth, 0f)

                                                                    cubicTo(
                                                                        size.width - gradientWidth + cornerRadius, 0f,
                                                                        size.width - gradientWidth + cornerRadius, cornerRadius,
                                                                        size.width - gradientWidth + cornerRadius, cornerRadius
                                                                    )

                                                                    lineTo(size.width - gradientWidth + cornerRadius, size.height - cornerRadius)

                                                                    cubicTo(
                                                                        size.width - gradientWidth + cornerRadius, size.height - cornerRadius,
                                                                        size.width - gradientWidth + cornerRadius, size.height,
                                                                        size.width - gradientWidth, size.height
                                                                    )

                                                                    lineTo(size.width, size.height)
                                                                    close()
                                                                }

                                                                drawPath(
                                                                    path = rightPath,
                                                                    brush = Brush.horizontalGradient(
                                                                        colors = listOf(
                                                                            Color.Transparent,
                                                                            Color(0xFFE91E63).copy(alpha = 0.2f),
                                                                            Color(0xFFFF2A6D).copy(alpha = 0.4f)
                                                                        ),
                                                                        startX = size.width - gradientWidth,
                                                                        endX = size.width
                                                                    )
                                                                )
                                                            }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = item.songArtUri,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.title, style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Text(
                                                                text = item.artists,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.weight(1f, fill = false)
                                                            )
                                                            Text(
                                                                modifier = Modifier.padding(end = 8.dp),
                                                                text = " • ${formatTime(item.duration)}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (filteredAlbums.isEmpty()) {
                                    EmptyLibrary(R.drawable.disp_empty_library, "No Results", "No albums match \"$searchQuery\"")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            bottom = totalBottomPadding,
                                            top = 0.dp,
                                            start = 0.dp,
                                            end = 0.dp
                                        )
                                    ) {
                                        items(filteredAlbums) { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val encodedAlbum = Uri.encode(item.album)
                                                        val encodedArtist = Uri.encode(item.albumArtists)
                                                        navController.navigate("album/$encodedAlbum/$encodedArtist") {
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = item.backCoverUri ?: item.songArtUri,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.album, style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text(item.albumArtists, style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                LazyColumn(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        bottom = totalBottomPadding,
                                        top = 0.dp,
                                        start = 0.dp,
                                        end = 0.dp
                                    )
                                ) {
                                    item(key = "create_playlist") {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable { showCreatePlaylistDialog = true }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(28.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .padding(horizontal = 10.dp, vertical = 12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.add),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "Create New Playlist",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))
                                    }

                                    if (filteredPlaylists.isEmpty() && searchQuery.isNotBlank()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "No playlists match \"$searchQuery\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else if (filteredPlaylists.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Spacer(Modifier.height(16.dp))
                                                    Text("No Playlists", style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                                    Spacer(Modifier.height(4.dp))
                                                    Text("Create a playlist to see it here",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    } else {
                                        items(filteredPlaylists, key = { it.playlistId }) { playlist ->
                                            val songCount by libraryViewModel.getPlaylistSongCount(playlist.playlistId)
                                                .collectAsState(initial = 0)

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        navController.navigate("playlist/${playlist.playlistId}")
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(vertical = 4.dp)
                                                        )
                                                    {
                                                        Text(playlist.playlistName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text(
                                                            text = if (songCount == 1) "1 song" else "$songCount songs",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }
            }
        }

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playbackViewModel = playbackViewModel,
                libraryViewModel = libraryViewModel,
                onDelete = { libraryViewModel.deleteSong(it) },
                onDismiss = { selectedSongId = null },
                showNowPlaying = showNowPlaying
            )
        }
    }
}

@Composable
private fun SortOptionGroup(
    sortOptions: List<SortOption>,
    currentOption: SortOption,
    onOptionSelected: (SortOption) -> Unit
) {
    sortOptions.forEach { option ->
        val isSelected = option::class == currentOption::class
        StyledDropdownItem(
            icon = option.iconRes,
            label = option.label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { onOptionSelected(option) }
        )
    }
}