package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
            primaryText = "Delete Song",
            secondaryText = "Are you sure you want to remove \"${song.title}\" from your library?",
            onConfirm = {
                showDeleteDialog = false
                onDelete(song)
                onDismiss()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

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
                                Icon(
                                    painterResource(R.drawable.add),
                                    contentDescription = "New",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "New Playlist",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    if (playlists.isEmpty()) {
                        item {
                            Text(
                                "No playlists yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else {
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
                                    Icon(
                                        painterResource(R.drawable.song_options_playlist),
                                        contentDescription = "Add to Playlist",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            pl.playlistName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "$count songs",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .widthIn(max = 460.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(24.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
                        RoundedCornerShape(32.dp)
                    )
                    .blur(20.dp)
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = song.songArtUri ?: -1,
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .blur(24.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.55f),
                                        Color.Black.copy(alpha = 0.72f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                                    .blur(20.dp)
                            )
                            AsyncImage(
                                model = song.songArtUri ?: -1,
                                contentDescription = "Album Art",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee(),
                            color = Color.White
                        )
                        Text(
                            song.artists.ifEmpty { "Unknown Artist" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painterResource(R.drawable.options_delete),
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                        .blur(8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.White, CircleShape)
                                        .clip(CircleShape), contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            playbackViewModel.playSong(song, playlist)
                                            onDismiss()
                                            showNowPlaying.value = true
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.song_play),
                                            "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { libraryViewModel.toggleFavourite(song.songId) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painterResource(id = if (song.isFavourite) R.drawable.song_favourite_true else R.drawable.song_favourite),
                                        contentDescription = "Favourite",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(Modifier.height(4.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            OptionItem(R.drawable.song_options_playnext, "Play next") { playbackViewModel.addToNext(song); onDismiss() }
                            OptionItem(R.drawable.song_options_addtoqueue, "Add to queue") { playbackViewModel.addToQueue(song); onDismiss() }
                            OptionItem(R.drawable.song_options_playlist, "Save to playlist") { showPlaylistPicker = true }
                        }
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
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
    }
}