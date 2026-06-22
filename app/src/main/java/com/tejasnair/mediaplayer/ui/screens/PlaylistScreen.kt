package com.tejasnair.mediaplayer.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val playlists by libraryViewModel.allPlaylists.collectAsState()
    val playlist = playlists.find { it.playlistId == playlistId }
    val playlistSongs by libraryViewModel.getSongsInPlaylist(playlistId).collectAsState(initial = emptyList())
    val allSongs by libraryViewModel.allSongs.collectAsState()
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
    var showCoverPickerDialog by remember { mutableStateOf(false) }
    var showMosaicSongPicker by remember { mutableStateOf(false) }
    var showSongArtPicker by remember { mutableStateOf(false) }
    var editName by remember(playlist?.playlistName) { mutableStateOf(playlist?.playlistName ?: "") }

    val headerArtUri = playlist?.artUri
    val customMosaicIds = playlist?.mosaicSongIds
        ?.split(",")
        ?.filter { it.isNotBlank() }
        ?: emptyList()

    val mosaicArts = remember(playlist, playlistSongs) {
        if (playlist?.artUri != null) {
            emptyList() // single image mode, mosaic not used
        } else if (customMosaicIds.isNotEmpty()) {
            customMosaicIds.mapNotNull { id -> allSongs.find { it.songId == id }?.songArtUri }
        } else {
            playlistSongs.mapNotNull { it.songArtUri }.distinct().take(4)
        }
    }

    // Gallery picker for custom single image
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            scope.launch {
                val localPath = withContext(Dispatchers.IO) {
                    libraryViewModel.copyImageToInternalStorage(uri, playlistId)
                }
                if (localPath != null) {
                    libraryViewModel.updatePlaylistCover(
                        playlistId = playlistId,
                        artUri = localPath,
                        mosaicSongIds = null
                    )
                }
            }
        }
    }

    // Mosaic song selection state
    val selectedMosaicIds = remember { mutableStateListOf<String>() }

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

    // Cover picker dialog
    if (showCoverPickerDialog) {
        AlertDialog(
            onDismissRequest = { showCoverPickerDialog = false },
            title = { Text("Edit Cover") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Option 1: Auto mosaic
                    TextButton(
                        onClick = {
                            libraryViewModel.updatePlaylistCover(
                                playlistId = playlistId,
                                artUri = null,
                                mosaicSongIds = null
                            )
                            showCoverPickerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start) {
                            Icon(painterResource(R.drawable.playlist_cover_auto), null,
                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Auto Mosaic", color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("First 4 unique arts from playlist",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Custom mosaic
                    TextButton(
                        onClick = {
                            selectedMosaicIds.clear()
                            customMosaicIds.forEach { selectedMosaicIds.add(it) }
                            showCoverPickerDialog = false
                            showMosaicSongPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start) {
                            Icon(painterResource(R.drawable.playlist_cover_custom), null,
                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Custom Mosaic", color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Pick 2–4 songs from this playlist",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Upload photo from gallery
                    TextButton(
                        onClick = {
                            showCoverPickerDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start) {
                            Icon(painterResource(R.drawable.nav_upload), null,
                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Upload Photo", color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Choose from your device",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Use a song's art
                    TextButton(
                        onClick = {
                            showCoverPickerDialog = false
                            showSongArtPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start) {
                            Icon(painterResource(R.drawable.playlist_song_cover), null,
                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Use Song Art", color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Pick any song from your library",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCoverPickerDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Custom mosaic song picker
    if (showMosaicSongPicker) {
        AlertDialog(
            onDismissRequest = { showMosaicSongPicker = false },
            title = {
                Column {
                    Text("Select Songs for Mosaic")
                    Text(
                        "Choose 2–4 songs  •  ${selectedMosaicIds.size}/4 selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                LazyColumn {
                    items(playlistSongs) { song ->
                        val isSelected = song.songId in selectedMosaicIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedMosaicIds.remove(song.songId)
                                    } else if (selectedMosaicIds.size < 4) {
                                        selectedMosaicIds.add(song.songId)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it && selectedMosaicIds.size < 4) selectedMosaicIds.add(song.songId)
                                    else if (!it) selectedMosaicIds.remove(song.songId)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            AsyncImage(
                                model = song.songArtUri,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(song.title, style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(song.artists, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedMosaicIds.size >= 2,
                    onClick = {
                        libraryViewModel.updatePlaylistCover(
                            playlistId = playlistId,
                            artUri = null,
                            mosaicSongIds = selectedMosaicIds.joinToString(",")
                        )
                        showMosaicSongPicker = false
                    }
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { showMosaicSongPicker = false }) { Text("Cancel") }
            }
        )
    }

    // Song art picker
    if (showSongArtPicker) {
        AlertDialog(
            onDismissRequest = { showSongArtPicker = false },
            title = { Text("Pick a Song's Art") },
            text = {
                LazyColumn {
                    items(allSongs.filter { it.songArtUri != null }) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        val srcPath = song.songArtUri ?: return@launch
                                        val destPath = withContext(Dispatchers.IO) {
                                            try {
                                                val srcFile = java.io.File(srcPath)
                                                val dir = java.io.File(
                                                    context.filesDir, "playlist_art"
                                                ).apply { mkdirs() }
                                                val dest = java.io.File(dir, "cover_$playlistId.jpg")
                                                srcFile.copyTo(dest, overwrite = true)
                                                dest.absolutePath
                                            } catch (e: Exception) { null }
                                        }
                                        if (destPath != null) {
                                            libraryViewModel.updatePlaylistCover(
                                                playlistId = playlistId,
                                                artUri = destPath,
                                                mosaicSongIds = null
                                            )
                                        }
                                    }
                                    showSongArtPicker = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = song.songArtUri,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(song.artists, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSongArtPicker = false }) { Text("Cancel") }
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

                // Mosaic header card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        if (headerArtUri != null) {
                            // Single image mode
                            AsyncImage(
                                model = headerArtUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (mosaicArts.isNotEmpty()) {
                            // Mosaic mode
                            PlaylistMosaic(arts = mosaicArts)
                        } else {
                            // Empty placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.playlist_cover_custom),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Diagonal gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    }

                    // Info row overlaid at the bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(painterResource(R.drawable.nav_back_arrow), "Back",
                                    tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = playlist?.playlistName ?: "Playlist",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (playlistSongs.size == 1) "1 song" else "${playlistSongs.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Box {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { showOptionsMenu = true }) {
                                    Icon(painterResource(R.drawable.options), "Options",
                                        tint = Color.White, modifier = Modifier.size(18.dp))
                                }
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
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(playlist?.playlistName ?: "Playlist",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${playlistSongs.size} songs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(Modifier.height(4.dp))

                                StyledDropdownItem(R.drawable.options_edit, "Edit Details") {
                                    showOptionsMenu = false
                                    editName = playlist?.playlistName ?: ""
                                    showEditNameDialog = true
                                }
                                StyledDropdownItem(R.drawable.playlist_cover, "Edit Cover") {
                                    showOptionsMenu = false
                                    showCoverPickerDialog = true
                                }
                                StyledDropdownItem(R.drawable.options_deletesome, "Edit Songs") {
                                    showOptionsMenu = false
                                    showEditSongsMode = !showEditSongsMode
                                }
                                StyledDropdownItem(R.drawable.options_delete, "Delete Playlist",
                                    tint = MaterialTheme.colorScheme.error) {
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
                            Icon(painterResource(R.drawable.song_play), null, Modifier.size(18.dp))
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
                            Icon(painterResource(R.drawable.song_shuffle), null, Modifier.size(18.dp))
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
                    modifier = Modifier.fillMaxWidth().height(1.dp)
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
                                            Icon(painterResource(R.drawable.chevron_up), null,
                                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                modifier = Modifier.size(18.dp))
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
                                            Icon(painterResource(R.drawable.chevron_down), null,
                                                tint = if (index < playlistSongs.size - 1) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        SongRow(song = song, onClick = {},
                                            showTrackNumbers = false,
                                            isFavourite = song.songId in favouriteIds)
                                    }

                                    IconButton(
                                        onClick = { libraryViewModel.removeSongFromPlaylist(song.songId, playlistId) },
                                        modifier = Modifier.padding(end = 4.dp).size(36.dp)
                                    ) {
                                        Icon(painterResource(R.drawable.options_delete), "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp))
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
                        Text("Editing songs", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary)
                        TextButton(onClick = { showEditSongsMode = false }) {
                            Text("Done", color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold)
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

// Mosaic composable
@Composable
fun PlaylistMosaic(arts: List<String>, modifier: Modifier = Modifier) {
    when (arts.size) {
        1 -> AsyncImage(model = arts[0], contentDescription = null,
            modifier = modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        2 -> Row(modifier = modifier.fillMaxSize()) {
            AsyncImage(model = arts[0], contentDescription = null,
                modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
            AsyncImage(model = arts[1], contentDescription = null,
                modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
        }
        3 -> Row(modifier = modifier.fillMaxSize()) {
            AsyncImage(model = arts[0], contentDescription = null,
                modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                AsyncImage(model = arts[1], contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxWidth(), contentScale = ContentScale.Crop)
                AsyncImage(model = arts[2], contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxWidth(), contentScale = ContentScale.Crop)
            }
        }
        else -> Column(modifier = modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AsyncImage(model = arts[0], contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
                AsyncImage(model = arts[1], contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AsyncImage(model = arts[2], contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
                AsyncImage(model = arts[3], contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
            }
        }
    }
}