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
import java.util.Locale

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    navController: NavController
) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }
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
                    onVinylClick = { },
                    onFavouriteClick = { navController.navigate("favourites") },
                    onUploadClick = { navController.navigate("upload") },
                    onSettingsClick = { navController.navigate("settings") })

                if (!viewModel.songs.values.isEmpty()) {
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

                selectedSong?.let { song ->
                    SongSheet(
                        song = song,
                        onDismiss = { selectedSong = null }
                    )
                }

                if (viewModel.songs.values.isEmpty()) {
                    EmptyLibrary(
                        primaryText = "Library is empty",
                        secondaryText = "Upload media to listen"
                    )
                }
                else {
                    when (selectedFilter) {
                        0 -> DisplayList(
                            items = viewModel.albums.values.toList().sortedBy { it.title },
                            title = { it.title },
                            subtitle = { it.albumArtist.name },
                            artModel = { it.artModel },
                            trackNumber = { -1 },
                            onClick = { album ->
                                navController.navigate("album/${album.id}")
                            }
                        )
                        1 -> DisplayList(
                            items = viewModel.songs.values.toList().sortedWith(
                                comparator = compareBy(
                                    { it.album?.title }, { it.discNumber }, { it.trackNumber })
                            ),
                            title = { it.title },
                            subtitle = {
                                it.artist.name + " • " + String.format(
                                    Locale.getDefault(),
                                    format = "%02d:%02d",
                                    it.duration / 60, it.duration % 60
                                )
                            },
                            artModel = { it.artModel },
                            trackNumber = { -1 },
                            onClick = { song -> selectedSong = song }
                        )
                        2 -> DisplayList(
                            items = viewModel.artists.values.toList().sortedBy { it.name },
                            title = { it.name },
                            subtitle = { "" },
                            artModel = { -1 },
                            trackNumber = { -1 },
                            onClick = { }
                        )
                        else -> { }
                    }
                }
            }
        }
    }
}