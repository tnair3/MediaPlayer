package com.tejasnair.mediaplayer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tejasnair.mediaplayer.components.*
import com.tejasnair.mediaplayer.ui.theme.*
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.viewmodel.LibraryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.data.Album
import com.tejasnair.mediaplayer.data.Song
import com.tejasnair.mediaplayer.data.Artist

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    navController: NavController
) {
    LaunchedEffect(Unit) {
        if (viewModel.songs.values.isEmpty()) {
            val testArtist = Artist(name = "Test Artist")

            val album1 = Album(
                title = "Test Album 1",
                albumArtist = testArtist
            )

            val testSongs = listOf(
                Song("1", "Song One", testArtist, 180, album = album1),
                Song("2", "Song Two", testArtist, 200)
            )

            testSongs.forEach { viewModel.addSong(it) }
        }
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Main content in a Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                TopNavigation(
                    title = "Library",
                    onUploadClick = { },
                    onSettingsClick = { })

                var selectedFilter by remember { mutableIntStateOf(0) }
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

                if (viewModel.songs.values.isEmpty()) {
                    EmptyLibrary()
                }
                else {
                    when (selectedFilter) {
                        0 -> DisplayList(
                            items = viewModel.albums.values.toList(),
                            title = { it.title },
                            subtitle = { it.albumArtist.name },
                            onClick = {  }
                        )
                        1 -> DisplayList(
                            items = viewModel.songs.values.toList(),
                            title = { it.title },
                            subtitle = { it.artist.name },
                            onClick = {  }
                        )
                        2 -> DisplayList(
                            items = viewModel.artists.values.toList(),
                            title = { it.name },
                            subtitle = { "" },
                            onClick = {  }
                        )
                        else -> { }
                    }
                }
            }
        }
    }
}