package com.tejasnair.mediaplayer.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun SongSheet(
    song: Song,
    playbackViewModel: PlaybackViewModel,
    onDelete: (Song) -> Unit,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        DeleteConfirmationDialog(
            songTitle = song.title,
            onConfirm = {
                showDialog = false
                onDelete(song)
                onDismiss()
            },
            onDismiss = { showDialog = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = song.songArtUri ?: song.songArtUri ?: -1,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 12.dp)
                        .size(256.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = song.artists.ifEmpty { "Unknown Artist" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.options_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            playbackViewModel.playSong(song)
                            onDismiss()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.song_play),
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {  },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.song_options_playnext),
                            contentDescription = "Play Next",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {  },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.song_options_addtoqueue),
                            contentDescription = "Add to Queue",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {  },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.song_options_playlist),
                            contentDescription = "Save to Playlist",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    songTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Song") },
        text = { Text("Are you sure you want to remove '$songTitle' from your library?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}