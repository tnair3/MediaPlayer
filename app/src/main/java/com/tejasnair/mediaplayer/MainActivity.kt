package com.tejasnair.mediaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme
import com.tejasnair.mediaplayer.ui.screens.*
import com.tejasnair.mediaplayer.ui.viewmodel.*
import com.tejasnair.mediaplayer.data.local.MusicDatabase
import com.tejasnair.mediaplayer.data.repository.MusicRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MusicDatabase.getDatabase(applicationContext)
        val repository = MusicRepository(database.musicDao())

        setContent {

            val settingsViewModel: SettingsViewModel = viewModel()
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModelFactory(repository)
            )

            // Collect the theme preference from the ViewModel
            val uiState by settingsViewModel.themeSetting.collectAsStateWithLifecycle()

            MediaPlayerTheme(themeSetting = uiState) {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "library",
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    composable(
                        route = "library"
                    ) {
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            navController = navController
                        )
                    }

                    composable(
                        route = "settings"
                    ) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            navController = navController,
                            currentSetting = uiState,
                            onSettingChanged = { settingsViewModel.updateTheme(it) }
                        )
                    }

                    composable(
                        route = "upload"
                    ) {
                        UploadScreen(
                            navController = navController
                        )
                    }

                    composable(
                        route = "favourites"
                    ) {
                        FavouritesScreen(
                            navController = navController
                        )
                    }

                    composable(
                        route = "vinyls"
                    ) {
                        VinylsScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}