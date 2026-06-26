package com.tejasnair.mediaplayer.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.ui.screens.NowPlayingScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ExpandingPlayerBound(
    song: Song,
    isExpanded: Boolean,
    onExpandToggle: (Boolean) -> Unit,
    playbackViewModel: PlaybackViewModel,
    libraryViewModel: LibraryViewModel
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val miniPlayerHeightPx = with(density) { 88.dp.toPx() }
    val collapsedOffsetY = screenHeightPx - miniPlayerHeightPx

    val animatedOffset = remember { Animatable(collapsedOffsetY) }

    val fraction by remember {
        derivedStateOf {
            ((collapsedOffsetY - animatedOffset.value) / collapsedOffsetY).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(isExpanded) {
        val target = if (isExpanded) 0f else collapsedOffsetY
        animatedOffset.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedOffset.value.roundToInt()) }
            .draggable(
                state = rememberDraggableState { delta ->
                    scope.launch {
                        animatedOffset.snapTo((animatedOffset.value + delta).coerceIn(0f, collapsedOffsetY))
                    }
                },
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    val shouldExpand = velocity < -500f || (velocity <= 500f && animatedOffset.value < collapsedOffsetY / 2f)
                    onExpandToggle(shouldExpand)

                    scope.launch {
                        animatedOffset.animateTo(
                            targetValue = if (shouldExpand) 0f else collapsedOffsetY,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
            )
            .background(Color.Transparent)
    ) {
        if (fraction > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = fraction }
            ) {
                NowPlayingScreen(
                    libraryViewModel = libraryViewModel,
                    playbackViewModel = playbackViewModel,
                    onBackClick = { onExpandToggle(false) },
                    expansionFraction = fraction
                )
            }
        }

        if (fraction < 0.99f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .graphicsLayer { alpha = 1f - fraction }
            ) {
                MiniPlayer(
                    song = song,
                    isPlaying = playbackViewModel.isPlaying,
                    onTogglePlay = { playbackViewModel.togglePlayPause() },
                    onClick = { onExpandToggle(true) },
                    onDismiss = { playbackViewModel.stopPlayback() },
                    onNext = { playbackViewModel.skipToNext() },
                    onPrevious = { playbackViewModel.skipToPrevious() },
                    libraryViewModel = libraryViewModel,
                    playbackViewModel = playbackViewModel
                )
            }
        }
    }
}