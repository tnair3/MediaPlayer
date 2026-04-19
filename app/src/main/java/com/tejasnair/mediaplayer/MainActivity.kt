package com.tejasnair.mediaplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.data.local.database.MusicDatabase
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import com.tejasnair.mediaplayer.ui.components.MiniPlayer
import com.tejasnair.mediaplayer.ui.components.UploadToast
import com.tejasnair.mediaplayer.ui.screens.*
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme
import com.tejasnair.mediaplayer.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS at runtime on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val database = MusicDatabase.getDatabase(applicationContext)
        val repository = MusicRepository(database.musicDao(), applicationContext)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(repository))
            val playbackViewModel: PlaybackViewModel = viewModel()
            val uploadViewModel: UploadViewModel = viewModel()

            val scope = rememberCoroutineScope()

            val currentSongId = playbackViewModel.currentSongId
            val currentSong by remember(currentSongId) {
                currentSongId?.let { id -> libraryViewModel.getSong(id) } ?: flowOf(null)
            }.collectAsState(initial = null)

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val showNowPlaying = remember { mutableStateOf(false) }

            val uploadProgress by uploadViewModel.uploadProgress.collectAsState()

            MediaPlayerTheme {
                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
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
                        composable("upload") { UploadScreen(navController, uploadViewModel) }
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
                                    onNext = { playbackViewModel.skipToNext() },
                                    onPrevious = { playbackViewModel.skipToPrevious() },
                                    libraryViewModel = libraryViewModel,
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

                    UploadToast(progress = uploadProgress)
                }
            }
        }
    }
}