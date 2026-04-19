package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.DiscHeader
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.StyledDropdownItem
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

import android.util.Log

@Composable
fun AlbumScreen(
    albumName: String,
    albumArtist: String,
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController,
    showNowPlaying: MutableState<Boolean>
) {
    val albumSongs by libraryViewModel.getSongsByAlbum(albumName, albumArtist).collectAsState(initial = emptyList())
    val favouriteSongs by libraryViewModel.favouriteSongs.collectAsState(initial = emptyList())

    val favouriteIds by remember(key1 = favouriteSongs) {
        derivedStateOf { favouriteSongs.map { it.songId }.toHashSet() }
    }

    val groupedSongs by remember(key1 = albumSongs) {
        derivedStateOf { albumSongs.groupBy { it.discNumber } }
    }

    val firstSong by remember(key1 = albumSongs) {
        derivedStateOf { albumSongs.firstOrNull() }
    }

    val albumYear by remember(key1 = firstSong) {
        derivedStateOf { firstSong?.year }
    }

    var selectedSongId by remember { mutableStateOf<String?>(value = null) }
    val selectedSong by remember { derivedStateOf { albumSongs.find { it.songId == selectedSongId } } }

    var showOptionsMenu by remember { mutableStateOf(value = false) }
    var showDeleteAlbumDialog by remember { mutableStateOf(value = false) }
    var showDeleteSongsDialog by remember { mutableStateOf(value = false) }
    var showConfirmEditDeleteDialog by remember { mutableStateOf(value = false) }
    var showEditDetailsDialog by remember { mutableStateOf(value = false) }

    var editAlbumName by remember { mutableStateOf(value = albumName) }
    var editAlbumArtist by remember { mutableStateOf(value = albumArtist) }
    var editAlbumYear by remember { mutableStateOf(value = albumYear ?: "") }

    val songsSelectedForDeletion = remember { mutableStateSetOf<String>() }

    var hasLoadedInitially by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = albumSongs) {
        if (albumSongs.isNotEmpty()) hasLoadedInitially = true
        if (albumSongs.isEmpty() && hasLoadedInitially) navController.navigateUp()
    }

    // Dialogs

    if (showDeleteAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAlbumDialog = false },
            title = { Text("Delete Album") },
            text = { Text("Are you sure you want to delete all songs in $albumName? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    albumSongs.forEach { libraryViewModel.deleteSong(it) }
                    showDeleteAlbumDialog = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlbumDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteSongsDialog) {
        AlertDialog(
            modifier = Modifier.padding(vertical = 28.dp),
            onDismissRequest = {
                showDeleteSongsDialog = false
                songsSelectedForDeletion.clear()
            },
            title = { Text("Select Songs to Delete") },
            text = {
                Column {
                    val allSelected = songsSelectedForDeletion.size == albumSongs.size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                if (checked) songsSelectedForDeletion.addAll(elements = albumSongs.map { it.filePath })
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
                                        else songsSelectedForDeletion.remove(element = song.filePath)
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
                        color =
                            if (songsSelectedForDeletion.isNotEmpty()) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSongsDialog = false; songsSelectedForDeletion.clear() }) { Text("Cancel") }
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
                    albumSongs.filter { it.filePath in songsSelectedForDeletion }
                        .forEach { libraryViewModel.deleteSong(it) }
                    songsSelectedForDeletion.clear()
                    showConfirmEditDeleteDialog = false
                    showDeleteSongsDialog = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmEditDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showEditDetailsDialog = false },
            title = { Text("Edit Album Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editAlbumName,
                        onValueChange = { editAlbumName = it },
                        label = { Text("Album Name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(size = 12.dp))
                    OutlinedTextField(
                        value = editAlbumArtist,
                        onValueChange = { editAlbumArtist = it },
                        label = { Text("Album Artist") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(size = 12.dp))
                    OutlinedTextField(
                        value = editAlbumYear,
                        onValueChange = { editAlbumYear = it },
                        label = { Text("Year") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(size = 12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                TextButton(
                    enabled = editAlbumName.isNotBlank() && editAlbumArtist.isNotBlank(),
                    onClick = {
                        libraryViewModel.updateAlbumDetails(
                            oldAlbum = albumName, oldArtist = albumArtist,
                            newAlbum = editAlbumName.trim(), newArtist = editAlbumArtist.trim(),
                            newYear = editAlbumYear.trim().takeIf { it.isNotBlank() }
                        )
                        showEditDetailsDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    editAlbumName = albumName; editAlbumArtist = albumArtist
                    editAlbumYear = albumYear ?: ""; showEditDetailsDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    // Main UI

    ThemedScreen {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Full bleed header
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)) {
                    AsyncImage(
                        model = firstSong?.backCoverUri ?: firstSong?.songArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background)
                                )
                        )
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .wrapContentSize()
                                .background(Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(size = 16.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = albumName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = albumArtist,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            val tertiaryText = buildString {
                                append("${albumSongs.size} songs")
                                if (albumYear != null) append(" • $albumYear")
                            }
                            if (albumSongs.isNotEmpty()) {
                                Text(
                                    text = tertiaryText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
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
                            if (albumSongs.isNotEmpty()) playbackViewModel.playSong(
                                selectedSong = albumSongs.first(),
                                playlist = albumSongs
                            )
                            showNowPlaying.value = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.song_play),
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
                                playbackViewModel.playSong(
                                    selectedSong = shuffled.first(),
                                    playlist = shuffled
                                )
                            }
                            showNowPlaying.value = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.song_shuffle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp))
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
                                isFavourite = song.songId in favouriteIds
                            )
                        }
                    }
                }
            }

            // Back / Options buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.safeDrawingPadding()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.nav_back_arrow),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Box(modifier = Modifier.safeDrawingPadding()) {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.options),
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

                        StyledDropdownItem(icon = R.drawable.song_play, label = "Play Album") {
                            showOptionsMenu = false
                            playbackViewModel.playSong(selectedSong = albumSongs.first(), playlist = albumSongs)
                        }
                        StyledDropdownItem(icon = R.drawable.song_shuffle, label = "Shuffle Album") {
                            showOptionsMenu = false
                            val shuffled = albumSongs.shuffled()
                            playbackViewModel.playSong(selectedSong = shuffled.first(), playlist = shuffled)
                        }
                        StyledDropdownItem(icon = R.drawable.song_options_addtoqueue, label = "Add to Queue") {
                            showOptionsMenu = false
                            playbackViewModel.addAlbumToQueue(albumSongs)
                        }
                        StyledDropdownItem(icon = R.drawable.song_options_playlist, label = "Save to Playlist") {
                            showOptionsMenu = false
                        }

                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(4.dp))

                        StyledDropdownItem(icon = R.drawable.options_edit, label = "Edit Details") {
                            showOptionsMenu = false
                            editAlbumName = albumName; editAlbumArtist = albumArtist
                            editAlbumYear = albumYear ?: ""; showEditDetailsDialog = true
                        }
                        StyledDropdownItem(icon = R.drawable.options_deletesome, label = "Delete Songs") {
                            showOptionsMenu = false
                            songsSelectedForDeletion.clear(); showDeleteSongsDialog = true
                        }
                        StyledDropdownItem(icon = R.drawable.options_delete, label = "Delete Album", tint = MaterialTheme.colorScheme.error) {
                            showOptionsMenu = false; showDeleteAlbumDialog = true
                        }

                        Spacer(Modifier.height(4.dp))
                    }
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
                onDismiss = { selectedSongId = null },
                showNowPlaying = showNowPlaying
            )
        }
    }
}