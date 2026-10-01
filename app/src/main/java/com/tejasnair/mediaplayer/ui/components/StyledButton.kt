package com.tejasnair.mediaplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StyledButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 4.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 150),
        label = "BorderColorAnimation"
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 150),
        label = "LabelColorAnimation"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 150),
        label = "BorderWidthAnimation"
    )

    var labelWidthPx by remember { mutableIntStateOf(0) }
    var labelHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val shape = RoundedCornerShape(cornerRadius)
    val labelMarginDp = 8.dp

    Box(
        modifier = modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .semantics { role = Role.Button }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokePx = borderWidth.toPx()
                val crPx = cornerRadius.toPx()
                val labelPaddingPx = with(density) { 4.dp.toPx() }
                val labelMarginPx = with(density) { labelMarginDp.toPx() }

                val left = strokePx / 2f
                val top = strokePx / 2f
                val right = size.width - strokePx / 2f
                val bottom = size.height - strokePx / 2f

                // Offset starts explicitly after the left corner radius completes
                val labelStartPx = crPx + labelMarginPx
                val gapStart = (labelStartPx - labelPaddingPx).coerceIn(crPx, right - crPx)
                val gapEnd = (labelStartPx + labelWidthPx + labelPaddingPx).coerceIn(crPx, right - crPx)

                val path = Path().apply {
                    moveTo(gapEnd, top)

                    lineTo(right - crPx, top)
                    arcTo(
                        rect = Rect(right - 2 * crPx, top, right, top + 2 * crPx),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(right, bottom - crPx)
                    arcTo(
                        rect = Rect(right - 2 * crPx, bottom - 2 * crPx, right, bottom),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(left + crPx, bottom)
                    arcTo(
                        rect = Rect(left, bottom - 2 * crPx, left + 2 * crPx, bottom),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(left, top + crPx)
                    arcTo(
                        rect = Rect(left, top, left + 2 * crPx, top + 2 * crPx),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(gapStart, top)
                }

                drawPath(
                    path = path,
                    color = borderColor,
                    style = Stroke(width = strokePx)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .padding(ButtonDefaults.ContentPadding),
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment
            ) {
                val contentColor = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
                ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = contentColor)) {
                    content()
                }
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = cornerRadius + labelMarginDp,
                    y = with(density) { (-labelHeightPx / 2).toDp() }
                )
                .onGloballyPositioned { coordinates ->
                    labelWidthPx = coordinates.size.width
                    labelHeightPx = coordinates.size.height
                }
        )
    }
}