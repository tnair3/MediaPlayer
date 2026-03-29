package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen

@Composable
fun RecordScreen(
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
                    title = "Recorded"
                )
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