package com.tejasnair.mediaplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap

private val DefaultDiscColor = Color(0xFF121212)

@Composable
private fun rememberDiscColor(coverArtUri: String?): Color {
    val context = LocalContext.current

    val state = produceState(initialValue = DefaultDiscColor, key1 = coverArtUri) {
        if (coverArtUri.isNullOrBlank()) {
            value = DefaultDiscColor
            return@produceState
        }

        val bitmap = try {
            val request = ImageRequest.Builder(context)
                .data(coverArtUri)
                .allowHardware(false) // needed so Palette can read pixels
                .build()
            val result = context.imageLoader.execute(request)
            (result as? SuccessResult)?.drawable
                ?.let { drawable ->
                    val bmp = createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(1),
                        drawable.intrinsicHeight.coerceAtLeast(1)
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp
                }
        } catch (e: Exception) {
            null
        }

        if (bitmap == null) {
            value = DefaultDiscColor
            return@produceState
        }

        val swatchColor = withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).generate()
            val swatch = palette.darkVibrantSwatch
                ?: palette.darkMutedSwatch
                ?: palette.dominantSwatch
                ?: palette.mutedSwatch
                ?: palette.vibrantSwatch
            swatch?.rgb?.let { Color(it) }
        }

        value = swatchColor ?: DefaultDiscColor
    }

    return state.value
}

@Composable
fun VinylArtwork(
    coverArtUri: String?,
    artSize: Dp,
    modifier: Modifier = Modifier
) {
    val discSize = artSize * 0.95f
    val discVisibleOffset = discSize * 0.45f

    val discColor by animateColorAsState(
        targetValue = rememberDiscColor(coverArtUri),
        label = "vinylDiscColor"
    )

    Box(
        modifier = modifier
            .width(artSize + discVisibleOffset + 20.dp)
            .height(artSize + 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(artSize + 20.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(28.dp)
                )
                .blur(20.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = discSize * 0.45f)
                .size(discSize)
                .clip(CircleShape)
                .background(discColor)
        ) {
            listOf(0.9f, 0.75f, 0.55f, 0.35f).forEach { ringFraction ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(discSize * ringFraction)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f))
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(discSize * 0.25f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(discSize * 0.05f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(artSize)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = coverArtUri,
                contentDescription = "Vinyl Cover",
                contentScale = ContentScale.Crop
            )
        }
    }
}