package com.tejasnair.mediaplayer.ui.screens

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset

// 2. Compose Runtime
import androidx.compose.runtime.*

// 3. Material3
import androidx.compose.material3.*

// 4. External Libraries
import coil.compose.AsyncImage

// 5. Kotlin Standard Library
import kotlin.collections.emptyList

// 6. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.DiscHeader
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun AlbumScreen(
    albumName: String,
    albumArtist: String,
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val albumSongs by libraryViewModel.getSongsByAlbum(albumName, albumArtist).collectAsState(initial = emptyList())
    val groupedSongs = albumSongs.groupBy { it.discNumber }

    val favouriteSongs by libraryViewModel.favouriteSongs.collectAsState(initial = emptyList())

    val firstSong = albumSongs.firstOrNull()
    var selectedSongId by remember { mutableStateOf<String?>(null) }
    val selectedSong = albumSongs.find { it.songId == selectedSongId }
    val albumYear = firstSong?.year

    // Dropdown state
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showDeleteAlbumDialog by remember { mutableStateOf(false) }
    var showEditAlbumDialog by remember { mutableStateOf(false) }
    var showConfirmEditDeleteDialog by remember { mutableStateOf(false) }

    val songsSelectedForDeletion = remember { mutableStateSetOf<String>() }

    // --- Dialogs ---
    if (showDeleteAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAlbumDialog = false },
            title = { Text("Delete Album") },
            text = { Text("Are you sure you want to delete all songs in $albumName? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    albumSongs.forEach { libraryViewModel.deleteSong(it) }
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
                        .forEach { libraryViewModel.deleteSong(it) }
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Full bleed header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    // Full bleed art
                    AsyncImage(
                        model = firstSong?.backCoverUri ?: firstSong?.songArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )

                    // Gradient fade to background at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    // Text pinned to bottom of the box
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = albumArtist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        var tertiaryText = albumSongs.size.toString() + " songs"
                        if (albumYear != null) tertiaryText = "$tertiaryText • $albumYear"

                        if (albumSongs.isNotEmpty()) {
                            Text(
                                text = tertiaryText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Play + Shuffle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (albumSongs.isNotEmpty())
                                playbackViewModel.playSong(albumSongs.first(), albumSongs)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.song_play),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Play")
                    }
                    OutlinedButton(
                        onClick = {
                            if (albumSongs.isNotEmpty()) {
                                val shuffled = albumSongs.shuffled()
                                playbackViewModel.playSong(shuffled.first(), shuffled)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.song_shuffle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                LazyColumn {
                    groupedSongs.forEach { (discNumber, discSongs) ->
                        item(key = "disc_header_$discNumber") {
                            if (groupedSongs.size > 1) DiscHeader(discNumber = discNumber)
                        }
                        items(items = discSongs, key = { it.filePath }) { song ->
                            SongRow(
                                song = song,
                                onClick = { selectedSongId = song.songId },
                                showTrackNumbers = true,
                                isFavourite = song in favouriteSongs
                            )
                        }
                    }
                }
            }

            // Options icon button + dropdown anchored to TopEnd
            Box(modifier = Modifier.align(TopEnd)) {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier
                        .safeDrawingPadding()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.options),
                        contentDescription = "Album Options",
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
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = albumArtist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Spacer(Modifier.height(4.dp))

                    // Playback options
                    StyledDropdownItem(
                        icon = R.drawable.song_play,
                        label = "Play Album",
                        onClick = {
                            showOptionsMenu = false
                            playbackViewModel.playSong(albumSongs.first(), albumSongs)
                        }
                    )
                    StyledDropdownItem(
                        icon = R.drawable.song_shuffle,
                        label = "Shuffle Album",
                        onClick = {
                            showOptionsMenu = false
                            val shuffled = albumSongs.shuffled()
                            playbackViewModel.playSong(shuffled.first(), shuffled)
                        }
                    )
                    StyledDropdownItem(
                        icon = R.drawable.song_options_addtoqueue,
                        label = "Add to Queue",
                        onClick = {
                            showOptionsMenu = false
                            playbackViewModel.addAlbumToQueue(albumSongs)
                        }
                    )
                    StyledDropdownItem(
                        icon = R.drawable.song_options_playlist,
                        label = "Save to Playlist",
                        onClick = {
                            showOptionsMenu = false
                        }
                    )

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))

                    // Destructive options
                    StyledDropdownItem(
                        icon = R.drawable.options_edit,
                        label = "Edit Album",
                        onClick = {
                            showOptionsMenu = false
                            songsSelectedForDeletion.clear()
                            showEditAlbumDialog = true
                        }
                    )
                    StyledDropdownItem(
                        icon = R.drawable.options_delete,
                        label = "Delete Album",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = {
                            showOptionsMenu = false
                            showDeleteAlbumDialog = true
                        }
                    )

                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playlist = albumSongs,
                playbackViewModel = playbackViewModel,
                libraryViewModel = libraryViewModel,
                onDelete = { libraryViewModel.deleteSong(it) },
                onDismiss = { selectedSongId = null }
            )
        }
    }
}

@Composable
fun StyledDropdownItem(
    icon: Int,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = tint
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        tint.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    )
}