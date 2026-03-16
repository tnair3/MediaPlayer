package com.tejasnair.mediaplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.data.model.Album
import com.tejasnair.mediaplayer.data.model.Artist
import com.tejasnair.mediaplayer.data.model.Song

@Composable
fun TestAdd(
    viewModel: LibraryViewModel
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
}