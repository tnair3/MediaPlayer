package com.tejasnair.mediaplayer.ui.screens

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 2. Compose Runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// 3. Material3
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme

// 4. Navigation
import androidx.navigation.NavController

// 5. Kotlin Standard Library
import kotlin.collections.emptyList

// 6. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun FavouritesScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController
) {
    val songs by libraryViewModel.favouriteSongs.collectAsState(initial = emptyList())
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(songs) {
        if (selectedSong != null && selectedSong !in songs) {
            selectedSong = null
        }
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp, top = 16.dp)
            ) {
                StandardUIBar(
                    navController = navController,
                    title = "Favourites"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                if (songs.isEmpty()) {
                    EmptyLibrary(
                        imageId = R.drawable.song_favourite_none,
                        primaryText = "No Favourites",
                        secondaryText = "Add songs to favourites to view here"
                    )
                } else {
                    DisplayList(
                        items = songs,
                        title = { it.title },
                        subtitle = { it.artists },
                        artModel = { it.songArtUri },
                        trackNumber = { -1 },
                        onClick = { selectedSong = it },
                        isFavourite = { false }
                    )
                }
            }
        }

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playlist = songs,
                libraryViewModel = libraryViewModel,
                playbackViewModel = playbackViewModel,
                onDelete = { libraryViewModel.deleteSong(it) },
                onDismiss = { selectedSong = null }
            )
        }
    }
}