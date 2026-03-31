package com.tejasnair.mediaplayer.ui.screens

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 2. Compose Runtime
import androidx.compose.runtime.Composable

// 3. Material3
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme

// 4. Navigation
import androidx.navigation.NavController

// 5. Local Project Imports
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen

@Composable
fun VinylsScreen(
    navController: NavController
) {
    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 56.dp, top = 16.dp)
            ) {
                StandardUIBar(
                    navController = navController,
                    title = "Vinyls"
                )
                // ragebait
                HorizontalDivider(
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}