package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.data.model.Playlist
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.DeleteConfirmationDialog
import com.tejasnair.mediaplayer.ui.components.DiscHeader
import com.tejasnair.mediaplayer.ui.components.drawScrollbar
import com.tejasnair.mediaplayer.ui.components.SongRow
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.StyledDropdownItem
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import java.util.UUID

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
    val groupedSongs by remember(key1 = albumSongs) { derivedStateOf { albumSongs.groupBy { it.discNumber } } }
    val firstSong by remember(key1 = albumSongs) { derivedStateOf { albumSongs.firstOrNull() } }
    var selectedSongId by remember { mutableStateOf<String?>(value = null) }
    val selectedSong by remember { derivedStateOf { albumSongs.find { it.songId == selectedSongId } } }
    val albumYear by remember(key1 = firstSong) { derivedStateOf { firstSong?.year } }
    val favouriteSongs by libraryViewModel.favouriteSongs.collectAsState(initial = emptyList())
    val favouriteIds by remember(key1 = favouriteSongs) { derivedStateOf { favouriteSongs.map { it.songId }.toHashSet() } }
    var showOptionsMenu by remember { mutableStateOf(value = false) }
    var showDeleteAlbumDialog by remember { mutableStateOf(value = false) }
    var showDeleteSongsDialog by remember { mutableStateOf(value = false) }
    var showConfirmEditDeleteDialog by remember { mutableStateOf(value = false) }
    var showEditDetailsDialog by remember { mutableStateOf(value = false) }
    var editAlbumName by remember { mutableStateOf(value = albumName) }
    var editAlbumArtist by remember { mutableStateOf(value = albumArtist) }
    var editAlbumYear by remember { mutableStateOf(value = albumYear ?: "") }
    var showAlbumPlaylistPicker by remember { mutableStateOf(value = false) }
    val songsSelectedForDeletion = remember { mutableStateSetOf<String>() }
    var hasLoadedInitially by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = albumSongs) {
        if (albumSongs.isNotEmpty()) hasLoadedInitially = true
        if (albumSongs.isEmpty() && hasLoadedInitially) navController.navigateUp()
    }

    if (showDeleteAlbumDialog) {
        DeleteConfirmationDialog(
            primaryText = "Delete Album",
            secondaryText = "Are you sure you want to delete all songs in \"${albumName}\"? This cannot be undone.",
            onConfirm = {
                albumSongs.forEach { libraryViewModel.deleteSong(it) }
                showDeleteAlbumDialog = false
            },
            onDismiss = { showDeleteAlbumDialog = false }
        )
    }

    if (showDeleteSongsDialog) {
        val lazyListState = rememberLazyListState()

        AlertDialog(
            onDismissRequest = {
                showDeleteSongsDialog = false
                songsSelectedForDeletion.clear()
            },
            confirmButton = {
                TextButton(
                    onClick = { showConfirmEditDeleteDialog = true },
                    enabled = songsSelectedForDeletion.isNotEmpty()
                ) {
                    Text(
                        text = "Delete (${songsSelectedForDeletion.size})",
                        color = if (songsSelectedForDeletion.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            },
            modifier = Modifier
                .padding(vertical = 28.dp)
                .heightIn(max = 650.dp),
            dismissButton = { TextButton(onClick = { showDeleteSongsDialog = false; songsSelectedForDeletion.clear() }) { Text("Cancel") } },
            icon = { Icon(painter = painterResource(id = R.drawable.options_deletesome), contentDescription = "Erasing") },
            title = { Text(text = "Select Songs to Delete") },
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
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

                    val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .drawScrollbar(lazyListState, scrollbarColor)
                            .padding(end = 8.dp)
                    ) {
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
            }
        )
    }

    if (showConfirmEditDeleteDialog) {
        DeleteConfirmationDialog(
            primaryText = "Delete Songs",
            secondaryText =
                if (songsSelectedForDeletion.size == 1) "Are you sure you want to delete 1 song? This cannot be undone."
                else "Are you sure you want to delete ${songsSelectedForDeletion.size} songs? This cannot be undone.",
            onConfirm = {
                albumSongs.filter { it.filePath in songsSelectedForDeletion }
                    .forEach { libraryViewModel.deleteSong(it) }
                songsSelectedForDeletion.clear()
                showConfirmEditDeleteDialog = false
                showDeleteSongsDialog = false
            },
            onDismiss = { showConfirmEditDeleteDialog = false }
        )
    }

    if (showEditDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showEditDetailsDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        libraryViewModel.updateAlbumDetails(
                            oldAlbum = albumName, oldArtist = albumArtist,
                            newAlbum = editAlbumName.trim(), newArtist = editAlbumArtist.trim(),
                            newYear = editAlbumYear.trim().takeIf { it.isNotBlank() }
                        )
                        showEditDetailsDialog = false
                    },
                    enabled = editAlbumName.isNotBlank() && editAlbumArtist.isNotBlank()
                ) { Text(text = "Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    editAlbumName = albumName; editAlbumArtist = albumArtist
                    editAlbumYear = albumYear ?: ""; showEditDetailsDialog = false
                }) { Text(text = "Cancel") }
            },
            icon = { Icon(painter = painterResource(id = R.drawable.options_edit), contentDescription = "Editing") },
            title = { Text(text = "Edit Album Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editAlbumName,
                        onValueChange = { editAlbumName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Album Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    OutlinedTextField(
                        value = editAlbumArtist,
                        onValueChange = { editAlbumArtist = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Album Artist") },
                        singleLine = true,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    OutlinedTextField(
                        value = editAlbumYear,
                        onValueChange = { editAlbumYear = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Year") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                }
            }
        )
    }

    if (showAlbumPlaylistPicker) {
        val playlists by libraryViewModel.allPlaylists.collectAsState()
        var showNewPlaylistFromAlbum by remember { mutableStateOf(value = false) }
        var newNameFromAlbum by remember { mutableStateOf(value = "") }

        if (showNewPlaylistFromAlbum) {
            AlertDialog(
                onDismissRequest = { showNewPlaylistFromAlbum = false },
                confirmButton = {
                    TextButton(
                        enabled = newNameFromAlbum.isNotBlank(),
                        onClick = {
                            val newId = UUID.randomUUID().toString()
                            val newPlaylist = Playlist(playlistId = newId, playlistName = newNameFromAlbum.trim())
                            libraryViewModel.createPlaylistWithId(newPlaylist)
                            libraryViewModel.addSongsToPlaylist(songIds = albumSongs.map { it.songId }, playlistId = newId, startPosition = 0)
                            showNewPlaylistFromAlbum = false
                            showAlbumPlaylistPicker = false
                            newNameFromAlbum = ""
                        }
                    ) { Text(text = "Create & Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showNewPlaylistFromAlbum = false }) { Text(text = "Cancel") }
                },
                title = { Text(text = "New Playlist") },
                text = {
                    OutlinedTextField(
                        value = newNameFromAlbum,
                        onValueChange = { newNameFromAlbum = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Playlist name") },
                        singleLine = true,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showAlbumPlaylistPicker = false },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { showAlbumPlaylistPicker = false }) { Text(text = "Cancel") } },
                title = { Text(text = "Add Album to Playlist") },
                text = {
                    LazyColumn {
                        item {
                            TextButton(
                                onClick = { showNewPlaylistFromAlbum = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.add),
                                        contentDescription = "New Playlist",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "New Playlist",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                        items(playlists) { pl ->
                            val count by libraryViewModel.getPlaylistSongCount(playlistId = pl.playlistId).collectAsState(initial = 0)
                            TextButton(
                                onClick = {
                                    libraryViewModel.addSongsToPlaylist(songIds = albumSongs.map { it.songId }, playlistId = pl.playlistId, startPosition = count)
                                    showAlbumPlaylistPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.song_options_playlist),
                                        contentDescription = "Playlist",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = pl.playlistName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "$count songs",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(insets = WindowInsets.safeDrawing.only(sides = WindowInsetsSides.Bottom))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)) {
                    AsyncImage(
                        model = firstSong?.backCoverUri ?: firstSong?.songArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.Crop
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
                                .background(color = Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(size = 16.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = albumName,
                                fontWeight = FontWeight.Bold, color = Color.White,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                text = albumArtist,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val tertiaryText = buildString {
                                append("${albumSongs.size} songs")
                                if (albumYear != null) append(" • $albumYear")
                            }
                            if (albumSongs.isNotEmpty()) {
                                Text(
                                    text = tertiaryText,
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

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
                        shape = RoundedCornerShape(size = 14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.song_play),
                            contentDescription = "Play",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Play")
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
                        shape = RoundedCornerShape(size = 14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.song_shuffle),
                            contentDescription = "Shuffle",
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Shuffle")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                val isPlayerActive = playbackViewModel.currentSongId != null
                val totalBottomPadding = if (isPlayerActive) 64.dp else 0.dp

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = totalBottomPadding,
                        top = 0.dp,
                        start = 0.dp,
                        end = 0.dp
                    )
                ) {
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
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                        offset = DpOffset(x = (-10).dp, y = (-10).dp),
                        shape = RoundedCornerShape(size = 20.dp),
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
                        StyledDropdownItem(
                            icon = R.drawable.song_options_playlist,
                            label = "Save to Playlist",
                            onClick = {
                                showOptionsMenu = false
                                showAlbumPlaylistPicker = true
                            }
                        )

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