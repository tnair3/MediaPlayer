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

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    navController: NavController
) {

    val songs by viewModel.allSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()

    var selectedFilter by remember { mutableIntStateOf(0) }

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
                    onUploadClick = { navController.navigate("upload") },
                    onSettingsClick = { navController.navigate("settings") })

                if (!songs.isEmpty()) {
                    FilterRow(
                        options = listOf("Albums", "Songs", "Artists", "Playlists"),
                        selectedIndex = selectedFilter,
                        onOptionSelected = { selectedFilter = it }
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )


                if(songs.isEmpty()) {
                    EmptyLibrary(
                        primaryText = "Library is Empty",
                        secondaryText = "Upload media to listen"
                    )
                }
                else {
                    when (selectedFilter) {
                        0 -> { // ALBUMS
                            DisplayList(
                                items = albums,
                                title = { it.album },
                                subtitle = { it.albumArtists },
                                // PRIORITY LOGIC: Use Back Cover if it exists, else Front
                                artModel = { it.backCoverUri ?: it.songArtUri },
                                trackNumber = { -1 }, // Don't show numbers in album list
                                onClick = { /* Navigate to Album Detail */ }
                            )
                        }

                        1 -> { // SONGS
                            DisplayList(
                                items = songs,
                                title = { it.title },
                                subtitle = { it.artists },
                                artModel = { it.songArtUri },
                                trackNumber = { it.trackNumber },
                                onClick = { /* Play Song */ }
                            )
                        }

                        2 -> { // ARTISTS
                            DisplayList(
                                items = artists,
                                title = { it }, // 'it' is just the String name
                                subtitle = { "Artist" },
                                artModel = { -1 }, // No art for individual artists yet
                                trackNumber = { -1 },
                                onClick = { /* Navigate to Artist Detail */ }
                            )
                        }
                    }
                }
            }
        }
    }
}