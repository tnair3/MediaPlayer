package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.DisplayList
import com.tejasnair.mediaplayer.ui.components.EmptyLibrary
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.FavouritesViewModel

@Composable
fun FavouritesScreen(
    viewModel: FavouritesViewModel,
    navController: NavController
) {
    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 56.dp, top = 16.dp)
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
                        text = "Favourites",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // ragebait
                HorizontalDivider(
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                if (viewModel.songs.values.isEmpty()) {
                    EmptyLibrary(
                        primaryText = "No favourites",
                        secondaryText = "Add songs to favourite to view"
                    )
                }
                else {
                    DisplayList(
                        items = viewModel.songs.values.toList().sortedWith(
                            comparator = compareBy(
                                { it.album?.title }, { it.discNumber }, { it.trackNumber }
                            )
                        ),
                        title = { it.title },
                        subtitle = { it.artist.name },
                        artModel = { it.artModel },
                        trackNumber = { -1 },
                        onClick = {  }
                            )
                    }
            }
        }
    }
}