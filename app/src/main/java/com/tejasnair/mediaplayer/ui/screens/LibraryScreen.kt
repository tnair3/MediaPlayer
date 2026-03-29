package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tejasnair.mediaplayer.ui.theme.*
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.FilterRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.TopNavigation
import android.net.Uri
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    navController: NavController,
    playbackViewModel: PlaybackViewModel
) {
    val songs by viewModel.allSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()

    val options = listOf("Songs", "Albums", "Artists", "Playlists")

    val pagerState = rememberPagerState(pageCount = { options.size })
    val coroutineScope = rememberCoroutineScope()

    var selectedSong by remember { mutableStateOf<Song?>(null) }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
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

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                if (songs.isEmpty()) {
                    EmptyLibrary(
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
                                DisplayList(
                                    items = songs,
                                    title = { it.title },
                                    subtitle = { it.artists },
                                    artModel = { it.songArtUri },
                                    trackNumber = { -1 },
                                    onClick = { selectedSong = it }
                                )
                            }

                            1 -> { // ALBUMS
                                DisplayList(
                                    items = albums,
                                    title = { it.album },
                                    subtitle = { it.albumArtists },
                                    artModel = { it.backCoverUri ?: it.songArtUri },
                                    trackNumber = { -1 },
                                    onClick = { album ->
                                        val encodedName = Uri.encode(album.album)
                                        val encodedArtist = Uri.encode(album.albumArtists)
                                        navController.navigate("album/$encodedName/$encodedArtist")
                                    }
                                )
                            }

                            2 -> { // ARTISTS
                                DisplayList(
                                    items = artists,
                                    title = { it },
                                    subtitle = { "Artist" },
                                    artModel = { -1 },
                                    trackNumber = { -1 },
                                    onClick = {  }
                                )
                            }

                            3 -> { // PLAYLISTS
                                EmptyLibrary(
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
                onDelete = { viewModel.deleteSong(it) },
                onDismiss = { selectedSong = null }
            )
        }
    }
}