package com.tejasnair.mediaplayer.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tejasnair.mediaplayer.ui.theme.MediaPlayerTheme

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to my app")
            ClickableButton()
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
                if(!buttonText.equals("Clicked")) {
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