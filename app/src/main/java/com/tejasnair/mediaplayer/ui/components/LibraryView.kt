package com.tejasnair.mediaplayer.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

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
            painter = painterResource(R.drawable.disp_empty_library),
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
fun <T> DisplayList(
    items: List<T>,
    title: (T) -> String,
    subtitle: (T) -> String,
    artModel: (T) -> Any?,
    trackNumber: (T) -> Int,
    onClick: (T) -> Unit
) {
    LazyColumn {
        items(items) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onClick(item) }
            ) {

                if(trackNumber(item) != -1) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trackNumber(item).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if(artModel(item) != -1) {
                    AsyncImage(
                        model = artModel(item),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 4.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title(item),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitle(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}