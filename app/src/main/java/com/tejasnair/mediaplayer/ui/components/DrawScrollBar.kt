package com.tejasnair.mediaplayer.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.ceil

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.drawScrollbar(
    state: LazyListState,
    color: Color,
    width: Float = 6f
): Modifier = composed {
    this.drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val totalItemsCount = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo

        if (totalItemsCount > 0 && visibleItems.isNotEmpty()) {
            val firstVisibleItem = visibleItems.first()
            val lastVisibleItem = visibleItems.last()
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val estimatedItemSize = firstVisibleItem.size.toFloat()
            val estimatedTotalHeight = estimatedItemSize * totalItemsCount

            if (estimatedTotalHeight > viewportHeight) {
                val totalHeight = size.height
                val currentScrollOffset = (firstVisibleItem.index * estimatedItemSize) - firstVisibleItem.offset
                val scrollbarHeight = ((viewportHeight / estimatedTotalHeight) * totalHeight).coerceIn(16f, totalHeight)
                val isAtTop = firstVisibleItem.index == 0 && firstVisibleItem.offset == 0
                val isAtBottom = lastVisibleItem.index == totalItemsCount - 1 && (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset
                val finalScrollbarTop = when {
                    isAtTop -> 0f
                    isAtBottom -> totalHeight - scrollbarHeight
                    else -> {
                        val estimatedScrollTop = (currentScrollOffset / estimatedTotalHeight) * totalHeight
                        estimatedScrollTop.coerceIn(0f, totalHeight - scrollbarHeight)
                    }
                }

                if (!finalScrollbarTop.isNaN() && !scrollbarHeight.isNaN()) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(size.width - width, finalScrollbarTop),
                        size = Size(width, scrollbarHeight),
                        cornerRadius = CornerRadius(width / 2, width / 2)
                    )
                }
            }
        }
    }
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.drawScrollbar(
    state: LazyGridState,
    color: Color,
    width: Float = 6f
): Modifier = composed {
    this.drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val totalItemsCount = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo

        if (totalItemsCount > 0 && visibleItems.isNotEmpty()) {
            val firstVisibleItem = visibleItems.first()
            val lastVisibleItem = visibleItems.last()
            val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()

            // Calculate column/span count dynamically from visible items
            val slotsPerLine = visibleItems.maxOfOrNull { it.column + 1 } ?: 1
            val totalRows = ceil(totalItemsCount.toDouble() / slotsPerLine).toInt()
            val firstVisibleRow = firstVisibleItem.row
            val lastVisibleRow = lastVisibleItem.row

            val estimatedRowHeight = firstVisibleItem.size.height.toFloat()
            val estimatedTotalHeight = estimatedRowHeight * totalRows

            if (estimatedTotalHeight > viewportHeight) {
                val totalHeight = size.height
                val currentScrollOffset = (firstVisibleRow * estimatedRowHeight) - firstVisibleItem.offset.y
                val scrollbarHeight = ((viewportHeight / estimatedTotalHeight) * totalHeight).coerceIn(16f, totalHeight)

                val isAtTop = firstVisibleRow == 0 && firstVisibleItem.offset.y == 0
                val isAtBottom = lastVisibleRow == totalRows - 1 && (lastVisibleItem.offset.y + lastVisibleItem.size.height) <= layoutInfo.viewportEndOffset

                val finalScrollbarTop = when {
                    isAtTop -> 0f
                    isAtBottom -> totalHeight - scrollbarHeight
                    else -> {
                        val estimatedScrollTop = (currentScrollOffset / estimatedTotalHeight) * totalHeight
                        estimatedScrollTop.coerceIn(0f, totalHeight - scrollbarHeight)
                    }
                }

                if (!finalScrollbarTop.isNaN() && !scrollbarHeight.isNaN()) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(size.width - width, finalScrollbarTop),
                        size = Size(width, scrollbarHeight),
                        cornerRadius = CornerRadius(width / 2, width / 2)
                    )
                }
            }
        }
    }
}