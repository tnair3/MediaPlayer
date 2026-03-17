package com.tejasnair.mediaplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.tejasnair.mediaplayer.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigation(
    title: String,
    onVinylClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onUploadClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {

            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onVinylClick) {
                    Icon(
                        painter = painterResource(R.drawable.nav_vinyl),
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onFavouriteClick) {
                    Icon(
                        painter = painterResource(R.drawable.navdisp_favourite),
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onUploadClick) {
                    Icon(
                        painter = painterResource(R.drawable.nav_upload),
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(R.drawable.nav_settings),
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FilterRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->

            val isSelected = index == selectedIndex

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent
                    )
                    .clickable { onOptionSelected(index) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StandardUIBar(
    navController: NavController,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {

        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.nav_back_arrow),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}