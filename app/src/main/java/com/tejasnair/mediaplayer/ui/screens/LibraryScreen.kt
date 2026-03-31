package com.tejasnair.mediaplayer.ui.screens

// 1. Android & Core
import android.net.Uri

// 2. Compose UI, Layout & Graphics
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// 3. Compose Runtime
import androidx.compose.runtime.*

// 4. Material3
import androidx.compose.material3.*

// 5. Navigation
import androidx.navigation.NavController

// 6. Coroutines
import kotlinx.coroutines.launch

// 7. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.FilterRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.TopNavigation
import com.tejasnair.mediaplayer.ui.theme.*
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController,
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
    var searchQuery by remember { mutableStateOf("") }

    // Filtered lists — recomputed whenever searchQuery or the source list changes
    val filteredSongs = remember(searchQuery, songs) {
        if (searchQuery.isBlank()) songs
        else songs.filter { song ->
            song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artists.contains(searchQuery, ignoreCase = true) ||
                    song.album.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredAlbums = remember(searchQuery, albums) {
        if (searchQuery.isBlank()) albums
        else albums.filter { album ->
            album.album.contains(searchQuery, ignoreCase = true) ||
                    album.albumArtists.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredArtists = remember(searchQuery, artists) {
        if (searchQuery.isBlank()) artists
        else artists.filter { artist ->
            artist.contains(searchQuery, ignoreCase = true)
        }
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
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }

                // Search bar — only shown when there is content to search
                if (songs.isNotEmpty()) {
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.large,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
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
                            0 -> { // SONGS
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
                                        onClick = { selectedSongId = it.songId },
                                        isFavourite = { it.isFavourite }
                                    )
                                }
                            }

                            1 -> { // ALBUMS
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
                                        onClick = { album ->
                                            val encodedName = Uri.encode(album.album)
                                            val encodedArtist = Uri.encode(album.albumArtists)
                                            navController.navigate("album/$encodedName/$encodedArtist")
                                        },
                                        isFavourite = { false }
                                    )
                                }
                            }

                            2 -> { // ARTISTS
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
                                        onClick = { },
                                        isFavourite = { false }
                                    )
                                }
                            }

                            3 -> { // PLAYLISTS
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
                onDismiss = { selectedSongId = null }
            )
        }
    }
}