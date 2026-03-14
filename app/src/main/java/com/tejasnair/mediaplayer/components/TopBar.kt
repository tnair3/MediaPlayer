package com.tejasnair.mediaplayer.components

import com.tejasnair.mediaplayer.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigation(
    title: String,
    onUploadClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            title = { Text(
                style = MaterialTheme.typography.titleMedium,
                text = title
            )
                    },
            navigationIcon = {
                IconButton(onClick = onUploadClick) {
                    Icon(
                        painter = painterResource(R.drawable.nav_upload),
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(R.drawable.nav_settings),
                        contentDescription = "settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}