package com.tejasnair.mediaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.data.local.database.MusicDatabase
import com.tejasnair.mediaplayer.data.local.files.MediaScanner
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import com.tejasnair.mediaplayer.ui.components.MiniPlayer
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme
import com.tejasnair.mediaplayer.ui.screens.*
import com.tejasnair.mediaplayer.ui.viewmodel.*

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
            val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(repository))
            val playbackViewModel: PlaybackViewModel = viewModel()

            val currentSongId = playbackViewModel.currentSongId

            val currentSong by remember(currentSongId) {
                currentSongId?.let { id ->
                    libraryViewModel.getSong(id)
                } ?: flowOf(null)
            }.collectAsState(initial = null)


            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            var showNowPlaying = remember { mutableStateOf(false) }

            MediaPlayerTheme() {
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
                        composable("library") { LibraryScreen(libraryViewModel, playbackViewModel, navController, showNowPlaying) }
                        composable("settings") { SettingsScreen(settingsViewModel, libraryViewModel, navController) }
                        composable("upload") { UploadScreen(navController, mediaScanner) }
                        composable("favourites") { FavouritesScreen(libraryViewModel, playbackViewModel, navController, showNowPlaying) }
                        composable("vinyls") { VinylsScreen(navController) }
                        composable("record") { RecordScreen(navController) }
                        composable(
                            route = "album/{albumName}/{albumArtist}",
                            arguments = listOf(
                                navArgument("albumName") { type = NavType.StringType },
                                navArgument("albumArtist") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("albumName") ?: ""
                            val artist = backStackEntry.arguments?.getString("albumArtist") ?: ""
                            AlbumScreen(name, artist, libraryViewModel, playbackViewModel, navController, showNowPlaying)
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    currentSong?.let { song ->
                        if (!showNowPlaying.value &&
                            currentRoute != "upload" &&
                            currentRoute != "settings"
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                MiniPlayer(
                                    song = song,
                                    isPlaying = playbackViewModel.isPlaying,
                                    onTogglePlay = { playbackViewModel.togglePlayPause() },
                                    onClick = { showNowPlaying.value = true },
                                    onDismiss = { playbackViewModel.stopPlayback() },
                                    playbackViewModel = playbackViewModel
                                )
                            }
                        }
                    }

                    val targetCornerSize by remember(sheetState) {
                        derivedStateOf {
                            val offset = kotlin.runCatching { sheetState.requireOffset() }.getOrDefault(0f)

                            val progress = (offset / 300f).coerceIn(0f, 1f)
                            (progress * 28).dp
                        }
                    }

                    val animatedCornerRadius by animateDpAsState(
                        targetValue = targetCornerSize,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "CornerAnimation"
                    )

                    if (showNowPlaying.value && currentSongId != null) {
                        ModalBottomSheet(
                            onDismissRequest = { showNowPlaying.value = false },
                            sheetState = sheetState,
                            dragHandle = null,
                            shape = RoundedCornerShape(topStart = animatedCornerRadius, topEnd = animatedCornerRadius),
                            scrimColor = Color.Black.copy(alpha = 0.5f),
                            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                NowPlayingScreen(
                                    libraryViewModel = libraryViewModel,
                                    playbackViewModel = playbackViewModel,
                                    onBackClick = {
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) showNowPlaying.value = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}