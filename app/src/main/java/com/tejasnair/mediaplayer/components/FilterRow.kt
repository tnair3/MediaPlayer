package com.tejasnair.mediaplayer.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun FilterRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(options) { index, label ->

            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(
                        if (isSelected) Color.White else Color.Transparent
                    )
                    .clickable { onOptionSelected(index) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.Black else Color.White
                )
            }
        }
    }
}