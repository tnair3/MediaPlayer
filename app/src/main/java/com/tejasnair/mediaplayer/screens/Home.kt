package com.tejasnair.mediaplayer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tejasnair.mediaplayer.components.*
import com.tejasnair.mediaplayer.ui.theme.*
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Main content in a Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Welcome to my app")
                ClickableButton()
            }

            // Bottom navbar
            NavigationBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }
    }
}



@Composable
fun ClickableButton() {
    var buttonText by remember { mutableStateOf("Meow") }
    var clickNum by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                if(buttonText != "Clicked") {
                    buttonText = "Clicked"
                }
                clickNum++
            }
        ) {
            if(clickNum > 0) {
                Text("$buttonText $clickNum")
            }
            else {
                Text(buttonText)
            }
        }
    }
}