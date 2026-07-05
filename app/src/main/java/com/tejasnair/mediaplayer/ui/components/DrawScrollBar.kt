package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

fun Modifier.drawScrollbar(
    state: LazyListState,
    color: Color,
    width: Float = 6f
): Modifier = this.drawWithContent {
    drawContent()

    val layoutInfo = state.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount

    if (totalItemsCount > 0 && layoutInfo.visibleItemsInfo.size < totalItemsCount) {
        val visibleItems = layoutInfo.visibleItemsInfo
        val firstVisibleItem = visibleItems.firstOrNull()

        if (firstVisibleItem != null) {
            val totalHeight = size.height
            val receivedPixels = visibleItems.sumOf { it.size }.toFloat()
            val averageItemSize = receivedPixels / visibleItems.size
            val estimatedTotalHeight = averageItemSize * totalItemsCount
            val currentScrollOffset = (firstVisibleItem.index * averageItemSize) - firstVisibleItem.offset
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val scrollbarHeight = (viewportHeight / estimatedTotalHeight) * totalHeight
            val scrollbarTop = (currentScrollOffset / estimatedTotalHeight) * totalHeight
            val finalScrollbarHeight = scrollbarHeight.coerceIn(16f, totalHeight)
            val finalScrollbarTop = scrollbarTop.coerceIn(0f, totalHeight - finalScrollbarHeight)

            drawRoundRect(
                color = color,
                topLeft = Offset(size.width - width, finalScrollbarTop),
                size = Size(width, finalScrollbarHeight),
                cornerRadius = CornerRadius(width / 2, width / 2)
            )
        }
    }
}