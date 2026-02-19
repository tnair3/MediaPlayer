package com.tejasnair.mediaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tejasnair.mediaplayer.components.NavigationBar
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme
import com.tejasnair.mediaplayer.screens.*
import com.tejasnair.mediaplayer.components.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaPlayerTheme {

                val navController = rememberNavController()
                HomeScreen()
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    )

}
