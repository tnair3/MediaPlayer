package com.tejasnair.mediaplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.VinylSide
import com.tejasnair.mediaplayer.ui.components.DeleteConfirmationDialog
import com.tejasnair.mediaplayer.ui.components.StyledButton
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.VinylDetailsView
import com.tejasnair.mediaplayer.ui.components.VinylSongsView
import com.tejasnair.mediaplayer.ui.components.drawScrollbar
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VinylDetailsScreen(
    vinylId: String,
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val record by libraryViewModel.getFullVinylRecordById(vinylId).collectAsState(initial = null)
    val vinyl by remember(record) { derivedStateOf { record?.vinyl } }
    val sides by remember(record) { derivedStateOf { record?.sides?.map { it.side }?.sortedBy { it.position } ?: emptyList() } }
    val sideSongsMap by remember(sides) {
        if (sides.isEmpty()) flowOf(emptyMap())
        else combine(
            sides.map { side ->
                libraryViewModel.getSongsInVinylSide(side.vinylSideId).map { songs -> side.vinylSideId to songs }
            }
        ) { pairs -> pairs.toMap() }
    }.collectAsState(initial = emptyMap())

    val orderedSongs by remember(sides, sideSongsMap) { derivedStateOf { sides.flatMap { sideSongsMap[it.vinylSideId] ?: emptyList() } } }
    val totalSongCount by remember(sideSongsMap) { derivedStateOf { sideSongsMap.values.sumOf { it.size } } }
    val favouriteSongs by libraryViewModel.favouriteSongs.collectAsState(initial = emptyList())
    val favouriteIds by remember(favouriteSongs) { derivedStateOf { favouriteSongs.map { it.songId }.toHashSet() } }
    var showSongsView by remember { mutableStateOf(value = false) }
    var showDetailsOptionsMenu by remember { mutableStateOf(value = false) }
    var showSongsOptionsMenu by remember { mutableStateOf(value = false) }
    var showEditDetailsDialog by remember { mutableStateOf(value = false) }
    var showDeleteVinylDialog by remember { mutableStateOf(value = false) }
    var showManageSidesDialog by remember { mutableStateOf(value = false) }
    var showAddSongsDialog by remember { mutableStateOf(value = false) }
    var showSelectSidePlayDialog by remember { mutableStateOf(value = false) }
    var isEditSongsMode by remember { mutableStateOf(value = false) }
    var selectedSongId by remember { mutableStateOf<String?>(value = null) }
    val selectedSong by remember { derivedStateOf { orderedSongs.find { it.songId == selectedSongId } } }
    var hasLoadedInitially by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = record) {
        if (record != null) hasLoadedInitially = true
        if (record == null && hasLoadedInitially) navController.navigateUp()
    }

    val currentVinyl = vinyl
    if (currentVinyl == null) {
        ThemedScreen {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    if (showDeleteVinylDialog) {
        DeleteConfirmationDialog(
            primaryText = "Delete Vinyl",
            secondaryText = "Are you sure you want to delete \"${currentVinyl.title}\"? This cannot be undone.",
            onConfirm = {
                libraryViewModel.deleteVinyl(vinylId)
                showDeleteVinylDialog = false
            },
            onDismiss = { showDeleteVinylDialog = false }
        )
    }

    if (showEditDetailsDialog) {
        var editTitle by remember { mutableStateOf(value = currentVinyl.title) }
        var editArtist by remember { mutableStateOf(value = currentVinyl.artist ?: "") }
        var editCoverUri by remember { mutableStateOf<Uri?>(value = null) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? -> if (uri != null) editCoverUri = uri }

        AlertDialog(
            onDismissRequest = { showEditDetailsDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        libraryViewModel.updateVinylDetails(
                            vinylId = vinylId,
                            title = editTitle.trim(),
                            artist = editArtist.trim().ifBlank { null }
                        )
                        editCoverUri?.let { uri ->
                            val savedPath = libraryViewModel.copyVinylImageToInternalStorage(context, uri, vinylId)
                            libraryViewModel.updateVinylCoverArt(vinylId, savedPath)
                        }
                        showEditDetailsDialog = false
                    },
                    enabled = editTitle.isNotBlank()
                ) { Text(text = "Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDetailsDialog = false }) { Text(text = "Cancel") }
            },
            icon = { Icon(painter = painterResource(id = R.drawable.options_edit), contentDescription = "Editing") },
            title = { Text(text = "Edit Vinyl Details") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 100.dp)
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val previewUri = editCoverUri
                        val existingPath = currentVinyl.coverArtUri
                        when {
                            previewUri != null -> Image(
                                painter = rememberAsyncImagePainter(model = previewUri),
                                contentDescription = "Selected artwork",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            existingPath != null && File(existingPath).exists() -> Image(
                                painter = rememberAsyncImagePainter(model = File(existingPath)),
                                contentDescription = "Current artwork",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            else -> Icon(
                                painter = painterResource(id = R.drawable.options_edit),
                                contentDescription = "Change Cover",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Artist (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                }
            }
        )
    }

    if (showManageSidesDialog) {
        var sideToDelete by remember { mutableStateOf<VinylSide?>(value = null) }
        val renameDrafts = remember(sides) {
            mutableStateMapOf<String, String>().apply { sides.forEach { put(it.vinylSideId, it.sideName) } }
        }
        val lazyListState = rememberLazyListState()

        AlertDialog(
            onDismissRequest = { showManageSidesDialog = false },
            modifier = Modifier
                .padding(vertical = 28.dp)
                .heightIn(max = 650.dp),
            confirmButton = {
                TextButton(onClick = {
                    sides.forEach { side ->
                        val newName = renameDrafts[side.vinylSideId]?.trim().orEmpty()
                        if (newName.isNotBlank() && newName != side.sideName) {
                            libraryViewModel.updateVinylSideName(side.vinylSideId, newName)
                        }
                    }
                    showManageSidesDialog = false
                }) { Text(text = "Done") }
            },
            title = { Text(text = "Manage Sides") },
            text = {
                Column {
                    val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .drawScrollbar(lazyListState, scrollbarColor)
                            .padding(end = 8.dp)
                    ) {
                        items(sides, key = { it.vinylSideId }) { side ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = renameDrafts[side.vinylSideId] ?: side.sideName,
                                    onValueChange = { renameDrafts[side.vinylSideId] = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    label = { Text(text = "Side name") },
                                    shape = RoundedCornerShape(size = 12.dp)
                                )
                                IconButton(
                                    onClick = { sideToDelete = side },
                                    enabled = sides.size > 1
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.options_delete),
                                        contentDescription = "Delete Side",
                                        tint = if (sides.size > 1) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                sides.forEach { side ->
                                    val newName = renameDrafts[side.vinylSideId]?.trim().orEmpty()
                                    if (newName.isNotBlank() && newName != side.sideName) {
                                        libraryViewModel.updateVinylSideName(side.vinylSideId, newName)
                                    }
                                }

                                val nextPosition = (sides.maxOfOrNull { it.position } ?: 0) + 1
                                val nextLetter = ('A' + sides.size)
                                libraryViewModel.createVinylSide(
                                    VinylSide(vinylId = vinylId, position = nextPosition, sideName = "Side $nextLetter")
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Add Side",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Add Side", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )

        sideToDelete?.let { side ->
            DeleteConfirmationDialog(
                primaryText = "Delete ${side.sideName}",
                secondaryText = "Are you sure you want to delete \"${side.sideName}\"? Its songs will be removed from the vinyl. This cannot be undone.",
                onConfirm = {
                    libraryViewModel.deleteVinylSide(side.vinylSideId)
                    sideToDelete = null
                },
                onDismiss = { sideToDelete = null }
            )
        }
    }

    if (showAddSongsDialog) {
        val allSongs by libraryViewModel.allSongs.collectAsState(initial = emptyList())
        val existingIds = remember(orderedSongs) { orderedSongs.map { it.songId }.toHashSet() }
        val availableSongs = remember(allSongs, existingIds) { allSongs.filter { it.songId !in existingIds } }
        var selectedSideId by remember { mutableStateOf(sides.firstOrNull()?.vinylSideId) }
        val songsSelectedForAdd = remember { mutableStateSetOf<String>() }
        val lazyListState = rememberLazyListState()
        var searchQuery by remember { mutableStateOf("") }
        val filteredSongs = remember(availableSongs, searchQuery) {
            if (searchQuery.isBlank()) availableSongs
            else availableSongs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artists.contains(searchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showAddSongsDialog = false },
            modifier = Modifier
                .padding(vertical = 28.dp)
                .heightIn(max = 650.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetSideId = selectedSideId
                        if (targetSideId != null && songsSelectedForAdd.isNotEmpty()) {
                            val startPosition = sideSongsMap[targetSideId]?.size ?: 0
                            libraryViewModel.addSongsToVinylSide(
                                songIds = songsSelectedForAdd.toList(),
                                vinylSideId = targetSideId,
                                startPosition = startPosition
                            )
                        }
                        showAddSongsDialog = false
                    },
                    enabled = selectedSideId != null && songsSelectedForAdd.isNotEmpty()
                ) { Text(text = "Add (${songsSelectedForAdd.size})") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSongsDialog = false }) { Text(text = "Cancel") }
            },
            icon = { Icon(painter = painterResource(id = R.drawable.add), contentDescription = "Adding Songs") },
            title = { Text(text = "Add Songs to Vinyl") },
            text = {
                Column {
                    Text(
                        text = "Add to:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        items(sides, key = { it.vinylSideId }) { side ->
                            val isSelected = side.vinylSideId == selectedSideId
                            Text(
                                text = side.sideName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedSideId = side.vinylSideId }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (availableSongs.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text(text = "Search songs") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

                    if (availableSongs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Every song in your library is already on this vinyl.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (filteredSongs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No songs match your search.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .drawScrollbar(lazyListState, scrollbarColor)
                                .padding(end = 8.dp)
                        ) {
                            items(filteredSongs, key = { it.songId }) { song ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = song.songId in songsSelectedForAdd,
                                        onCheckedChange = { checked ->
                                            if (checked) songsSelectedForAdd.add(song.songId)
                                            else songsSelectedForAdd.remove(song.songId)
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artists,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    if (showSelectSidePlayDialog) {
        val lazyGridState = rememberLazyGridState()

        AlertDialog(
            onDismissRequest = { showSelectSidePlayDialog = false },
            modifier = Modifier
                .padding(vertical = 28.dp)
                .heightIn(max = 650.dp),
            confirmButton = {
                TextButton(onClick = { showSelectSidePlayDialog = false }) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = "Select Side to Play") },
            text = {
                Column {
                    val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = lazyGridState,
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .drawScrollbar(lazyGridState, scrollbarColor)
                            .padding(end = 8.dp)
                    ) {
                        items(sides, key = { it.vinylSideId }) { side ->
                            val songsForSide = sideSongsMap[side.vinylSideId] ?: emptyList()

                            StyledButton(
                                label = if (songsForSide.size == 1) "1 song" else "${songsForSide.size} songs",
                                onClick = {
                                    if (songsForSide.isNotEmpty()) {
                                        playbackViewModel.playSong(
                                            selectedSong = songsForSide.first(),
                                            playlist = songsForSide
                                        )
                                    }
                                    showSelectSidePlayDialog = false
                                },
                                enabled = songsForSide.isNotEmpty(),
                                cornerRadius = 12.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = side.sideName)
                            }
                        }
                    }
                }
            }
        )
    }

    ThemedScreen {
        BackHandler(enabled = showSongsView) {
            if (!isEditSongsMode) showSongsView = false
            isEditSongsMode = false
        }

        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Crossfade(targetState = showSongsView, label = "vinylDetailsCrossfade") { isSongsView ->
                if (isSongsView) {
                    VinylSongsView(
                        vinylTitle = currentVinyl.title,
                        sides = sides,
                        sideSongsMap = sideSongsMap,
                        favouriteIds = favouriteIds,
                        isEditMode = isEditSongsMode,
                        showOptionsMenu = showSongsOptionsMenu,
                        onOptionsMenuChange = { showSongsOptionsMenu = it },
                        onBack = { showSongsView = false; isEditSongsMode = false },
                        onSongClick = { song -> selectedSongId = song.songId },
                        onToggleEditMode = { isEditSongsMode = !isEditSongsMode },
                        onRemoveSong = { sideId, songId -> libraryViewModel.removeSongFromVinylSide(sideId, songId) },
                        onReorderSong = { sideId, orderedIds -> libraryViewModel.reorderVinylSide(sideId, orderedIds) },
                        onAddSongs = { showAddSongsDialog = true },
                        onManageSides = { showManageSidesDialog = true }
                    )
                } else {
                    VinylDetailsView(
                        title = currentVinyl.title,
                        artist = currentVinyl.artist,
                        coverArtUri = currentVinyl.coverArtUri,
                        sideCount = sides.size,
                        songCount = totalSongCount,
                        showOptionsMenu = showDetailsOptionsMenu,
                        onOptionsMenuChange = { showDetailsOptionsMenu = it },
                        onBack = { navController.navigateUp() },
                        onPlay = { if (sides.isNotEmpty()) showSelectSidePlayDialog = true },
                        onViewSongs = { showSongsView = true },
                        onEditDetails = { showEditDetailsDialog = true },
                        onDeleteVinyl = { showDeleteVinylDialog = true }
                    )
                }
            }
        }

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playlist = orderedSongs,
                playbackViewModel = playbackViewModel,
                libraryViewModel = libraryViewModel,
                onDelete = { libraryViewModel.deleteSong(it) },
                onDismiss = { selectedSongId = null },
                showNowPlaying = remember { mutableStateOf(false) }
            )
        }
    }
}