package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.VinylSide
import kotlin.collections.forEach

@Composable
fun VinylDetailsView(
    title: String,
    artist: String?,
    coverArtUri: String?,
    sideCount: Int,
    songCount: Int,
    showOptionsMenu: Boolean,
    onOptionsMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onViewSongs: () -> Unit,
    onEditDetails: () -> Unit,
    onDeleteVinyl: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.nav_back_arrow),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { onOptionsMenuChange(true) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.options),
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { onOptionsMenuChange(false) },
                    offset = DpOffset(x = (-10).dp, y = (-10).dp),
                    shape = RoundedCornerShape(size = 20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    modifier = Modifier.width(220.dp)
                ) {
                    StyledDropdownItem(icon = R.drawable.song_play, label = "Play") { onOptionsMenuChange(false); onPlay() }
                    StyledDropdownItem(icon = R.drawable.song_list, label = "View Songs") { onOptionsMenuChange(false); onViewSongs() }
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    StyledDropdownItem(icon = R.drawable.options_edit, label = "Edit Details") { onOptionsMenuChange(false); onEditDetails() }
                    StyledDropdownItem(icon = R.drawable.options_delete, label = "Delete Vinyl", tint = MaterialTheme.colorScheme.error) { onOptionsMenuChange(false); onDeleteVinyl() }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            VinylArtwork(
                coverArtUri = coverArtUri,
                artSize = 260.dp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp, bottom = 16.dp)
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = artist ?: "Unknown Artist",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = buildString {
                    append(if (sideCount == 1) "1 side" else "$sideCount sides")
                    append(" • ")
                    append(if (songCount == 1) "1 song" else "$songCount songs")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
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
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onViewSongs,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(size = 14.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.song_list),
                    contentDescription = "View Songs",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "View Songs")
            }
        }
    }
}

@Composable
fun VinylSongsView(
    vinylTitle: String,
    sides: List<VinylSide>,
    sideSongsMap: Map<String, List<Song>>,
    favouriteIds: Set<String>,
    isEditMode: Boolean,
    showOptionsMenu: Boolean,
    onOptionsMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onToggleEditMode: () -> Unit,
    onRemoveSong: (sideId: String, songId: String) -> Unit,
    onReorderSong: (sideId: String, orderedSongIds: List<String>) -> Unit,
    onAddSongs: () -> Unit,
    onManageSides: () -> Unit
) {
    val listState = rememberLazyListState()
    val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.nav_back_arrow),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = vinylTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
            Box {
                IconButton(onClick = { onOptionsMenuChange(true) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.options),
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { onOptionsMenuChange(false) },
                    offset = DpOffset(x = (-10).dp, y = (-10).dp),
                    shape = RoundedCornerShape(size = 20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    modifier = Modifier.width(220.dp)
                ) {
                    StyledDropdownItem(icon = R.drawable.song_options_playlist, label = "Add Songs to Vinyl") {
                        onOptionsMenuChange(false); onAddSongs()
                    }
                    StyledDropdownItem(
                        icon = R.drawable.options_edit,
                        label = if (isEditMode) "Done Editing" else "Edit Songs"
                    ) {
                        onOptionsMenuChange(false); onToggleEditMode()
                    }
                    StyledDropdownItem(icon = R.drawable.options_deletesome, label = "Manage Sides") {
                        onOptionsMenuChange(false); onManageSides()
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Songs List grouped by Side Cards
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .drawScrollbar(listState, scrollbarColor)
        ) {
            items(sides, key = { it.vinylSideId }) { side ->
                val songsForSide = sideSongsMap[side.vinylSideId] ?: emptyList()

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Header Box (Darker Rounded Cell)
                        Surface(
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = side.sideName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (songsForSide.size == 1) "1 song" else "${songsForSide.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Content Body inside card
                        if (songsForSide.isEmpty()) {
                            Text(
                                text = "No songs on this side yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                songsForSide.forEachIndexed { index, song ->
                                    if (isEditMode) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.width(24.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = song.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
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
                                                // Reorder controls
                                                IconButton(
                                                    onClick = {
                                                        if (index > 0) {
                                                            val reordered = songsForSide.toMutableList()
                                                            reordered.removeAt(index)
                                                            reordered.add(index - 1, song)
                                                            onReorderSong(side.vinylSideId, reordered.map { it.songId })
                                                        }
                                                    },
                                                    enabled = index > 0,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.chevron_up),
                                                        contentDescription = "Move Up",
                                                        tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (index < songsForSide.lastIndex) {
                                                            val reordered = songsForSide.toMutableList()
                                                            reordered.removeAt(index)
                                                            reordered.add(index + 1, song)
                                                            onReorderSong(side.vinylSideId, reordered.map { it.songId })
                                                        }
                                                    },
                                                    enabled = index < songsForSide.lastIndex,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.chevron_down),
                                                        contentDescription = "Move Down",
                                                        tint = if (index < songsForSide.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { onRemoveSong(side.vinylSideId, song.songId) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.options_delete),
                                                        contentDescription = "Remove from Side",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.width(28.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = song.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
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
                }
            }
        }
    }
}