package com.tejasnair.mediaplayer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.R
import androidx.compose.foundation.lazy.LazyColumn
import com.tejasnair.mediaplayer.data.Song
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun EmptyLibrary() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(64.dp),
            painter = painterResource(R.drawable.empty_library),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Empty Library",
        )

        Text(
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = "Library is empty"
        )

        Text(
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = "Upload media to listen"
        )
    }
}

@Composable
fun LibraryDisplay(songs: List<Song>) {
    LazyColumn {
        items(songs) { song ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Placeholder for artwork
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}