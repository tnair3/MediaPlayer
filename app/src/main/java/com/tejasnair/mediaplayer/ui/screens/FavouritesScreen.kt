package com.tejasnair.mediaplayer.ui.screens

// 1. Compose UI, Layout & Graphics
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 2. Compose Runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// 3. Material3
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// 4. Navigation
import androidx.navigation.NavController

// 5. Kotlin Standard Library
import kotlin.collections.emptyList

// 6. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.components.SongSheet
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
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
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(songs) {
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

                // Top Bar
                StandardUIBar(
                    navController = navController,
                    title = "Favourites"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section header with count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your liked songs",
                        style = MaterialTheme.typography.titleSmall, // smaller
                        color = MaterialTheme.colorScheme.onSurfaceVariant // softer
                    )

                    if (songs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${songs.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

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
                        onClick = { selectedSong = it },
                        isFavourite = { false }
                    )
                }
            }

            // Play/Shuffle
            if (songs.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                                )
                            )
                        )
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AnimatedVisibility(
                        visible = songs.isNotEmpty(),
                        enter = fadeIn() + scaleIn(initialScale = 0.9f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // PLAY (Primary)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.15f),
                                            CircleShape
                                        )
                                        .blur(8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.White, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            playbackViewModel.playSong(songs.first(), songs)
                                            showNowPlaying.value = true
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.song_play),
                                            contentDescription = "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // SHUFFLE (Secondary / greyed)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape) // clip BEFORE blur
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                            CircleShape
                                        )
                                        .blur(6.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .shadow(6.dp, CircleShape) // optional subtle lift
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            val shuffled = songs.shuffled()
                                            playbackViewModel.playSong(shuffled.first(), shuffled)
                                            showNowPlaying.value = true
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.song_shuffle),
                                            contentDescription = "Shuffle",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
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