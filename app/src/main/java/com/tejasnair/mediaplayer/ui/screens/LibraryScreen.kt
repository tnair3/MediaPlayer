package com.tejasnair.mediaplayer.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.*
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

// Sort Model

enum class SortDirection { Ascending, Descending }

sealed class SortOption(val label: String, val iconRes: Int) {
    object SongName   : SortOption("Name",    R.drawable.sort_name)
    object SongAlbum  : SortOption("Album",   R.drawable.sort_album)
    object SongArtist : SortOption("Artist",  R.drawable.sort_artist)
    object SongYear   : SortOption("Year",    R.drawable.sort_year)

    object AlbumName   : SortOption("Name",   R.drawable.sort_name)
    object AlbumArtist : SortOption("Artist", R.drawable.sort_artist)
    object AlbumYear   : SortOption("Year",   R.drawable.sort_year)

    object ArtistName      : SortOption("Name",            R.drawable.sort_name)
    object ArtistSongCount : SortOption("Number of Songs", R.drawable.sort_number)
}

// Screen

@SuppressLint("DefaultLocale")
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController,
    showNowPlaying: MutableState<Boolean>
) {
    val songs by libraryViewModel.allSongs.collectAsState()
    val albums by libraryViewModel.albums.collectAsState()
    val artists by libraryViewModel.artists.collectAsState()

    val options = listOf("Songs", "Albums", "Artists", "Playlists")
    val focusManager = LocalFocusManager.current
    val pagerState = rememberPagerState(pageCount = { options.size })
    val coroutineScope = rememberCoroutineScope()

    var selectedSongId by remember { mutableStateOf<String?>(null) }
    val selectedSong = songs.find { it.songId == selectedSongId }

    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    var songSortOption by remember { mutableStateOf<SortOption>(SortOption.SongName) }
    var songSortDirection by remember { mutableStateOf(SortDirection.Ascending) }
    var albumSortOption by remember { mutableStateOf<SortOption>(SortOption.AlbumName) }
    var albumSortDirection by remember { mutableStateOf(SortDirection.Ascending) }
    var artistSortOption by remember { mutableStateOf<SortOption>(SortOption.ArtistName) }
    var artistSortDirection by remember { mutableStateOf(SortDirection.Ascending) }

    val currentPage = pagerState.currentPage

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
        if (songSortDirection == SortDirection.Descending) sorted.reversed() else sorted
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
        if (albumSortDirection == SortDirection.Descending) sorted.reversed() else sorted
    }

    val filteredArtists = remember(searchQuery, artists, artistSortOption, artistSortDirection) {
        val filtered = if (searchQuery.isBlank()) artists
        else artists.filter { it.contains(searchQuery, ignoreCase = true) }
        val sorted = when (artistSortOption) {
            SortOption.ArtistName      -> filtered.sortedBy { it.lowercase() }
            SortOption.ArtistSongCount -> filtered
            else -> filtered
        }
        if (artistSortDirection == SortDirection.Descending) sorted.reversed() else sorted
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopNavigation(
                    title = "Library",
                    onVinylClick = { navController.navigate("vinyls") },
                    onFavouriteClick = { navController.navigate("favourites") },
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
                }

                if (songs.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play button
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

                            // Shuffle button
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

                            // Sort + Search grouped tightly together
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sort button
                                Box {
                                    IconButton(
                                        onClick = { showSortMenu = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.sort),
                                            contentDescription = "Sort",
                                            modifier = Modifier.size(20.dp),
                                            tint = if (isSortActive(currentPage, songSortOption, songSortDirection, albumSortOption, albumSortDirection, artistSortOption, artistSortDirection))
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
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
                                        // Direction toggle — does NOT close the menu
                                        val currentDirection = when (currentPage) {
                                            0 -> songSortDirection
                                            1 -> albumSortDirection
                                            2 -> artistSortDirection
                                            else -> SortDirection.Ascending
                                        }
                                        val directionIcon = if (currentDirection == SortDirection.Ascending)
                                            R.drawable.sort_modeasc else R.drawable.sort_modedesc
                                        val directionLabel = if (currentDirection == SortDirection.Ascending)
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
                                                // Toggle direction without closing
                                                val newDir = if (currentDirection == SortDirection.Ascending)
                                                    SortDirection.Descending else SortDirection.Ascending
                                                when (currentPage) {
                                                    0 -> songSortDirection = newDir
                                                    1 -> albumSortDirection = newDir
                                                    2 -> artistSortDirection = newDir
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

                                        // Sort options — selecting one closes the menu
                                        when (currentPage) {
                                            0 -> SortOptionGroup(
                                                sortOptions = listOf(
                                                    SortOption.SongName,
                                                    SortOption.SongAlbum,
                                                    SortOption.SongArtist,
                                                    SortOption.SongYear
                                                ),
                                                currentOption = songSortOption,
                                                onOptionSelected = { opt ->
                                                    songSortOption = opt
                                                    showSortMenu = false
                                                }
                                            )
                                            1 -> SortOptionGroup(
                                                sortOptions = listOf(
                                                    SortOption.AlbumName,
                                                    SortOption.AlbumArtist,
                                                    SortOption.AlbumYear
                                                ),
                                                currentOption = albumSortOption,
                                                onOptionSelected = { opt ->
                                                    albumSortOption = opt
                                                    showSortMenu = false
                                                }
                                            )
                                            2 -> SortOptionGroup(
                                                sortOptions = listOf(
                                                    SortOption.ArtistName,
                                                    SortOption.ArtistSongCount
                                                ),
                                                currentOption = artistSortOption,
                                                onOptionSelected = { opt ->
                                                    artistSortOption = opt
                                                    showSortMenu = false
                                                }
                                            )
                                            else -> Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
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

                                // Search button
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
                                    Icon(
                                        painter = painterResource(R.drawable.search),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                painter = painterResource(R.drawable.close),
                                                contentDescription = "Clear search",
                                                modifier = Modifier.size(20.dp)
                                            )
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
                                        secondaryText = "No songs match \"$searchQuery\""
                                    )
                                } else {
                                    DisplayList(
                                        items = filteredSongs,
                                        title = { it.title },
                                        subtitle = { it.artists },
                                        artModel = { it.songArtUri },
                                        trackNumber = { -1 },
                                        trackDuration = { formatTime(it.duration) },
                                        onClick = { selectedSongId = it.songId },
                                        isFavourite = { it.isFavourite },
                                    )
                                }
                            }
                            1 -> {
                                if (filteredAlbums.isEmpty()) {
                                    EmptyLibrary(
                                        imageId = R.drawable.disp_empty_library,
                                        primaryText = "No Results",
                                        secondaryText = "No albums match \"$searchQuery\""
                                    )
                                } else {
                                    DisplayList(
                                        items = filteredAlbums,
                                        title = { it.album },
                                        subtitle = { it.albumArtists },
                                        artModel = { it.backCoverUri ?: it.songArtUri },
                                        trackNumber = { -1 },
                                        trackDuration = { "" },
                                        onClick = { album ->
                                            val encodedName = Uri.encode(album.album)
                                            val encodedArtist = Uri.encode(album.albumArtists)
                                            navController.navigate("album/$encodedName/$encodedArtist")
                                        },
                                        isFavourite = { false },
                                    )
                                }
                            }
                            2 -> {
                                if (filteredArtists.isEmpty()) {
                                    EmptyLibrary(
                                        imageId = R.drawable.disp_empty_library,
                                        primaryText = "No Results",
                                        secondaryText = "No artists match \"$searchQuery\""
                                    )
                                } else {
                                    DisplayList(
                                        items = filteredArtists,
                                        title = { it },
                                        subtitle = { "Artist" },
                                        artModel = { -1 },
                                        trackNumber = { -1 },
                                        trackDuration = { "" },
                                        onClick = { },
                                        isFavourite = { false },
                                    )
                                }
                            }
                            3 -> {
                                EmptyLibrary(
                                    imageId = R.drawable.disp_empty_library,
                                    primaryText = "No Playlists",
                                    secondaryText = "Create a playlist to see it here"
                                )
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

// Sort Dropdown Components

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
            tint =
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { onOptionSelected(option) }
        )
    }
}

// Sort Helpers

private fun isSortActive(
    currentPage: Int,
    songSort: SortOption, songDir: SortDirection,
    albumSort: SortOption, albumDir: SortDirection,
    artistSort: SortOption, artistDir: SortDirection
): Boolean = when (currentPage) {
    0 -> songSort !is SortOption.SongName || songDir != SortDirection.Ascending
    1 -> albumSort !is SortOption.AlbumName || albumDir != SortDirection.Ascending
    2 -> artistSort !is SortOption.ArtistName || artistDir != SortDirection.Ascending
    else -> false
}