package com.tejasnair.mediaplayer.ui.components

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// 2. Compose Runtime
import androidx.compose.runtime.Composable

// 3. Material3
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// 4. External Libraries
import coil.compose.AsyncImage

// 5. Local Project Imports
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song

@Composable
fun EmptyLibrary(
    imageId: Int,
    primaryText: String,
    secondaryText: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(64.dp),
            painter = painterResource(imageId),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Empty Library",
        )

        Text(
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = primaryText
        )

        Text(
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = secondaryText
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
    onClick: (T) -> Unit,
    isFavourite: ((T) -> Boolean)? = null
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

                Column(modifier = Modifier.weight(1f)) {
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
                if (isFavourite != null && isFavourite(item)) {
                    Icon(
                        painter = painterResource(R.drawable.song_favourite_true),
                        contentDescription = "Favourite Song",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    showTrackNumbers: Boolean,
    isFavourite: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(showTrackNumbers) {
            Text(
                text = song.trackNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(36.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artists,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isFavourite) {
            Icon(
                painter = painterResource(R.drawable.song_favourite_true),
                contentDescription = "Favourite Song",
                tint = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp)
            )
        }
    }
}