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
import com.tejasnair.mediaplayer.data.Album
import com.tejasnair.mediaplayer.data.Song
import com.tejasnair.mediaplayer.data.Artist
import kotlin.time.Duration

@Composable
fun HomeScreen(viewModel: LibraryViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        if (viewModel.songs.value.isEmpty()) {
            val testArtist = Artist("Test Artist")

            val album1 = Album(
                id = "album1",
                title = "Test Album 1",
                albumArtist = testArtist
            )
            val album2 = Album(
                id = "album2",
                title = "Test Album 2",
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

                TopNavigation("Library", { }, { })

                var selectedFilter by remember { mutableIntStateOf(0) }
                if (!viewModel.songs.value.isEmpty()) {
                    FilterRow(
                        listOf("Albums", "Songs", "Artists", "Playlists"),
                        selectedIndex = selectedFilter,
                        onOptionSelected = { selectedFilter = it }
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                if (viewModel.songs.value.isEmpty()) {
                    EmptyLibrary()
                }
                else {
                    when (selectedFilter) {
                        0 -> DisplayList(viewModel.albums.value.keys.toList(), title = { it.title }, subtitle = { it.albumArtist.name })
                        1 -> DisplayList(viewModel.songs.value, title = { it.title }, subtitle = { it.artist.name })
                        2 -> DisplayList(viewModel.artists.value.keys.toList(), title = { it.name }, subtitle = { "" })
                        3 -> { }
                        else -> { }
                    }
                }
            }
        }
    }
}