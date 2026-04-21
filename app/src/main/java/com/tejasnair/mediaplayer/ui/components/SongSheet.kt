package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Playlist
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun SongSheet(
    song: Song,
    playlist: List<Song>? = null,
    playbackViewModel: PlaybackViewModel,
    libraryViewModel: LibraryViewModel,
    onDelete: (Song) -> Unit,
    onDismiss: () -> Unit,
    showNowPlaying: MutableState<Boolean>
) {
    val playlists by libraryViewModel.allPlaylists.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            songTitle = song.title,
            onConfirm = {
                showDeleteDialog = false
                onDelete(song)
                onDismiss()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Playlist picker dialog
    if (showPlaylistPicker) {
        AlertDialog(
            onDismissRequest = { showPlaylistPicker = false },
            title = { Text("Add to Playlist") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                showPlaylistPicker = false
                                showNewPlaylistDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(painterResource(R.drawable.add), null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("New Playlist", color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    if (playlists.isEmpty()) {
                        item {
                            Text("No playlists yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(playlists) { pl ->
                            val count by libraryViewModel.getPlaylistSongCount(pl.playlistId)
                                .collectAsState(initial = 0)
                            TextButton(
                                onClick = {
                                    libraryViewModel.addSongToPlaylist(song.songId, pl.playlistId, count)
                                    showPlaylistPicker = false
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(painterResource(R.drawable.song_options_playlist), null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(pl.playlistName, color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text("$count songs",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPlaylistPicker = false }) { Text("Cancel") }
            }
        )
    }

    // New playlist dialog (from song sheet)
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newPlaylistName.isNotBlank(),
                    onClick = {
                        // Create the playlist then add song to it.
                        // We generate the ID here so we can use it immediately.
                        val newId = java.util.UUID.randomUUID().toString()
                        val newPlaylist = Playlist(
                            playlistId = newId,
                            playlistName = newPlaylistName.trim()
                        )
                        libraryViewModel.createPlaylistWithId(newPlaylist)
                        libraryViewModel.addSongToPlaylist(song.songId, newId, 0)
                        showNewPlaylistDialog = false
                        newPlaylistName = ""
                        onDismiss()
                    }
                ) { Text("Create & Add") }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
            ),
            modifier = Modifier.width(300.dp).widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = song.songArtUri ?: -1,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .padding(top = 4.dp).fillMaxWidth().aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(16.dp))

                Text(song.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(song.artists.ifEmpty { "Unknown Artist" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(40.dp)) {
                            Icon(painterResource(R.drawable.options_delete), "Delete",
                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        }
                    }

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                        Box(modifier = Modifier.size(68.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape).blur(8.dp))
                        Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape).clip(CircleShape),
                            contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = {
                                    playbackViewModel.playSong(song, playlist)
                                    onDismiss()
                                    showNowPlaying.value = true
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(painterResource(R.drawable.song_play), "Play",
                                    tint = Color.Black, modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.size(40.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { libraryViewModel.toggleFavourite(song.songId) }, modifier = Modifier.size(40.dp)) {
                            Icon(painterResource(if (song.isFavourite) R.drawable.song_favourite_true else R.drawable.song_favourite),
                                "Favourite", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    OptionItem(R.drawable.song_options_playnext, "Play next") {
                        playbackViewModel.addToNext(song); onDismiss()
                    }
                    OptionItem(R.drawable.song_options_addtoqueue, "Add to queue") {
                        playbackViewModel.addToQueue(song); onDismiss()
                    }
                    OptionItem(R.drawable.song_options_playlist, "Save to playlist") {
                        showPlaylistPicker = true
                    }
                }
            }
        }
    }
}

// OptionItem, DeleteConfirmationDialog unchanged — keep as-is from your existing file
@Composable
fun OptionItem(iconRes: Int, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier.size(32.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(iconRes), null,
                    modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun DeleteConfirmationDialog(songTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Song") },
        text = { Text("Are you sure you want to remove '$songTitle' from your library?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}