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
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.data.Album
import com.tejasnair.mediaplayer.data.Song
import com.tejasnair.mediaplayer.data.Artist
import com.tejasnair.mediaplayer.R
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        if (viewModel.songs.values.isEmpty()) {
            val testArtist = Artist(name = "Test Artist")
            val lTestard = Artist(name = "Lorien Testard")
            val sMiyazawa = Artist(name = "Shoi Miyazawa")
            val randomArtist = Artist(name = "Random Artist")

            val album1 = Album(
                title = "Test Album 1",
                albumArtist = testArtist,
                albumArtRes = R.drawable.test_album_art,
                year = 2026
            )

            val e33 = Album(
                title = "Clair Obscur: Expedition 33",
                albumArtist = lTestard,
                albumArtRes = R.drawable.test_art_e33,
                year = 2025
            )

            val nightreign = Album(
                title = "Elden Ring: Nightreign",
                albumArtist = sMiyazawa,
                albumArtRes = R.drawable.test_art_nightreign,
                year = 2025
            )

            val testSongs = listOf(
                Song(title = "Song One", artist = testArtist, duration = 180, album = album1, trackNumber = 1),
                Song(title = "Song Two", artist = testArtist, duration = 200, album = album1, trackNumber = 2),
                Song(title = "Song Three", artist = testArtist, duration =  180, album = album1, trackNumber = 3),
                Song(title = "Song Four", artist = testArtist, duration =  180, album = album1, trackNumber = 4),
                Song(title = "Song Five", artist = testArtist, duration =  180, album = album1, trackNumber = 5),
                Song(title = "Song Six", artist = testArtist, duration =  180, album = album1, trackNumber = 6),
                Song(title = "Song Seven", artist = testArtist, duration =  180, album = album1, trackNumber = 7),
                Song(title = "Random Song 01", artist = randomArtist, duration =  14, trackNumber = 1),
                Song(title = "Random Song 02", artist = randomArtist, duration =  23, trackNumber = 2),
                Song(title = "Alicia", artist = lTestard, duration = 170, album = e33, trackNumber = 1, songArtRes = R.drawable.test_art_e33_one),
                Song(title = "Gustave", artist = lTestard, duration = 228, album = e33, trackNumber = 2, songArtRes = R.drawable.test_art_e33_one),
                Song(title = "Lumière", artist = lTestard, duration = 222, album = e33, trackNumber = 3, songArtRes = R.drawable.test_art_e33_one),
                Song(title = "Une vie à t'aimer", artist = lTestard, duration = 660, album = e33, trackNumber = 60, songArtRes = R.drawable.test_art_e33_one),
                Song(title = "Renoir", artist = lTestard, duration = 183, album = e33, trackNumber = 144, songArtRes = R.drawable.test_art_e33_three),
                Song(title = "Fulghor, Champion of Nightglow", artist = sMiyazawa, duration = 363, album = nightreign, trackNumber = 23, songArtRes = R.drawable.test_art_nightreign),
                Song(title = "Caligo, Miasma of Night", artist = sMiyazawa, duration = 423, album = nightreign, trackNumber = 24, songArtRes = R.drawable.test_art_nightreign),
                Song(title = "Heolstor the Nightlord", artist = sMiyazawa, duration = 472, album = nightreign, trackNumber = 25, songArtRes = R.drawable.test_art_nightreign)
            )

            testSongs.forEach { viewModel.addSong(it) }
        }
    }

    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var selectedFilter by remember { mutableIntStateOf(0) }

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
                    EmptyLibrary()
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
                                    { it.album?.title }, { it.discNumber }, { it.trackNumber })),
                            title = { it.title },
                            subtitle = { it.artist.name + " • " + String.format(
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
                            onClick = {  }
                        )
                        else -> { }
                    }
                }
            }
        }
    }
}