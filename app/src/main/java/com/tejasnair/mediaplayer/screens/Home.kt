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
import kotlin.time.Duration

@Composable
fun HomeScreen(viewModel: LibraryViewModel = viewModel()) {
    val songs = viewModel.songs.value

    LaunchedEffect(Unit) {
        if (viewModel.songs.value.isEmpty()) {
            val album = Album(
                id = "album1",
                title = "Test Album",
                albumArtist = "Test Artist"
            )

            val testSongs = listOf(
                Song("1", "Song One", "Test Artist", 180, album = album),
                Song("2", "Song Two", "Test Artist", 200, album = album)
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
                FilterRow(
                    listOf("Albums", "Songs", "Playlists", "Artists"),
                    selectedIndex = selectedFilter,
                    onOptionSelected = { selectedFilter = it }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                if (songs.isEmpty()) {
                    EmptyLibrary()
                }
                else {
                    LibraryDisplay(songs)
                }
            }
        }
    }
}