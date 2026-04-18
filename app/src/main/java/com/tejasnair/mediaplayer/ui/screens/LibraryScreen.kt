package com.tejasnair.mediaplayer.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
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

    var selectedSongId by remember { mutableStateOf<String?>(value = null) }
    val selectedSong by remember { derivedStateOf { songs.find { it.songId == selectedSongId } } }

    var showSearch by remember { mutableStateOf(value = false) }
    var searchQuery by remember { mutableStateOf(value = "") }
    var showSortMenu by remember { mutableStateOf(value = false) }

    var songSortOption by remember { mutableStateOf<SortOption>(value = SortOption.SongName) }
    var songSortDirection by remember { mutableStateOf(value = SortDirection.ASC) }
    var albumSortOption by remember { mutableStateOf<SortOption>(value = SortOption.AlbumName) }
    var albumSortDirection by remember { mutableStateOf(value = SortDirection.ASC) }

    val currentPage = pagerState.currentPage

    val sortActive by derivedStateOf {
        isSortActive(currentPage,
            songSortOption, songDir = songSortDirection,
            albumSortOption, albumDir = albumSortDirection
        )
    }

    val currentSortDirection by derivedStateOf {
        when (currentPage) {
            0 -> songSortDirection
            1 -> albumSortDirection
            else -> SortDirection.ASC
        }
    }

    val filteredSongs = remember(searchQuery, songs, songSortOption, songSortDirection) {
        val filtered = if (searchQuery.isBlank()) songs
        else songs.filter { song ->
            song.title.contains(other = searchQuery, ignoreCase = true) ||
                    song.artists.contains(other = searchQuery, ignoreCase = true) ||
                    song.album.contains(other = searchQuery, ignoreCase = true)
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
            album.album.contains(other = searchQuery, ignoreCase = true) ||
                    album.albumArtists.contains(other = searchQuery, ignoreCase = true)
        }
        val sorted = when (albumSortOption) {
            SortOption.AlbumName   -> filtered.sortedBy { it.album.lowercase() }
            SortOption.AlbumArtist -> filtered.sortedBy { it.albumArtists.lowercase() }
            SortOption.AlbumYear   -> filtered.sortedBy { it.year ?: "" }
            else -> filtered
        }
        if (albumSortDirection == SortDirection.DESC) sorted.reversed() else sorted
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .pointerInput(key1 = Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopNavigation(
                    title = "Library",
                    onVinylClick = { navController.navigate(route = "vinyls") },
                    onFavouriteClick = { navController.navigate(route = "favourites") },
                    onRecordedClick = { navController.navigate(route = "record") },
                    onUploadClick = { navController.navigate(route = "upload") },
                    onSettingsClick = { navController.navigate(route = "settings") }
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
                                    playbackViewModel.playSong(selectedSong = songs.first(), playlist = songs)
                                    showNowPlaying.value = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.song_play),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Play")
                            }

                            OutlinedButton(
                                onClick = {
                                    val shuffled = songs.shuffled()
                                    playbackViewModel.playSong(selectedSong = shuffled.first(), playlist = shuffled)
                                    showNowPlaying.value = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(size = 14.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.song_shuffle),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
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
                                            painter = painterResource(id = R.drawable.sort),
                                            contentDescription = "Sort",
                                            modifier = Modifier.size(20.dp),
                                            tint =
                                                if (sortActive) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false },
                                        offset = DpOffset(x = (-10).dp, y = (-4).dp),
                                        shape = RoundedCornerShape(size = 20.dp),
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
                                                            color = MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.1f
                                                            ),
                                                            shape = RoundedCornerShape(size = 8.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = directionIcon),
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
                                            else -> Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(all = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No sort options for this tab",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
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
                                            painter = painterResource(id =
                                                if (isOpen) R.drawable.close
                                                else R.drawable.search),
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
                                    Icon(
                                        painter = painterResource(id = R.drawable.search),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.close),
                                                contentDescription = "Clear search",
                                                modifier = Modifier.size(20.dp))
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
                    EmptyLibrary(
                        imageId = R.drawable.disp_empty_library,
                        primaryText = "Library is Empty",
                        secondaryText = "Upload media to listen"
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                        beyondViewportPageCount = 1
                    ) { pageIndex ->
                        when (pageIndex) {
                            0 -> {
                                if (filteredSongs.isEmpty()) {
                                    EmptyLibrary(
                                        imageId = R.drawable.disp_empty_library,
                                        primaryText = "No Results",
                                        secondaryText = "No songs match \"$searchQuery\"")
                                }
                                else {
                                    LazyColumn {
                                        items(filteredSongs) { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedSongId = item.songId }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                // Outer card-like row container
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.3f
                                                            )
                                                        )
                                                        .padding(
                                                            horizontal = 10.dp,
                                                            vertical = 8.dp
                                                        )
                                                ) {

                                                    AsyncImage(
                                                        model = item.songArtUri,
                                                        contentDescription = "Album Art",
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.title,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "${item.artists} • ${formatTime(ms = item.duration)}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    if (item.isFavourite) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.song_favourite_true),
                                                            contentDescription = "Favourite",
                                                            tint = MaterialTheme.colorScheme.surfaceVariant,
                                                            modifier = Modifier
                                                                .padding(start = 8.dp)
                                                                .size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (filteredAlbums.isEmpty()) {
                                    EmptyLibrary(
                                        imageId = R.drawable.disp_empty_library,
                                        primaryText = "No Results",
                                        secondaryText = "No albums match \"$searchQuery\"")
                                }
                                else {
                                    LazyColumn {
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
                                                // Outer card-like row container
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.3f
                                                            )
                                                        )
                                                        .padding(
                                                            horizontal = 10.dp,
                                                            vertical = 8.dp
                                                        )
                                                ) {

                                                    AsyncImage(
                                                        model = item.backCoverUri ?: item.songArtUri,
                                                        contentDescription = "Album Art",
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.album,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = item.albumArtists,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                if (playlists.isEmpty()) {
                                    EmptyLibrary(
                                        imageId = R.drawable.disp_empty_library,
                                        primaryText = "No Playlists",
                                        secondaryText = "Create a playlist to see it here")
                                }
                                else {
                                    LazyColumn {
                                        items(playlists) { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {  }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                // Outer card-like row container
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.playlistName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "Number of songs",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
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

private fun isSortActive(
    currentPage: Int,
    songSort: SortOption, songDir: SortDirection,
    albumSort: SortOption, albumDir: SortDirection,
): Boolean = when (currentPage) {
    0 -> songSort !is SortOption.SongName || songDir != SortDirection.ASC
    1 -> albumSort !is SortOption.AlbumName || albumDir != SortDirection.ASC
    else -> false
}