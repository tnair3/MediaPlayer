package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text


@Composable
fun DiscHeader(discNumber: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    )
                )
        )

        Text(
            text = "Disc $discNumber",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
    }
}