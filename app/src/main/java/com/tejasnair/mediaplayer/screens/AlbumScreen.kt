package com.tejasnair.mediaplayer.screens

import android.view.Display
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.data.Album
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.components.DisplayList
import com.tejasnair.mediaplayer.data.Song
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.tejasnair.mediaplayer.components.SongSheet
import java.util.Locale

@Composable
fun AlbumScreen(
    album: Album,
    viewModel: LibraryViewModel
) {
    val songs: List<String> = viewModel.albumsToSongs.value[album.id] ?: emptyList()
    val songsList: List<Song> = songs.mapNotNull { id -> viewModel.songs[id] }
    var selectedSong by remember { mutableStateOf<Song?>(null) }

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

                AsyncImage(
                    model = album.artModel,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .padding(22.dp)
                        .size(256.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = album.albumArtist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                var tertiaryLabel = songsList.count().toString() + " songs"

                if(album.year != null) {
                    tertiaryLabel += " • " + album.year
                }

                Text(
                    text = tertiaryLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .height(1.dp) // Your thickness
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

                selectedSong?.let { song ->
                    SongSheet(
                        song = song,
                        onDismiss = { selectedSong = null }
                    )
                }

                DisplayList(
                    items = songsList.sortedWith(
                        comparator = compareBy(
                            { it.discNumber }, { it.trackNumber })),
                    title = { it.title },
                    subtitle = { it.artist.name + " • " + String.format(
                        Locale.getDefault(),
                        format = "%02d:%02d",
                        it.duration / 60, it.duration % 60
                    )
                               },
                    artModel = { it.artModel },
                    trackNumber = { it.trackNumber },
                    onClick = { song -> selectedSong = song }
                )
            }
        }
    }
}