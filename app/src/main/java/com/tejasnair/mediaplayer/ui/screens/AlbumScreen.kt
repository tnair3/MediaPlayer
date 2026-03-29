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
import androidx.compose.ui.unit.DpOffset
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
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
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.DiscHeader
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.foundation.layout.Row

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

    // Dropdown state
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showDeleteAlbumDialog by remember { mutableStateOf(false) }
    var showEditAlbumDialog by remember { mutableStateOf(false) }
    var showConfirmEditDeleteDialog by remember { mutableStateOf(false) }

    // Tracks which songs are checked in the edit dialog
    val songsSelectedForDeletion = remember { mutableStateSetOf<String>() }

    // --- Dialogs ---

    if (showDeleteAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAlbumDialog = false },
            title = { Text("Delete Album") },
            text = { Text("Are you sure you want to delete all songs in $albumName? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    albumSongs.forEach { viewModel.deleteSong(it) }
                    showDeleteAlbumDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlbumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditAlbumDialog) {
        AlertDialog(
            modifier = Modifier.padding(vertical = 28.dp),
            onDismissRequest = {
                showEditAlbumDialog = false
                songsSelectedForDeletion.clear()
            },
            title = { Text("Select Songs to Delete") },
            text = {
                Column {
                    // Select all toggle row
                    val allSelected = songsSelectedForDeletion.size == albumSongs.size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                if (checked) songsSelectedForDeletion.addAll(albumSongs.map { it.filePath })
                                else songsSelectedForDeletion.clear()
                            }
                        )
                        Text(
                            text = "Select All",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

                    // Scrollable song list
                    LazyColumn {
                        items(albumSongs) { song ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = song.filePath in songsSelectedForDeletion,
                                    onCheckedChange = { checked ->
                                        if (checked) songsSelectedForDeletion.add(song.filePath)
                                        else songsSelectedForDeletion.remove(song.filePath)
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = song.artists,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = songsSelectedForDeletion.isNotEmpty(),
                    onClick = { showConfirmEditDeleteDialog = true }
                ) {
                    Text(
                        "Delete (${songsSelectedForDeletion.size})",
                        color = if (songsSelectedForDeletion.isNotEmpty())
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditAlbumDialog = false
                    songsSelectedForDeletion.clear()
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmEditDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmEditDeleteDialog = false },
            title = { Text("Delete Songs") },
            text = { Text("Are you sure you want to delete ${songsSelectedForDeletion.size} song(s)? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    albumSongs
                        .filter { it.filePath in songsSelectedForDeletion }
                        .forEach { viewModel.deleteSong(it) }
                    songsSelectedForDeletion.clear()
                    showConfirmEditDeleteDialog = false
                    showEditAlbumDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmEditDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Main UI ---

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
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = albumName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = albumArtist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                var tertiaryText = albumSongs.size.toString() + " songs"
                if (albumYear != null) tertiaryText = "$tertiaryText • $albumYear"

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
                            if (groupedSongs.size > 1) DiscHeader(discNumber = discNumber)
                        }
                        items(items = discSongs, key = { it.filePath }) { song ->
                            SongRow(
                                song = song,
                                onClick = { selectedSong = song },
                                showTrackNumbers = true
                            )
                        }
                    }
                }
            }

            // Options icon button + dropdown anchored to TopEnd
            Box(modifier = Modifier.align(TopEnd)) {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.options),
                        contentDescription = "Album Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    modifier = Modifier.padding(end = 16.dp),
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    offset = DpOffset(x = (-10).dp, y = (-10).dp)

                ) {
                    DropdownMenuItem(
                        text = { Text("Play Album") },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.song_play), contentDescription = null)
                        },
                        onClick = {
                            showOptionsMenu = false
                            playbackViewModel.playSong(albumSongs.first(), albumSongs)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Shuffle Album") },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.song_shuffle), contentDescription = null)
                        },
                        onClick = {
                            showOptionsMenu = false
                            val shuffled = albumSongs.shuffled()
                            playbackViewModel.playSong(shuffled.first(), shuffled)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue") },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.song_options_addtoqueue), contentDescription = null)
                        },
                        onClick = {
                            showOptionsMenu = false
                            playbackViewModel.addAlbumToQueue(albumSongs)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Save to Playlist") },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.song_options_playlist), contentDescription = null)
                        },
                        onClick = {
                            showOptionsMenu = false
                            // Playlist system not yet implemented
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Edit Album") },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.options_edit), contentDescription = null)
                        },
                        onClick = {
                            showOptionsMenu = false
                            songsSelectedForDeletion.clear()
                            showEditAlbumDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Album", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.options_delete),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showOptionsMenu = false
                            showDeleteAlbumDialog = true
                        }
                    )
                }
            }
        }

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playlist = albumSongs,
                playbackViewModel = playbackViewModel,
                onDelete = { viewModel.deleteSong(it) },
                onDismiss = { selectedSong = null }
            )
        }
    }
}