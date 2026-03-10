package com.tejasnair.mediaplayer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ClickableButton() {
    var buttonText by remember { mutableStateOf("Button") }
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