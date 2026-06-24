package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import coil.compose.AsyncImage
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Song

@Composable
fun EmptyLibrary(
    imageId: Int,
    primaryText: String,
    secondaryText: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon in a soft tinted box
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(imageId),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            text = primaryText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            style = MaterialTheme.typography.bodySmall,
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
    trackDuration: (T) -> String,
    onClick: (T) -> Unit,
    isFavourite: ((T) -> Boolean)
) {
    LazyColumn {
        items(items) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Outer card-like row container
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {

                    AsyncImage(
                        model = artModel(item),
                        contentDescription = "Art",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title(item),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${subtitle(item)} • ${trackDuration(item)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isFavourite(item)) {
                        Icon(
                            painter = painterResource(R.drawable.song_favourite_true),
                            contentDescription = "Favourite",
                            tint = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(16.dp)
                        )
                    }
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
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val containerShape = RoundedCornerShape(12.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(containerShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .drawWithContent {
                    drawContent()
                    if (song.isFavourite) {
                        val gradientWidth = 16.dp.toPx()
                        val cornerRadius = 10.dp.toPx()

                        val leftPath = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(gradientWidth, 0f)

                            cubicTo(
                                gradientWidth - cornerRadius, 0f,
                                gradientWidth - cornerRadius, cornerRadius,
                                gradientWidth - cornerRadius, cornerRadius
                            )

                            lineTo(gradientWidth - cornerRadius, size.height - cornerRadius)

                            cubicTo(
                                gradientWidth - cornerRadius, size.height - cornerRadius,
                                gradientWidth - cornerRadius, size.height,
                                gradientWidth, size.height
                            )

                            lineTo(0f, size.height)
                            close()
                        }

                        drawPath(
                            path = leftPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF2A6D).copy(alpha = 0.4f),
                                    Color(0xFFE91E63).copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = gradientWidth
                            )
                        )

                        val rightPath = Path().apply {
                            moveTo(size.width, 0f)
                            lineTo(size.width - gradientWidth, 0f)

                            cubicTo(
                                size.width - gradientWidth + cornerRadius, 0f,
                                size.width - gradientWidth + cornerRadius, cornerRadius,
                                size.width - gradientWidth + cornerRadius, cornerRadius
                            )

                            lineTo(size.width - gradientWidth + cornerRadius, size.height - cornerRadius)

                            cubicTo(
                                size.width - gradientWidth + cornerRadius, size.height - cornerRadius,
                                size.width - gradientWidth + cornerRadius, size.height,
                                size.width - gradientWidth, size.height
                            )

                            lineTo(size.width, size.height)
                            close()
                        }

                        drawPath(
                            path = rightPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFE91E63).copy(alpha = 0.2f),
                                    Color(0xFFFF2A6D).copy(alpha = 0.4f)
                                ),
                                startX = size.width - gradientWidth,
                                endX = size.width
                            )
                        )
                    }
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showTrackNumbers) {
                Text(
                    text = song.trackNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.artists,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = " • ${formatTime(song.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}