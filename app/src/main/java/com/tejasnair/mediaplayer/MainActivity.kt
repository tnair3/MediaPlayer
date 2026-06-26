package com.tejasnair.mediaplayer

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.flowOf
import com.tejasnair.mediaplayer.data.local.database.MusicDatabase
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import com.tejasnair.mediaplayer.ui.components.ExpandingPlayerBound
import com.tejasnair.mediaplayer.ui.components.UploadToast
import com.tejasnair.mediaplayer.ui.screens.*
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme
import com.tejasnair.mediaplayer.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(repository, application))
            val playbackViewModel: PlaybackViewModel = viewModel()
            val uploadViewModel: UploadViewModel = viewModel()

            val currentSongId = playbackViewModel.currentSongId
            val currentSong by remember(currentSongId) {
                currentSongId?.let { id -> libraryViewModel.getSong(id) } ?: flowOf(null)
            }.collectAsState(initial = null)

            var isPlayerExpanded by remember { mutableStateOf(false) }
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
                        composable("library") { LibraryScreen(libraryViewModel, playbackViewModel, navController, mutableStateOf(isPlayerExpanded)) }
                        composable("settings") { SettingsScreen(settingsViewModel, libraryViewModel, navController) }
                        composable("upload") { UploadScreen(navController, uploadViewModel) }
                        composable("favourites") { FavouritesScreen(libraryViewModel, playbackViewModel, navController, mutableStateOf(isPlayerExpanded)) }
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
                            AlbumScreen(name, artist, libraryViewModel, playbackViewModel, navController, mutableStateOf(isPlayerExpanded))
                        }
                        composable(
                            route = "playlist/{playlistId}",
                            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                            PlaylistScreen(playlistId, libraryViewModel, playbackViewModel, navController, mutableStateOf(isPlayerExpanded))
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    currentSong?.let { song ->
                        if (currentRoute != "upload" && currentRoute != "settings") {
                            ExpandingPlayerBound(
                                song = song,
                                isExpanded = isPlayerExpanded,
                                onExpandToggle = { isPlayerExpanded = it },
                                playbackViewModel = playbackViewModel,
                                libraryViewModel = libraryViewModel
                            )
                        }
                    }

                    UploadToast(progress = uploadProgress)
                }
            }
        }
    }
}