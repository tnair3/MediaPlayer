package com.tejasnair.mediaplayer.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.StyledDropdownItem
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlaylistScreen(
    playlistId: String,
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController,
    showNowPlaying: MutableState<Boolean>
) {
    val playlists by libraryViewModel.allPlaylists.collectAsState()
    val playlist = playlists.find { it.playlistId == playlistId }
    val playlistSongs by libraryViewModel.getSongsInPlaylist(playlistId).collectAsState(initial = emptyList())
    val favouriteSongs by libraryViewModel.favouriteSongs.collectAsState()
    val favouriteIds by remember(favouriteSongs) {
        derivedStateOf { favouriteSongs.map { it.songId }.toHashSet() }
    }

    var selectedSongId by remember { mutableStateOf<String?>(null) }
    val selectedSong by remember { derivedStateOf { playlistSongs.find { it.songId == selectedSongId } } }

    var showOptionsMenu by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSongsMode by remember { mutableStateOf(false) }
    var editName by remember(playlist?.playlistName) { mutableStateOf(playlist?.playlistName ?: "") }

    LaunchedEffect(playlist) {
        if (playlist == null && playlists.isNotEmpty()) navController.navigateUp()
    }

    // Edit name dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Playlist Name") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editName.isNotBlank(),
                    onClick = {
                        libraryViewModel.updatePlaylistName(playlistId, editName.trim())
                        showEditNameDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete \"${playlist?.playlistName}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    libraryViewModel.deletePlaylist(playlistId)
                    showDeleteDialog = false
                    navController.navigateUp()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
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
                    .padding(bottom = 56.dp)
            ) {

                // Top Title Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(shape = RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.nav_back_arrow),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = playlist?.playlistName ?: "Playlist",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (playlistSongs.size == 1) "1 song" else "${playlistSongs.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.options),
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false },
                                offset = DpOffset(x = (-10).dp, y = (-10).dp),
                                shape = RoundedCornerShape(20.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                modifier = Modifier.width(220.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = playlist?.playlistName ?: "Playlist",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${playlistSongs.size} songs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(4.dp))

                                StyledDropdownItem(R.drawable.song_play, "Play Playlist") {
                                    showOptionsMenu = false
                                    if (playlistSongs.isNotEmpty()) {
                                        playbackViewModel.playSong(playlistSongs.first(), playlistSongs)
                                        showNowPlaying.value = true
                                    }
                                }
                                StyledDropdownItem(R.drawable.song_shuffle, "Shuffle Playlist") {
                                    showOptionsMenu = false
                                    if (playlistSongs.isNotEmpty()) {
                                        val shuffled = playlistSongs.shuffled()
                                        playbackViewModel.playSong(shuffled.first(), shuffled)
                                        showNowPlaying.value = true
                                    }
                                }
                                StyledDropdownItem(R.drawable.song_options_addtoqueue, "Add to Queue") {
                                    showOptionsMenu = false
                                    playlistSongs.forEach { playbackViewModel.addToQueue(it) }
                                }

                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(4.dp))

                                StyledDropdownItem(R.drawable.options_edit, "Edit Details") {
                                    showOptionsMenu = false
                                    editName = playlist?.playlistName ?: ""
                                    showEditNameDialog = true
                                }
                                StyledDropdownItem(R.drawable.options_deletesome, "Edit Songs") {
                                    showOptionsMenu = false
                                    showEditSongsMode = !showEditSongsMode
                                }
                                StyledDropdownItem(
                                    icon = R.drawable.options_delete,
                                    label = "Delete Playlist",
                                    tint = MaterialTheme.colorScheme.error
                                ) {
                                    showOptionsMenu = false
                                    showDeleteDialog = true
                                }

                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Play + Shuffle row
                if (playlistSongs.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 12.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                playbackViewModel.playSong(playlistSongs.first(), playlistSongs)
                                showNowPlaying.value = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(painterResource(R.drawable.song_play), "Play", Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play")
                        }
                        OutlinedButton(
                            onClick = {
                                val shuffled = playlistSongs.shuffled()
                                playbackViewModel.playSong(shuffled.first(), shuffled)
                                showNowPlaying.value = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(painterResource(R.drawable.song_shuffle), "Shuffle", Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Shuffle")
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                // Gradient divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(Modifier.height(4.dp))

                // Song list
                if (playlistSongs.isEmpty()) {
                    EmptyLibrary(
                        imageId = R.drawable.disp_empty_library,
                        primaryText = "Empty Playlist",
                        secondaryText = "Add songs from the library"
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = playlistSongs,
                            key = { _, song -> song.songId }
                        ) { index, song ->
                            if (showEditSongsMode) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val reordered = playlistSongs.toMutableList()
                                                    reordered.removeAt(index)
                                                    reordered.add(index - 1, song)
                                                    libraryViewModel.reorderPlaylist(playlistId, reordered.map { it.songId })
                                                }
                                            },
                                            modifier = Modifier.size(28.dp),
                                            enabled = index > 0
                                        ) {
                                            Icon(
                                                painterResource(
                                                    id = R.drawable.chevron_up),
                                                contentDescription = null,
                                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (index < playlistSongs.size - 1) {
                                                    val reordered = playlistSongs.toMutableList()
                                                    reordered.removeAt(index)
                                                    reordered.add(index + 1, song)
                                                    libraryViewModel.reorderPlaylist(playlistId, reordered.map { it.songId })
                                                }
                                            },
                                            modifier = Modifier.size(28.dp),
                                            enabled = index < playlistSongs.size - 1
                                        ) {
                                            Icon(
                                                painterResource(
                                                    id = R.drawable.chevron_down),
                                                contentDescription = null,
                                                tint = if (index < playlistSongs.size - 1) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        SongRow(
                                            song = song, onClick = {},
                                            showTrackNumbers = false,
                                            isFavourite = song.songId in favouriteIds
                                        )
                                    }

                                    IconButton(
                                        onClick = { libraryViewModel.removeSongFromPlaylist(song.songId, playlistId) },
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            painterResource(
                                                id = R.drawable.options_delete),
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                SongRow(
                                    song = song,
                                    onClick = { selectedSongId = song.songId },
                                    showTrackNumbers = false,
                                    isFavourite = song.songId in favouriteIds
                                )
                            }
                        }
                    }
                }
            }

            // Edit songs mode banner
            if (showEditSongsMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editing songs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        TextButton(onClick = { showEditSongsMode = false }) {
                            Text(
                                text = "Done",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playlist = playlistSongs,
                playbackViewModel = playbackViewModel,
                libraryViewModel = libraryViewModel,
                onDelete = { libraryViewModel.deleteSong(it) },
                onDismiss = { selectedSongId = null },
                showNowPlaying = showNowPlaying
            )
        }
    }
}