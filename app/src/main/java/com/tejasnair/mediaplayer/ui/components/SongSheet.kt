package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun SongSheet(
    song: Song,
    playlist: List<Song>? = null,
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
                .width(320.dp)
                .widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = song.songArtUri ?: -1,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(256.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = song.artists.ifEmpty { "Unknown Artist" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Play/Delete/Fav Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(painterResource(
                            id = R.drawable.options_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(
                        onClick = {
                            playbackViewModel.playSong(song, playlist)
                            onDismiss()
                        },
                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(painterResource(
                            id = R.drawable.song_play),
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp))
                    }

                    IconButton(onClick = { /* Fav */ }, modifier = Modifier.size(32.dp)) {
                        Icon(painterResource(
                            id = R.drawable.song_favourite),
                            contentDescription = "Favourite",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // The List Options
                Column(modifier = Modifier.fillMaxWidth()) {
                    OptionItem(R.drawable.song_options_playnext, "Play next") {
                        playbackViewModel.addToNext(song)
                        onDismiss()
                    }
                    OptionItem(R.drawable.song_options_addtoqueue, "Add to queue") {
                        playbackViewModel.addToQueue(song)
                        onDismiss()
                    }
                    OptionItem(R.drawable.song_options_playlist, "Save to playlist") {
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
fun OptionItem(iconRes: Int, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(painterResource(iconRes), null, Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
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