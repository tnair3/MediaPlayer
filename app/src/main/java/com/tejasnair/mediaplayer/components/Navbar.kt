package com.tejasnair.mediaplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.R

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(modifier = modifier) {
        NavbarLine()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            IconButton(onClick = { onNavigate("home") }) {
                Icon(painterResource(R.drawable.home), null)
            }

            IconButton(onClick = { onNavigate("library") }) {
                Icon(painterResource(R.drawable.library), null)
            }

            IconButton(onClick = { onNavigate("settings") }) {
                Icon(painterResource(R.drawable.settings), null)
            }
        }
    }
}

@Composable
fun NavbarLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 32.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Gray,
                        Color.Transparent
                    )
                )
            )
    )
}