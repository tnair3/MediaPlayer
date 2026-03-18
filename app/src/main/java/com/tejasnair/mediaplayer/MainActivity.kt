package com.tejasnair.mediaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.navigation.navArgument
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme
import com.tejasnair.mediaplayer.ui.screens.*
import com.tejasnair.mediaplayer.ui.viewmodel.*
import com.tejasnair.mediaplayer.data.local.database.MusicDatabase
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import com.tejasnair.mediaplayer.data.local.files.MediaScanner
import com.tejasnair.mediaplayer.ui.components.MiniPlayer
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MusicDatabase.getDatabase(applicationContext)
        val repository = MusicRepository(database.musicDao(), applicationContext)
        val mediaScanner = MediaScanner(applicationContext, repository)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModelFactory(repository)
            )
            val playbackViewModel: PlaybackViewModel = viewModel()

            val currentSong = playbackViewModel.currentSong
            val uiState by settingsViewModel.themeSetting.collectAsStateWithLifecycle()

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var showNowPlaying by remember { mutableStateOf(false) }

            LaunchedEffect(currentSong) {
                if (currentSong != null) {
                    showNowPlaying = true
                }
            }

            MediaPlayerTheme(themeSetting = uiState) {
                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (BuildConfig.DEBUG) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onError,
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "DEBUG BUILD",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "library",
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
                    ) {
                        composable("library") { LibraryScreen(libraryViewModel, navController, playbackViewModel) }
                        composable("settings") { SettingsScreen(settingsViewModel, navController, uiState) { settingsViewModel.updateTheme(it) } }
                        composable("upload") { UploadScreen(navController, mediaScanner) }
                        composable("favourites") { FavouritesScreen(navController) }
                        composable("vinyls") { VinylsScreen(navController) }
                        composable(
                            route = "album/{albumName}/{albumArtist}",
                            arguments = listOf(
                                navArgument("albumName") { type = NavType.StringType },
                                navArgument("albumArtist") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("albumName") ?: ""
                            val artist = backStackEntry.arguments?.getString("albumArtist") ?: ""
                            AlbumScreen(name, artist, libraryViewModel, playbackViewModel)
                        }
                    }

                    if (currentSong != null && !showNowPlaying) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd // Pinned to bottom right
                        ) {
                            MiniPlayer(
                                song = currentSong,
                                isPlaying = playbackViewModel.isPlaying,
                                onTogglePlay = { playbackViewModel.togglePlayPause() },
                                onClick = { showNowPlaying = true },
                                onDismiss = { playbackViewModel.stopPlayback() },
                                viewModel = playbackViewModel
                            )
                        }
                    }

                    if (showNowPlaying && currentSong != null) {
                        ModalBottomSheet(
                            onDismissRequest = { showNowPlaying = false },
                            sheetState = sheetState,
                            containerColor = MaterialTheme.colorScheme.surface,
                            dragHandle = { BottomSheetDefaults.DragHandle() }
                        ) {
                            NowPlayingScreen(
                                viewModel = playbackViewModel,
                                onBackClick = { showNowPlaying = false }
                            )
                        }
                    }
                }
            }
        }
    }
}