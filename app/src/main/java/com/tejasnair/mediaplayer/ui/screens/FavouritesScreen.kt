package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.navigation.NavController
import kotlin.collections.emptyList
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.formatTime
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel

@Composable
fun FavouritesScreen(
    libraryViewModel: LibraryViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavController,
    showNowPlaying: MutableState<Boolean>
) {
    val songs by libraryViewModel.favouriteSongs.collectAsState(initial = emptyList())
    var selectedSong by remember { mutableStateOf<Song?>(value = null) }

    LaunchedEffect(key1 = songs) {
        if (selectedSong != null && selectedSong !in songs) {
            selectedSong = null
        }
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
                // Decorative header card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(shape = RoundedCornerShape(size = 20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.nav_back_arrow),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Liked Songs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text =
                                    if (songs.isEmpty()) "No songs yet"
                                    else "${songs.size} song${if (songs.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Large decorative heart
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.song_favourite_true),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Options button
                            IconButton(
                                modifier = Modifier,
                                onClick = {  }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.options),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
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

                Spacer(modifier = Modifier.height(4.dp))

                if (songs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyLibrary(
                            imageId = R.drawable.song_favourite_none,
                            primaryText = "No favourites yet",
                            secondaryText = "Tap the heart icon on any song"
                        )
                    }
                } else {
                    DisplayList(
                        items = songs,
                        title = { it.title },
                        subtitle = { it.artists },
                        artModel = { it.songArtUri },
                        trackNumber = { -1 },
                        trackDuration = { formatTime(ms = it.duration) },
                        onClick = { selectedSong = it },
                        isFavourite = { false }
                    )
                }
            }

            // Floating play/shuffle buttons with backdrop
            if (songs.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AnimatedVisibility(
                        visible = songs.isNotEmpty(),
                        enter = fadeIn() + scaleIn(initialScale = 0.9f)
                    ) {
                        // Subtle frosted backdrop behind the buttons
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .clip(shape = RoundedCornerShape(size = 40.dp))
                                .border(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    width = 2.dp,
                                    shape = RoundedCornerShape(size = 40.dp)
                                )
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(vertical = 12.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    enabled = true,
                                    onClick = {  }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Play button
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(76.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            )
                                            .blur(radius = 8.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .shadow(elevation = 8.dp, shape = CircleShape)
                                            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                                            .clip(shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                playbackViewModel.playSong(selectedSong = songs.first(), playlist = songs)
                                                showNowPlaying.value = true
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.song_play),
                                                contentDescription = "Play",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }

                                // Shuffle button
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                            .blur(6.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(elevation = 4.dp, shape =  CircleShape)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = CircleShape
                                            )
                                            .clip(shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val shuffled = songs.shuffled()
                                                playbackViewModel.playSong(selectedSong = shuffled.first(), playlist = shuffled)
                                                showNowPlaying.value = true
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.song_shuffle),
                                                contentDescription = "Shuffle",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp)
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

        selectedSong?.let { song ->
            SongSheet(
                song = song,
                playlist = songs,
                libraryViewModel = libraryViewModel,
                playbackViewModel = playbackViewModel,
                onDelete = { libraryViewModel.deleteSong(it) },
                onDismiss = { selectedSong = null },
                showNowPlaying = showNowPlaying
            )
        }
    }
}