package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.data.model.Song
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlin.collections.emptyList
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.DiscHeader
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import java.util.Locale

@Composable
fun AlbumScreen(
    albumName: String,
    albumArtist: String,
    viewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel
) {

    val albumSongs by viewModel.getSongsByAlbum(albumName, albumArtist).collectAsState(initial = emptyList())
    val groupedSongs = albumSongs.groupBy { it.discNumber }

    val firstSong = albumSongs.firstOrNull()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    val albumYear = firstSong?.year

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

                AsyncImage(
                    model = firstSong?.backCoverUri ?: firstSong?.songArtUri ?: -1,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .padding(22.dp)
                        .size(256.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = albumName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = albumArtist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                var tertiaryText: String = albumSongs.size.toString() + " songs"
                if(albumYear != null) {
                    tertiaryText = "$tertiaryText • $albumYear"
                }

                if (albumSongs.isNotEmpty()) {
                    Text(
                        text = tertiaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    Color.Transparent
                                )
                            )
                        )
                )

                LazyColumn {
                    groupedSongs.forEach { (discNumber, discSongs) ->

                        item(key = "disc_header_$discNumber") {
                            if (groupedSongs.size > 1) {
                                DiscHeader(discNumber = discNumber)
                            }
                        }

                        items(
                            items = discSongs,
                            key = { it.filePath }
                        ) { song ->
                            SongRow(
                                song = song,
                                onClick = { selectedSong = song }
                            )
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