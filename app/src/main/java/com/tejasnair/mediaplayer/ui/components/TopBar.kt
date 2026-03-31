package com.tejasnair.mediaplayer.ui.components

// 1. Compose UI, Layout & Graphics
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// 2. Compose Runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// 3. Material3
import androidx.compose.material3.*
import androidx.compose.material3.Icon

// 4. Navigation
import androidx.navigation.NavController

// 5. Local Project Imports
import com.tejasnair.mediaplayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigation(
    title: String,
    onVinylClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onRecordedClick: () -> Unit,
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

                IconButton(onClick = onRecordedClick) {
                    Icon(
                        painter = painterResource(R.drawable.nav_recorded),
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
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ShowHelpMessage(
            title = title,
            onDismiss = { showHelp = false }
        )
    }


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

        if(title == "Recordings" || title == "Vinyls") {
            IconButton(
                onClick = { showHelp = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.help),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ShowHelpMessage(
    title: String,
    onDismiss: () -> Unit
) {
    val bodyText = when (title) {
        "Recordings" -> "This is where your recordings will appear. You can record audio directly in the app and it will be saved here for playback. Placeholder text — replace with your own description."
        "Vinyls" -> "Vinyls lets you visualise your music as a vinyl record. Placeholder text — replace with your own description."
        else -> "Help is not available for this screen."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}